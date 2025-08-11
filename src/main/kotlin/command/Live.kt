package command

import chat.Formatting
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import plugin
import java.util.UUID

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
            Bukkit.getServer().sendMessage(Formatting.allTags.deserialize("<tbdcolour>${player.name}</tbdcolour> stopped streaming"))
        } else {
            LiveUtil.startLive(player)
            Bukkit.getServer().sendMessage(Formatting.allTags.deserialize("<tbdcolour>${player.name}</tbdcolour> went live"))
        }
    }
}

object LiveUtil {
    val livePlayers = mutableSetOf<UUID>()
    val liveTasks = mutableMapOf<UUID, BukkitRunnable>()

    fun isLive(player: Player): Boolean {
        return livePlayers.contains(player.uniqueId)
    }

    fun startLive(player: Player) {
        livePlayers.add(player.uniqueId)
        player.sendMessage("Live mode enabled.")
        val timerRunnable = object : BukkitRunnable() {
            override fun run() {
                player.displayName(null)
                player.playerListName(null)
                val newName = Formatting.allTags.deserialize("\uF017 ")
                    .append(player.displayName().color(TextColor.color(255, 156, 237)))
                player.displayName(newName)
                player.playerListName(newName)
            }
        }
        timerRunnable.runTaskTimer(plugin, 0L, 20L)
        liveTasks[player.uniqueId] = timerRunnable
    }

    fun stopLive(player: Player) {
        livePlayers.remove(player.uniqueId)
        liveTasks.remove(player.uniqueId)?.cancel()
        player.displayName(null)
        player.playerListName(null)
        player.sendMessage("Live mode disabled.")
    }
}