package util

import org.bukkit.NamespacedKey
import plugin

object Keys {
    val FISH_RARITY = NamespacedKey(plugin, "fish.rarity")
    val FISH_IS_SHINY = NamespacedKey(plugin, "fish.is_shiny")
    val FISH_IS_SHADOW = NamespacedKey(plugin, "fish.is_shadow")
    val FISH_IS_OBFUSCATED = NamespacedKey(plugin, "fish.is_obfuscated")

    val TRUE_EYE = NamespacedKey(plugin, "eye_of_ender.true_eye")
    val END_PORTAL_FRAMES_WITH_EYE = NamespacedKey(plugin, "end_portal_frames.with_true_eye")

    val CURRENCY_HASH = NamespacedKey(plugin, "currency.sha256")

    val PDC_LOCATION_WORLD = NamespacedKey(plugin, "pdc.type.location_world")!!
    val PDC_LOCATION_X = NamespacedKey(plugin, "pdc.type.location_x")!!
    val PDC_LOCATION_Y = NamespacedKey(plugin, "pdc.type.location_y")!!
    val PDC_LOCATION_Z = NamespacedKey(plugin, "pdc.type.location_z")!!

    val MEMENTO_TYPE = NamespacedKey(plugin, "pdc.type.memento_type")!!
}
