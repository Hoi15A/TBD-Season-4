package util.api

import APIKeys
import chat.Formatting
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import item.ItemRarity
import item.ItemType
import item.convertRarity
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.tbdsmp.tbdseason4.ActiveIslandExchangeListingsQuery
import net.tbdsmp.tbdseason4.PreviousExchangeSalesQuery
import net.tbdsmp.tbdseason4.type.CosmeticCategory
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import util.dateTimeDifference
import java.text.NumberFormat
import java.util.*

object IslandAPI {
    private val apolloClient = ApolloClient.Builder()
        .serverUrl("https://api.mccisland.net/graphql")
        .addHttpInterceptor(IslandAPIKeyInterceptor(APIKeys.getIslandAPIKey()))
        .build()

    fun getListings(): List<Listings> = runBlocking {
        val listings = mutableListOf<Listings>()
        val response = apolloClient.query(ActiveIslandExchangeListingsQuery()).execute()
        val queryListings = response.data?.activeIslandExchangeListings ?: emptyList()

        for(listing in queryListings) {
            if(listing.asset.onCosmeticToken != null) {
                val cosmeticToken = listing.asset.onCosmeticToken
                val rarity = cosmeticToken.rarity.convertRarity()
                val item = ItemStack(getCosmeticMaterial(cosmeticToken.cosmetic.category), listing.amount)
                var meta = item.itemMeta
                meta.setMaxStackSize(64)
                meta.displayName(Formatting.allTags.deserialize("<!i><${rarity.colourHex}>${cosmeticToken.name} Token"))
                meta.lore(listOf(
                    Formatting.allTags.deserialize("<!i><white>${rarity.rarityGlyph}${ItemType.CONSUMABLE.typeGlyph}"),
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>Unlock the \"${cosmeticToken.name}\" ${cosmeticToken.cosmetic.category.name.lowercase().replace("_", " ")}"),
                    Formatting.allTags.deserialize("<!i><aqua>in your wardrobe."),
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><gray>Remaining Time: <white>${dateTimeDifference(listing.endTime.toString())}"),
                    Formatting.allTags.deserialize("<!i><gray>Listed Price: <#ffff00>\uD83E\uDE99<white>${NumberFormat.getIntegerInstance().format(listing.cost)}"),
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>Join <b><white>play.<#ffff00>mccisland<white>.net<aqua></b> to purchase."),
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><tbdcolour>> <key:key.attack> <white>to inspect cosmetic"),
                    Formatting.allTags.deserialize("<!i><tbdcolour>> <key:key.use> <white>to view last 24hrs sales"),
                    Formatting.allTags.deserialize("<!i><tbdcolour>> Shift + <key:key.use> <white>to share to chat")
                ))
                if(item.type in listOf(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.LEATHER_HORSE_ARMOR)) {
                    meta = meta as LeatherArmorMeta
                    meta.setColor(rarity.colour)
                }
                meta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES)
                item.itemMeta = meta
                listings.add(Listings(item, listing.creationTime.toString(), listing.endTime.toString(), IslandAssetType.COSMETIC_TOKEN, rarity, listing.cost))
            }
            if(listing.asset.onSimpleAsset != null) {
                val simpleAsset = listing.asset.onSimpleAsset
                val rarity = simpleAsset.rarity.convertRarity()
                val item = ItemStack(getSimpleAssetMaterial(simpleAsset.name), listing.amount)
                val meta = item.itemMeta
                meta.displayName(Formatting.allTags.deserialize("<!i><${rarity.colourHex}>${simpleAsset.name}"))

                val loreLines = mutableListOf<Component>()
                loreLines.add(Formatting.allTags.deserialize("<!i><white>${rarity.rarityGlyph}${ItemType.CONSUMABLE.typeGlyph}"))
                for(component in getSimpleAssetLore(simpleAsset.name)) loreLines.add(component)
                val auctionLoreLines = listOf(
                    Formatting.allTags.deserialize("<!i><gray>Remaining Time: <white>${dateTimeDifference(listing.endTime.toString())}"),
                    Formatting.allTags.deserialize("<!i><gray>Listed Price: <#ffff00>\uD83E\uDE99<white>${NumberFormat.getIntegerInstance().format(listing.cost)}"),
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>Join <b><white>play.<#ffff00>mccisland<white>.net<aqua></b> to purchase"),
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><tbdcolour>> <key:key.use> <white>to view last 24hrs sales"),
                    Formatting.allTags.deserialize("<!i><tbdcolour>> Shift + <key:key.use> <white>to share to chat")
                )
                for(component in auctionLoreLines) loreLines.add(component)
                meta.lore(loreLines)
                item.itemMeta = meta
                val assetType = if(simpleAsset.name.contains("MCC+ Token")) IslandAssetType.MCC_PLUS_TOKEN else IslandAssetType.OTHER
                listings.add(Listings(item, listing.creationTime.toString(), listing.endTime.toString(), assetType, rarity, listing.cost))
            }
        }
        listings
    }

    fun previousSales(soldItem: String): List<Listings> = runBlocking {
        val listings = mutableListOf<Listings>()
        val response = apolloClient.query(PreviousExchangeSalesQuery()).execute()
        val queryListings = response.data?.soldIslandExchangeListings ?: emptyList()

        for(listing in queryListings) {
            if(listing.asset.onCosmeticToken != null) {
                val cosmeticToken = listing.asset.onCosmeticToken
                if(cosmeticToken.name == soldItem) {
                    val rarity = cosmeticToken.rarity.convertRarity()
                    val item = ItemStack(getCosmeticMaterial(cosmeticToken.cosmetic.category), listing.amount)
                    var meta = item.itemMeta
                    meta.displayName(Formatting.allTags.deserialize("<!i><${rarity.colourHex}>${cosmeticToken.name} Token<white>: Sales Data"))
                    meta.lore(listOf(
                        Formatting.allTags.deserialize("<!i><white>${rarity.rarityGlyph}${ItemType.CONSUMABLE.typeGlyph}"),
                        Formatting.allTags.deserialize("<!i>"),
                        Formatting.allTags.deserialize("<!i><aqua>Unlock the \"${cosmeticToken.name}\" ${cosmeticToken.cosmetic.category.name.lowercase()}"),
                        Formatting.allTags.deserialize("<!i><aqua>in your wardrobe."),
                        Formatting.allTags.deserialize("<!i>")
                    ))
                    if(item.type in listOf(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.LEATHER_HORSE_ARMOR)) {
                        meta = meta as LeatherArmorMeta
                        meta.setColor(rarity.colour)
                    }
                    meta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES)
                    item.itemMeta = meta
                    listings.add(Listings(item, listing.creationTime.toString(), listing.endTime.toString(), IslandAssetType.COSMETIC_TOKEN, rarity, listing.cost))
                }
            }
            if(listing.asset.onSimpleAsset != null) {
                val simpleAsset = listing.asset.onSimpleAsset
                if(simpleAsset.name == soldItem) {
                    val rarity = simpleAsset.rarity.convertRarity()
                    val item = ItemStack(getSimpleAssetMaterial(simpleAsset.name), listing.amount)
                    val meta = item.itemMeta
                    meta.displayName(Formatting.allTags.deserialize("<!i><${rarity.colourHex}>${simpleAsset.name}<white>: Sales Data"))
                    val loreLines = mutableListOf<Component>()
                    loreLines.add(Formatting.allTags.deserialize("<!i><white>${rarity.rarityGlyph}${ItemType.CONSUMABLE.typeGlyph}"))
                    for(component in getSimpleAssetLore(simpleAsset.name)) loreLines.add(component)
                    meta.lore(loreLines)
                    item.itemMeta = meta
                    listings.add(Listings(item, listing.creationTime.toString(), listing.endTime.toString(), IslandAssetType.COSMETIC_TOKEN, rarity, listing.cost))
                }
            }
        }
        listings
    }

    fun getCosmetic(cosmeticName: String): ItemStack = runBlocking {
        var itemStack = ItemStack.empty()
        val response = apolloClient.query(ActiveIslandExchangeListingsQuery()).execute()
        val queryListings = response.data?.activeIslandExchangeListings ?: emptyList()

        for(listing in queryListings) {
            if(listing.asset.onCosmeticToken != null) {
                val cosmeticToken = listing.asset.onCosmeticToken
                if(cosmeticToken.name == cosmeticName) {
                    val cosmetic = cosmeticToken.cosmetic
                    val rarity = cosmetic.rarity.convertRarity()
                    val item = ItemStack(getCosmeticMaterial(cosmetic.category), listing.amount)
                    var meta = item.itemMeta
                    meta.displayName(Formatting.allTags.deserialize("<!i><${rarity.colourHex}>${cosmetic.name}"))
                    meta.lore(listOf(
                        Formatting.allTags.deserialize("<!i><white>${rarity.rarityGlyph}${ItemType.CONSUMABLE.typeGlyph}"),
                        Formatting.allTags.deserialize("<!i>"),
                        Formatting.allTags.deserialize("<!i><tbdcolour>Obtainment:"),
                        Formatting.allTags.deserialize("<!i><gray>${cosmetic.obtainmentHint}"),
                        Formatting.allTags.deserialize("<!i>"),
                        Formatting.allTags.deserialize("<!i><tbdcolour>Type: <white>${cosmetic.type.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}"),
                        Formatting.allTags.deserialize("<!i><tbdcolour>Collection: <white>${cosmetic.collection}"),
                        Formatting.allTags.deserialize("<!i><tbdcolour>Trophies: <white>${cosmetic.trophies}"),
                        Formatting.allTags.deserialize("<!i><tbdcolour>Global amount owned: <white>${cosmetic.globalNumberOwned}")

                    ))
                    if(item.type in listOf(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.LEATHER_HORSE_ARMOR)) {
                        meta = meta as LeatherArmorMeta
                        meta.setColor(rarity.colour)
                    }
                    meta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES)
                    item.itemMeta = meta
                    itemStack = item
                }
            }
        }
        itemStack
    }

    private fun getCosmeticMaterial(cosmeticCategory: CosmeticCategory): Material {
        return when(cosmeticCategory) {
            CosmeticCategory.HAT -> Material.LEATHER_HELMET
            CosmeticCategory.HAIR -> Material.LEATHER_HELMET
            CosmeticCategory.ACCESSORY -> Material.LEATHER_HORSE_ARMOR
            CosmeticCategory.AURA -> Material.LEATHER_BOOTS
            CosmeticCategory.TRAIL -> Material.LEATHER_LEGGINGS
            CosmeticCategory.CLOAK -> Material.LEATHER_CHESTPLATE
            CosmeticCategory.ROD -> Material.FISHING_ROD
            CosmeticCategory.BOW -> Material.BOW
            CosmeticCategory.CROSSBOW -> Material.CROSSBOW
            CosmeticCategory.DAGGER -> Material.WOODEN_SWORD
            CosmeticCategory.HEAVY_CROSSBOW -> Material.CROSSBOW
            CosmeticCategory.SHORTBOW -> Material.BOW
            CosmeticCategory.SWORD -> Material.STONE_SWORD
            CosmeticCategory.UNKNOWN__ -> Material.STRUCTURE_VOID
        }
    }

    private fun getSimpleAssetMaterial(simpleAssetName: String): Material {
       return with(simpleAssetName) {
           when {
               contains("30d MCC+ Token") -> Material.PURPLE_DYE
               contains("Style Soul") -> Material.SOUL_CAMPFIRE
               contains("Ruby Style Shard") -> Material.RED_DYE
               contains("Amber Style Shard") -> Material.ORANGE_DYE
               contains("Citrine Style Shard") -> Material.YELLOW_DYE
               contains("Jade Style Shard") -> Material.LIME_DYE
               contains("Aquamarine Style Shard") -> Material.LIGHT_BLUE_DYE
               contains("Sapphire Style Shard") -> Material.BLUE_DYE
               contains("Amethyst Style Shard") -> Material.MAGENTA_DYE
               contains("Garnet Style Shard") -> Material.PINK_DYE
               contains("Opal Style Shard") -> Material.WHITE_DYE
               contains("Crate") -> Material.BARREL
               contains("Elimination Effect") -> Material.SKULL_BANNER_PATTERN
               contains("Chroma Set") -> Material.PRIZE_POTTERY_SHERD
               contains("Weapon Core") -> Material.HEAVY_CORE
               else -> Material.STRUCTURE_VOID
           }
       }
    }

    private fun getSimpleAssetLore(simpleAssetName: String): List<Component> {
        return with(simpleAssetName) {
            when {
                contains("Style Shard") -> listOf(
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>An extremely rare shard that can be"),
                    Formatting.allTags.deserialize("<!i><aqua>used somewhere on the island..."),
                    Formatting.allTags.deserialize("<!i>")
                )
                contains("30d MCC+ Token") -> listOf(
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>Can be claimed for either <white>+30 days <aqua>of"),
                    Formatting.allTags.deserialize("<!i><aqua>the <white>MCC+ Rank <aqua>or <${ItemRarity.EPIC.colourHex}>400 gems<aqua>."),
                    Formatting.allTags.deserialize("<!i>")
                )
                contains("Style Soul") -> listOf(
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>A strange soul carefully extracted"),
                    Formatting.allTags.deserialize("<!i><aqua>from a <white>Limited <aqua>cosmetic, necessary for"),
                    Formatting.allTags.deserialize("<!i><aqua>crafting or purchasing the rarest of"),
                    Formatting.allTags.deserialize("<!i><aqua>cosmetics and upgrades."),
                    Formatting.allTags.deserialize("<!i>")
                )
                contains("Ultimate Cyber Surge Crate") -> listOf(
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>A crate obtained along with the"),
                    Formatting.allTags.deserialize("<!i><aqua>purchase of the <white>Cyber Surge Ultimate"),
                    Formatting.allTags.deserialize("<!i><white>Battlepass<aqua>."),
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><green>Contains 1 of the following:"),
                    Formatting.allTags.deserialize("<!i><dark_gray>• <${ItemRarity.LEGENDARY.colourHex}>[Spider Goggles Token]"),
                    Formatting.allTags.deserialize("<!i><dark_gray>• <${ItemRarity.LEGENDARY.colourHex}>[Spider Bud Token]"),
                    Formatting.allTags.deserialize("<!i><dark_gray>• <${ItemRarity.LEGENDARY.colourHex}>[Spider Claws Token]"),
                    Formatting.allTags.deserialize("<!i>")
                )
                contains("Limited Sea Monsters Crate (2025)") -> listOf(
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>A crate containing a random limited"),
                    Formatting.allTags.deserialize("<!i><aqua>cosmetic from the <white>2025 Sea Monsters"),
                    Formatting.allTags.deserialize("<!i><white>Event Machine<aqua>."),
                    Formatting.allTags.deserialize("<!i>")
                )
                contains("Limited Halloween Crate (2025)") -> listOf(
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>A crate containing a random limited"),
                    Formatting.allTags.deserialize("<!i><aqua>cosmetic from the <white>2025 Halloween"),
                    Formatting.allTags.deserialize("<!i><white>Event Machine<aqua>."),
                    Formatting.allTags.deserialize("<!i>")
                )
                contains("Elimination Effect") -> listOf(
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>Applies the \"${simpleAssetName.removeSuffix(" Elimination Effect")}\" elimination"),
                    Formatting.allTags.deserialize("<!i><aqua>effect to a selected weapon skin."),
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><gold>Only <yellow>Tier 1<gold> weapon skins and above"),
                    Formatting.allTags.deserialize("<!i><gold>support elimination effects."),
                    Formatting.allTags.deserialize("<!i>")
                )
                contains("Chroma Set") -> listOf(
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>Applies the \"${simpleAssetName.removeSuffix(" Chroma Set")}\" chroma"),
                    Formatting.allTags.deserialize("<!i><aqua>set to a selected weapon skin."),
                    Formatting.allTags.deserialize("<!i>")
                )
                contains("Weapon Core") -> listOf(
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><gray>A fragment of a weapon crate."),
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>Bring to the <white>Weapon Crate Shop<aqua> to"),
                    Formatting.allTags.deserialize("<!i><aqua>craft them into a <white>Weapon Crate<aqua>."),
                    Formatting.allTags.deserialize("<!i>")
                )
                else -> listOf(
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><red><b>Description unknown."),
                    Formatting.allTags.deserialize("<!i><red>Contact an admin if you see this."),
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><red><prefix:warning>This item may be a Weapon Skin"),
                    Formatting.allTags.deserialize("<!i><red>with a tier, which requires an API fix."),
                    Formatting.allTags.deserialize("<!i>")
                )
            }
        }
    }
}

class IslandAPIKeyInterceptor(private val key: String): HttpInterceptor {
    override suspend fun intercept(request: HttpRequest, chain: HttpInterceptorChain): HttpResponse {
        val newRequest = request.newBuilder()
            .addHeader("X-API-Key", key)
            .build()
        return chain.proceed(newRequest)
    }
}

enum class IslandAssetType {
    COSMETIC_TOKEN,
    MCC_PLUS_TOKEN,
    OTHER
}

data class Listings(val item: ItemStack, val startTime: String, val endTime: String, val assetType: IslandAssetType, val rarity: ItemRarity, val cost: Int)