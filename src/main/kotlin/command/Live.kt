package command

import io.papermc.paper.command.brigadier.CommandSourceStack

import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class Live {
    @Command("live|streamermode")
    @CommandDescription("Toggle Streamer mode.")
    @Permission("tbd.command.streamermode")
    fun live(css: CommandSourceStack) {
        val player = css.sender as Player
        if (LiveUtil.livePlayers.containsKey(player)) {
            LiveUtil.livePlayers.remove(player)
            player.sendMessage("Live mode disabled.")
        } else {
            LiveUtil.livePlayers[player] = true
            player.sendMessage("Live mode enabled.")
        }
    }
}
object LiveUtil {
    val livePlayers = mutableMapOf<Player, Boolean>()
}