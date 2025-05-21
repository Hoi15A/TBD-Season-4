package util

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

object Sounds {
    val EPIC_CATCH = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.25f)
    val LEGENDARY_CATCH = Sound.sound(Key.key("entity.ender_dragon.death"), Sound.Source.VOICE, 0.3f, 2f)
    val LEGENDARY_CATCH_EXPLODE = Sound.sound(Key.key("entity.generic.explode"), Sound.Source.VOICE, 1f, 1f)
    val MYTHIC_CATCH = Sound.sound(Key.key("block.portal.travel"), Sound.Source.VOICE, 0.5f, 2f)
    val UNREAL_CATCH = Sound.sound(Key.key("ambient.cave"), Sound.Source.VOICE, 10f, 2f)
    val UNREAL_CATCH_SPAWN = Sound.sound(Key.key("entity.warden.sonic_boom"), Sound.Source.VOICE, 2f, 2f)
    val UNREAL_CATCH_SPAWN_BATS = Sound.sound(Key.key("entity.warden.death"), Sound.Source.VOICE, 2f, 1f)
    val TRANSCENDENT_CATCH = Sound.sound(Key.key("entity.blaze.ambient"), Sound.Source.VOICE, 2f, 0.75f)
    val TRANSCENDENT_CATCH_SPAWN = Sound.sound(Key.key("entity.elder_guardian.curse"), Sound.Source.VOICE, 1f, 0.5f)
    val CELESTIAL_CATCH = Sound.sound(Key.key("item.totem.use"), Sound.Source.VOICE, 2f, 0.75f)
    val CELESTIAL_CATCH_SPAWN = Sound.sound(Key.key("item.trident.thunder"), Sound.Source.VOICE, 3f, 1.25f)
    val SHINY_CATCH = Sound.sound(Key.key("block.amethyst_cluster.step"), Sound.Source.VOICE, 2f, 2f)
    val SHADOW_CATCH = Sound.sound(Key.key("entity.wither.ambient"), Sound.Source.VOICE, 0.5f, 0f)
    val OBFUSCATED_CATCH = Sound.sound(Key.key("entity.shulker.ambient"), Sound.Source.VOICE, 1.25f, 0.75f)
    val CAMPFIRE_DISALLOW_FISH_COOK = Sound.sound(Key.key("block.fire.extinguish"), Sound.Source.BLOCK, 1f, 0f)
    val ENDER_EYE_PLACE_FAIL = Sound.sound(Key.key("block.end_portal_frame.fill"), Sound.Source.BLOCK, 2f, 0.5f)
    val ENDER_EYE_PLACE_FAIL_BACKGROUND = Sound.sound(Key.key("entity.lightning_bolt.thunder"), Sound.Source.BLOCK, 1f, 2f)
    val TRUE_EYE_SPAWN = Sound.sound(Key.key("block.end_portal.spawn"), Sound.Source.MASTER, 1f, 0.75f)
    val ADMIN_MESSAGE = Sound.sound(Key.key("ui.button.click"), Sound.Source.MASTER, 0.5f, 2f)
    val SERVER_ANNOUNCEMENT = Sound.sound(Key.key("block.note_block.pling"), Sound.Source.MASTER, 1f, 1f)
    val LORE_ANNOUNCEMENT = Sound.sound(Key.key("entity.item.pickup"), Sound.Source.MASTER, 1f, 1f)
    val RENAME_ITEM = Sound.sound(Key.key("block.smithing_table.use"), Sound.Source.PLAYER, 1f, 1f)
    val FRAME_EYE_BREAK = Sound.sound(Key.key("entity.ender_eye.death"), Sound.Source.BLOCK, 1f, 0.75f)

}