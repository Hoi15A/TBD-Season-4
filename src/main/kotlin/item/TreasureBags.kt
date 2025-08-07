package item

import chat.Formatting.allTags
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BundleMeta
import org.bukkit.persistence.PersistentDataType
import util.Keys.GENERIC_RARITY
import util.Keys.GENERIC_SUB_RARITY
import util.Keys.TRUE_EYE

object TreasureBags {
    fun getTreasureBag(treasureBagType: TreasureBagType): ItemStack {
        val treasureBag = ItemStack(treasureBagType.treasureBagMaterial)
        val treasureBagMeta = treasureBag.itemMeta as BundleMeta
        for(item in treasureBagType.treasureBagContents) {
            treasureBagMeta.addItem(item)
        }
        treasureBagMeta.displayName(allTags.deserialize(treasureBagType.treasureBagName))
        treasureBagMeta.lore(listOf(
            allTags.deserialize("<reset><white>${ItemRarity.EPIC.rarityGlyph}${ItemType.CONSUMABLE.typeGlyph}").decoration(TextDecoration.ITALIC, false),
            allTags.deserialize("<reset><yellow>A treasure bag dropped from a boss.").decoration(TextDecoration.ITALIC, false)
        ))
        treasureBag.itemMeta = treasureBagMeta
        return treasureBag
    }
}

enum class TreasureBagType(val treasureBagName: String, val treasureBagMaterial: Material, val treasureBagContents: List<ItemStack>) {
    ENDER_DRAGON("<!i><light_purple>Ender Dragon Treasure Bag", Material.PURPLE_BUNDLE, listOf(
        ItemStack(Material.ELYTRA).apply {
            val subRarity = SubRarity.getRandomSubRarity()
            val elytraMeta = this.itemMeta
            elytraMeta.displayName(allTags.deserialize("${if (subRarity == SubRarity.SHADOW) "<#0><shadow:${ItemRarity.EPIC.colourHex}>" else "<${ItemRarity.EPIC.colourHex}>"}${if (subRarity == SubRarity.OBFUSCATED) "<font:alt>" else ""}${PlainTextComponentSerializer.plainText().serialize(this.effectiveName())}").decoration(TextDecoration.ITALIC, false))
            val elytraLore = mutableListOf<String>()
            elytraLore += "<reset><!i><white>${ItemRarity.EPIC.rarityGlyph}${if (subRarity != item.SubRarity.NONE) subRarity.subRarityGlyph else ""}${ItemType.ARMOUR.typeGlyph}"
            elytraMeta.lore(
                elytraLore.map { allTags.deserialize(it) }
            )
            if(subRarity == SubRarity.SHINY) {
                elytraMeta.setEnchantmentGlintOverride(true)
            }
            elytraMeta.persistentDataContainer.set(GENERIC_RARITY, PersistentDataType.STRING, ItemRarity.EPIC.rarityName.uppercase())
            elytraMeta.persistentDataContainer.set(GENERIC_SUB_RARITY, PersistentDataType.STRING, subRarity.name.uppercase())
            this.itemMeta = elytraMeta
        },
        ItemStack(Material.DRAGON_EGG),
        ItemStack(Material.ENDER_EYE).apply {
            val dragonEyeMeta = this.itemMeta
            dragonEyeMeta.displayName(allTags.deserialize("<${ItemRarity.RARE.colourHex}>Dragon Eye of Ender").decoration(TextDecoration.ITALIC, false))
            val baseLore = mutableListOf(
                allTags.deserialize("<reset><white>${ItemRarity.RARE.rarityGlyph}${ItemType.CONSUMABLE.typeGlyph}").decoration(TextDecoration.ITALIC, false),
                allTags.deserialize("<reset><yellow>A slain Ender Dragon's eye.").decoration(TextDecoration.ITALIC, false)
            )
            dragonEyeMeta.lore(baseLore)
            dragonEyeMeta.setEnchantmentGlintOverride(true)
            dragonEyeMeta.persistentDataContainer.set(TRUE_EYE, PersistentDataType.BOOLEAN, true)
            this.itemMeta = dragonEyeMeta
        }
    )),
    //WITHER("Wither Treasure Bag", Material.BLACK_BUNDLE, listOf()),
    //ELDER_GUARDIAN("Elder Guardian Treasure Bag", Material.CYAN_BUNDLE, listOf()),
    //WARDEN("Warden Treasure Bag", Material.BLUE_BUNDLE, listOf())
}