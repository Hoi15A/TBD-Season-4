package lib

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

object Sounds {
    val EPIC_CATCH = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.75f, 1.25f)
    val LEGENDARY_CATCH = Sound.sound(Key.key("entity.ender_dragon.death"), Sound.Source.VOICE, 0.5f, 2f)
    val LEGENDARY_CATCH_EXPLODE = Sound.sound(Key.key("entity.generic.explode"), Sound.Source.VOICE, 1f, 1f)
    val MYTHIC_CATCH = Sound.sound(Key.key("block.portal.travel"), Sound.Source.VOICE, 0.75f, 2f)
    val UNREAL_CATCH = Sound.sound(Key.key("ambient.cave"), Sound.Source.VOICE, 10f, 2f)
    val UNREAL_CATCH_SPAWN = Sound.sound(Key.key("entity.warden.sonic_boom"), Sound.Source.VOICE, 2f, 2f)
    val UNREAL_CATCH_SPAWN_BATS = Sound.sound(Key.key("entity.warden.death"), Sound.Source.VOICE, 2f, 1f)
    val SHINY_CATCH = Sound.sound(Key.key("block.amethyst_cluster.step"), Sound.Source.VOICE, 2f, 2f)
    val CAMPFIRE_DISALLOW_FISH_COOK = Sound.sound(Key.key("block.fire.extinguish"), Sound.Source.BLOCK, 1f, 0f)
    val ADMIN_MESSAGE = Sound.sound(Key.key("ui.button.click"), Sound.Source.MASTER, 0.5f, 2f)
    val SERVER_ANNOUNCEMENT = Sound.sound(Key.key("block.note_block.pling"), Sound.Source.MASTER, 1f, 1f)

    val SOUL_RELEASE = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.MASTER, 0.75f, 1f)
    val SOUL_HURT = Sound.sound(Key.key("entity.vex.charge"), Sound.Source.MASTER, 1.5f, 1f)
}