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
    val FRAME_EYE_BREAK = sound(key("entity.ender_eye.death"), Source.BLOCK, 1f, 0.75f)
    val FRAME_BREAK = sound(key("block.glass.break"), Source.BLOCK, 1f, 0.75f)
    val PLING = sound(key("minecraft:block.note_block.pling"), Source.PLAYER, 0.5f, 1f)
    val ERROR_DIDGERIDOO = sound(key("minecraft:block.note_block.didgeridoo"), Source.PLAYER, 0.5f, 1f)
    val FISHING_SOCIAL = sound(key("entity.fishing_bobber.splash"), Source.MASTER, 0.5f, 1.5f)
    val INTERFACE_INTERACT = sound(key("block.vault.insert_item"), Source.MASTER, 1f, 1f)
    val INTERFACE_ENTER_SUB_MENU = sound(key("block.vault.activate"), Source.MASTER, 1f, 1f)
    val INTERFACE_BACK = sound(key("block.vault.deactivate"), Source.MASTER, 1f, 1f)
    val INTERFACE_ERROR = sound(key("block.vault.reject_rewarded_player"), Source.MASTER, 1f, 1f)
    val MEMORY_SAVE = sound(key("entity.evoker.cast_spell"), Source.MASTER, 1f, 1f)
    val MEMORY_ALREADY_EXISTS = sound(key("entity.allay.ambient_with_item"), Source.MASTER, 1f, 1f)
    val MEMORY_INVALID = sound(key("entity.allay.ambient_without_item"), Source.MASTER, 1f, 1f)
}