package util

import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.sound.Sound.sound

object Sounds {
    val EPIC_CATCH = sound(key("entity.wither.spawn"), Source.VOICE, 0.5f, 1.25f)
    val LEGENDARY_CATCH = sound(key("entity.ender_dragon.death"), Source.VOICE, 0.15f, 2f)
    val LEGENDARY_CATCH_EXPLODE = sound(key("entity.generic.explode"), Source.VOICE, 0.5f, 1f)
    val MYTHIC_CATCH = sound(key("block.portal.travel"), Source.VOICE, 0.5f, 2f)
    val UNREAL_CATCH = sound(key("ambient.cave"), Source.VOICE, 10f, 2f)
    val UNREAL_CATCH_SPAWN = sound(key("entity.warden.sonic_boom"), Source.VOICE, 2f, 2f)
    val UNREAL_CATCH_SPAWN_BATS = sound(key("entity.warden.death"), Source.VOICE, 2f, 1f)
    val TRANSCENDENT_CATCH = sound(key("entity.blaze.ambient"), Source.VOICE, 2f, 0.75f)
    val TRANSCENDENT_CATCH_SPAWN = sound(key("entity.elder_guardian.curse"), Source.VOICE, 1.5f, 0.5f)
    val CELESTIAL_CATCH = sound(key("item.totem.use"), Source.VOICE, 2f, 0.75f)
    val CELESTIAL_CATCH_SPAWN = sound(key("item.trident.thunder"), Source.VOICE, 5f, 1.25f)
    val SHINY_CATCH = sound(key("block.amethyst_cluster.step"), Source.VOICE, 2f, 2f)
    val SHADOW_CATCH = sound(key("entity.wither.ambient"), Source.VOICE, 0.5f, 0f)
    val OBFUSCATED_CATCH = sound(key("entity.shulker.ambient"), Source.VOICE, 1.25f, 0.75f)
    val CAMPFIRE_DISALLOW_FISH_COOK = sound(key("block.fire.extinguish"), Source.BLOCK, 1f, 0f)
    val ENDER_EYE_PLACE_FAIL = sound(key("block.end_portal_frame.fill"), Source.BLOCK, 2f, 0.5f)
    val ENDER_EYE_PLACE_FAIL_BACKGROUND = sound(key("entity.lightning_bolt.thunder"), Source.BLOCK, 1f, 2f)
    val TRUE_EYE_SPAWN = sound(key("block.end_portal.spawn"), Source.MASTER, 1f, 0.75f)
    val ADMIN_MESSAGE = sound(key("ui.button.click"), Source.MASTER, 0.5f, 2f)
    val SERVER_ANNOUNCEMENT = sound(key("block.note_block.pling"), Source.MASTER, 1f, 1f)
    val LORE_ANNOUNCEMENT = sound(key("entity.item.pickup"), Source.MASTER, 1f, 1f)
    val RENAME_ITEM = sound(key("block.smithing_table.use"), Source.PLAYER, 1f, 1f)
    val MINT_CURRENCY = sound(key("block.chain.break"), Source.PLAYER, 1f, 1.3f)
    val DIVINIFY_ITEM = sound(key("block.enchantment_table.use"), Source.PLAYER, 1f, 1f)
    val DIVINIFICATION_BLADE_OUT = sound(key("item.armor.equip_netherite"), Source.MASTER, 5f, 0.75f)
    val DIVINIFICATION_BEAM_START = sound(key("block.respawn_anchor.set_spawn"), Source.MASTER, 10f, 1.25f)
    val DIVINIFICATION_CHARGING = sound(key("particle.soul_escape"), Source.MASTER, 5f, 1f)
    val DIVINIFICATION_DEATH = sound(key("entity.breeze.wind_burst"), Source.MASTER, 10f, 0.5f)
    val DIVINIFICATION_BEAM_EXTINGUISH = sound(key("block.fire.extinguish"), Source.MASTER, 10f, 0f)
    val FRAME_EYE_BREAK = sound(key("entity.ender_eye.death"), Source.BLOCK, 1f, 0.75f)
    val PLING = sound(key("minecraft:block.note_block.pling"), Source.PLAYER, 0.5f, 1f)
    val ERROR_DIDGERIDOO = sound(key("minecraft:block.note_block.didgeridoo"), Source.PLAYER, 0.5f, 1f)
    val FISHING_SOCIAL = sound(key("entity.fishing_bobber.splash"), Source.MASTER, 0.5f, 1.5f)
}
