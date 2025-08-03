package command

import chat.ChatUtility
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE
import org.bukkit.Bukkit
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
        val player = css.sender as? Player ?: return
        if (LiveUtil.isLive(player)) {
            LiveUtil.stopLive(player)
            ChatUtility.messageAudience(Audience.audience(Bukkit.getOnlinePlayers()), "<tbdcolour>${player.name} stopped streaming", false)
        } else {
            LiveUtil.startLive(player)
            ChatUtility.messageAudience(Audience.audience(Bukkit.getOnlinePlayers()), "<tbdcolour>${player.name} went live", false)
        }
    }
}

object LiveUtil {
    val livePlayers = mutableSetOf<java.util.UUID>()

    fun isLive(player: Player): Boolean {
        return livePlayers.contains(player.uniqueId)
    }

    fun startLive(player: Player) {
        livePlayers.add(player.uniqueId)
        val newName = player.displayName().color(LIGHT_PURPLE)
        player.displayName(newName)
        player.playerListName(newName)
        player.sendMessage("Live mode enabled.")
    }

    fun stopLive(player: Player) {
        livePlayers.remove(player.uniqueId)
        player.displayName(null)
        player.playerListName(null)
        player.sendMessage("Live mode disabled.")
    }
}