package item.treasurebag

import chat.Formatting.allTags
import io.papermc.paper.datacomponent.item.Equippable
import io.papermc.paper.datacomponent.DataComponentTypes
import item.ItemRarity.*
import item.ItemType
import item.SubRarity
import item.SubRarity.*
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.*
import org.bukkit.NamespacedKey
import org.bukkit.inventory.*

import util.Keys.GENERIC_RARITY
import util.Keys.GENERIC_SUB_RARITY
import util.Keys.TRUE_EYE

/**
 * @param pctChanceToRoll How likely this item is to be rolled as a percentage (Int 0-100)
 * @param amountRange How many of the item should be rolled
 * @param itemStack The actual itemStack of a single bag item
 */
@Suppress("unstableApiUsage")
enum class BagItem(val pctChanceToRoll: Int, val amountRange: IntRange, val itemStack: ItemStack) {
    DRAGON_EGG(100, 1..1,
        ItemStack(Material.DRAGON_EGG)
    ),
    DRAGON_HEAD(50, 1..1,
        ItemStack(Material.DRAGON_HEAD)
    ),
    DRAGON_EYE(100, 1..2,
        ItemStack(Material.ENDER_EYE).apply {
            val dragonEyeMeta = this.itemMeta
            dragonEyeMeta.displayName(allTags.deserialize("<!i><${RARE.colourHex}>Dragon Eye of Ender"))
            val baseLore = mutableListOf(
                allTags.deserialize("<reset><!i><white>${RARE.rarityGlyph}${ItemType.CONSUMABLE.typeGlyph}"),
                allTags.deserialize("<reset><!i><yellow>A slain Ender Dragon's eye.")
            )
            dragonEyeMeta.lore(baseLore)
            dragonEyeMeta.setEnchantmentGlintOverride(true)
            dragonEyeMeta.persistentDataContainer.set(TRUE_EYE, BOOLEAN, true)
            this.itemMeta = dragonEyeMeta
        }
    ),
    DRAGON_ELYTRA(100, 1..1,
        ItemStack(Material.ELYTRA).apply {
            val subRarity = SubRarity.getRandomSubRarity()
            val elytraMeta = this.itemMeta
            elytraMeta.displayName(allTags.deserialize("${if (subRarity == SHADOW) "<#0><shadow:${EPIC.colourHex}>" else "<${EPIC.colourHex}>"}${if (subRarity == OBFUSCATED) "<font:alt>" else ""}${PlainTextComponentSerializer.plainText().serialize(this.effectiveName())}").decoration(TextDecoration.ITALIC, false))
            val elytraLore = mutableListOf<String>()
            elytraLore += "<reset><!i><white>${EPIC.rarityGlyph}${if (subRarity != NONE) subRarity.subRarityGlyph else ""}${ItemType.ARMOUR.typeGlyph}"
            elytraMeta.lore(
                elytraLore.map { allTags.deserialize(it) }
            )
            when(subRarity) {
                SHINY -> {
                    elytraMeta.setEnchantmentGlintOverride(true)
                    elytraMeta.itemModel = NamespacedKey("tbdsmp", "elytra_shiny")
                    val equippable = Equippable.equippable(EquipmentSlot.CHEST).assetId(Key.key("tbdsmp:elytra_shiny")).build()
                    this.setData(DataComponentTypes.EQUIPPABLE, equippable)
                }
                SHADOW -> {
                    elytraMeta.itemModel = NamespacedKey("tbdsmp", "elytra_shadow")
                    val equippable = Equippable.equippable(EquipmentSlot.CHEST).assetId(Key.key("tbdsmp:elytra_shadow")).build()
                    this.setData(DataComponentTypes.EQUIPPABLE, equippable)
                }
                OBFUSCATED -> {
                    elytraMeta.itemModel = NamespacedKey("tbdsmp", "elytra_obf")
                    val equippable = Equippable.equippable(EquipmentSlot.CHEST).assetId(Key.key("tbdsmp:elytra_obf")).build()
                    this.setData(DataComponentTypes.EQUIPPABLE, equippable)
                }
                else -> {}
            }
            elytraMeta.persistentDataContainer.set(GENERIC_RARITY, STRING, EPIC.rarityName.uppercase())
            elytraMeta.persistentDataContainer.set(GENERIC_SUB_RARITY, STRING, subRarity.name.uppercase())
            this.itemMeta = elytraMeta
        }
    ),
    DRAGON_PORTAL_FRAME(1, 1..1,
        ItemStack(Material.END_PORTAL_FRAME).apply {
            val frameMeta = this.itemMeta
            frameMeta.displayName(allTags.deserialize("<!i><${MYTHIC.colourHex}>End Portal Frame"))
            frameMeta.lore(listOf(
                allTags.deserialize("<!i><white>${MYTHIC.rarityGlyph}${ItemType.CONSUMABLE.typeGlyph}"),
                allTags.deserialize("<!i><yellow>A very rare drop from an Ender Dragon."),
            ))
            frameMeta.persistentDataContainer.set(GENERIC_RARITY, STRING, MYTHIC.rarityName.uppercase())
            this.itemMeta = frameMeta
        }
    )
}