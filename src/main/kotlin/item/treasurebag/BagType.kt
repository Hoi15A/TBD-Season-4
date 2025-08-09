package item.treasurebag

import chat.Formatting.allTags
import item.ItemRarity
import item.ItemType
import item.treasurebag.BagLootPool.ENDER_DRAGON
import net.kyori.adventure.text.Component
import org.bukkit.Material

/**
 * A type of treasure bag
 * @param displayName The name of the item that should be given to the bag
 * @param loreLines The lore that should be attached to the bag
 * @param bagMaterial The type of bag (MUST BE A BUNDLE)
 * @param lootPool The loot pool to pull from when generating bag contents
 */
enum class BagType(val displayName: Component, val loreLines: List<Component>, val bagMaterial: Material, val lootPool: BagLootPool) {
    DRAGON_SCALE(
        allTags.deserialize("<!i><light_purple>Dragon Scale Treasure Bag"),
        listOf(
            allTags.deserialize("<reset><!i><white>${ItemRarity.EPIC.rarityGlyph}${ItemType.CONSUMABLE.typeGlyph}"),
            allTags.deserialize("<reset><!i><yellow>A treasure bag dropped from a boss.")
        ),
        Material.PURPLE_BUNDLE,
        ENDER_DRAGON
    )
}
