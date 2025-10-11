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
import net.tbdsmp.tbdseason4.type.CosmeticCategory
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import util.dateTimeDifference
import java.text.NumberFormat

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
                meta.displayName(Formatting.allTags.deserialize("<!i><${rarity.colourHex}>${cosmeticToken.name} Token"))
                meta.lore(listOf(
                    Formatting.allTags.deserialize("<!i><white>${rarity.rarityGlyph}${ItemType.CONSUMABLE.typeGlyph}"),
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>Unlock the \"${cosmeticToken.name}\" ${cosmeticToken.cosmetic.category.name.lowercase()}"),
                    Formatting.allTags.deserialize("<!i><aqua>in your wardrobe."),
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><gray>Remaining Time: <white>${dateTimeDifference(listing.endTime.toString())}"),
                    Formatting.allTags.deserialize("<!i><gray>Listed Price: <#ffff00>\uD83E\uDE99<white>${NumberFormat.getIntegerInstance().format(listing.cost)}"),
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>Join <b><white>play.<#ffff00>mccisland<white>.net<aqua></b> to purchase.")
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
                // Add rarity and type tag
                loreLines.add(Formatting.allTags.deserialize("<!i><white>${rarity.rarityGlyph}${ItemType.CONSUMABLE.typeGlyph}"))
                // Add item description
                for(component in getSimpleAssetLore(simpleAsset.name)) loreLines.add(component)
                // Add auction lines
                val auctionLoreLines = listOf(
                    Formatting.allTags.deserialize("<!i><gray>Remaining Time: <white>${dateTimeDifference(listing.endTime.toString())}"),
                    Formatting.allTags.deserialize("<!i><gray>Listed Price: <#ffff00>\uD83E\uDE99<white>${NumberFormat.getIntegerInstance().format(listing.cost)}"),
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>Join <b><white>play.<#ffff00>mccisland<white>.net<aqua></b> to purchase.")
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

    private fun getCosmeticMaterial(cosmeticCategory: CosmeticCategory): Material {
        return when(cosmeticCategory) {
            CosmeticCategory.HAT -> Material.LEATHER_HELMET
            CosmeticCategory.HAIR -> Material.LEATHER_HELMET
            CosmeticCategory.ACCESSORY -> Material.LEATHER_HORSE_ARMOR
            CosmeticCategory.AURA -> Material.LEATHER_BOOTS
            CosmeticCategory.TRAIL -> Material.LEATHER_LEGGINGS
            CosmeticCategory.CLOAK -> Material.LEATHER_CHESTPLATE
            CosmeticCategory.ROD -> Material.FISHING_ROD
            CosmeticCategory.UNKNOWN__ -> Material.BARRIER
        }
    }

    private fun getSimpleAssetMaterial(simpleAssetName: String): Material {
        return when(simpleAssetName) {
            "30d MCC+ Token" -> Material.PURPLE_DYE
            "Style Soul" -> Material.SOUL_CAMPFIRE
            "Ruby Style Shard" -> Material.RED_DYE
            "Amber Style Shard" -> Material.ORANGE_DYE
            "Citrine Style Shard" -> Material.YELLOW_DYE
            "Jade Style Shard" -> Material.LIME_DYE
            "Aquamarine Style Shard" -> Material.LIGHT_BLUE_DYE
            "Sapphire Style Shard" -> Material.BLUE_DYE
            "Amethyst Style Shard" -> Material.MAGENTA_DYE
            "Garnet Style Shard" -> Material.PINK_DYE
            "Opal Style Shard" -> Material.WHITE_DYE
            "Ultimate Cyber Surge Crate" -> Material.BARREL
            else -> Material.STRUCTURE_VOID
        }
    }

    private fun getSimpleAssetLore(simpleAssetName: String): List<Component> {
        return if(simpleAssetName.contains("Style Shard")) {
            listOf(
                Formatting.allTags.deserialize("<!i>"),
                Formatting.allTags.deserialize("<!i><aqua>An extremely rare shard that can be"),
                Formatting.allTags.deserialize("<!i><aqua>used somewhere on the island..."),
                Formatting.allTags.deserialize("<!i>")
            )
        } else {
            when(simpleAssetName) {
                "30d MCC+ Token" -> listOf(
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>Can be claimed for either <white>+30 days <aqua>of"),
                    Formatting.allTags.deserialize("<!i><aqua>the <white>MCC+ Rank <aqua>or <${ItemRarity.EPIC.colourHex}>400 gems<aqua>."),
                    Formatting.allTags.deserialize("<!i>")
                )
                "Style Soul" -> listOf(
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><aqua>A strange soul carefully extracted"),
                    Formatting.allTags.deserialize("<!i><aqua>from a <white>Limited <aqua>cosmetic, necessary for"),
                    Formatting.allTags.deserialize("<!i><aqua>crafting or purchasing the rarest of"),
                    Formatting.allTags.deserialize("<!i><aqua>cosmetics and upgrades."),
                    Formatting.allTags.deserialize("<!i>")
                )
                "Ultimate Cyber Surge Crate" -> listOf(
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
                else -> listOf(
                    Formatting.allTags.deserialize("<!i>"),
                    Formatting.allTags.deserialize("<!i><red><b>Description unknown."),
                    Formatting.allTags.deserialize("<!i><red>Contact an admin if you see this."),
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