package event.player

import chat.Formatting
import command.LiveUtil

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

import util.Noxesium

class PlayerQuit: Listener {
    @EventHandler
    private fun onQuit(e: PlayerQuitEvent) {
        Noxesium.removeNoxesiumUser(e.player)
        if(e.player.name in listOf("Byrtrum", "fish_25")) {
            e.quitMessage(null)
        } else {
            e.quitMessage(Formatting.allTags.deserialize("<dark_gray>[<red>-<dark_gray>] <tbdcolour>${e.player.name}<reset> left the game."))
        }

        if(LiveUtil.isLive(e.player)) {
            LiveUtil.onPlayerQuit(e.player)
        }
    }
}