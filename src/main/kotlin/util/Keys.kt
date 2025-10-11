package util

import org.bukkit.NamespacedKey
import plugin

object Keys {
    /**
     * General / Utility
     */
    val CURRENCY_HASH = NamespacedKey(plugin, "currency.sha256")
    val ITEM_IS_UNPLACEABLE = NamespacedKey(plugin, "item.unplaceable")
    val MEMENTO_TYPE = NamespacedKey(plugin, "item.memento_type")
    val GENERIC_RARITY = NamespacedKey(plugin, "item.rarity")
    val GENERIC_SUB_RARITY = NamespacedKey(plugin, "item.rarity.sub_rarity")
    val PLAYER_PLACED_END_PORTAL_FRAMES = NamespacedKey(plugin, "end_portal_frames.placed_by_player")
    val TBD_PLUS_ITEM = NamespacedKey(plugin, "item.tbd_plus")
    val NOXESIUM_IMMOVABLE = NamespacedKey("noxesium", "immovable")

    /**
     * Lore
     */
    val TRUE_EYE = NamespacedKey(plugin, "eye_of_ender.true_eye")
    val END_PORTAL_FRAMES_WITH_EYE = NamespacedKey(plugin, "end_portal_frames.with_true_eye")
    val DIVINITY_CHAINS = NamespacedKey(plugin, "divinity.can_chain")

    /**
     * Fishing related
     */
    val FISH_RARITY = NamespacedKey(plugin, "fish.rarity")
    val FISH_IS_SHINY = NamespacedKey(plugin, "fish.is_shiny")
    val FISH_IS_SHADOW = NamespacedKey(plugin, "fish.is_shadow")
    val FISH_IS_OBFUSCATED = NamespacedKey(plugin, "fish.is_obfuscated")

    /**
     * Used for custom PDC types when you want to serialise more complicated objects into pdc
     */
    val PDC_LOCATION_WORLD = NamespacedKey(plugin, "pdc.type.location_world")!!
    val PDC_LOCATION_X = NamespacedKey(plugin, "pdc.type.location_x")!!
    val PDC_LOCATION_Y = NamespacedKey(plugin, "pdc.type.location_y")!!
    val PDC_LOCATION_Z = NamespacedKey(plugin, "pdc.type.location_z")!!
}
