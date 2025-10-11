package command

import chat.Formatting
import fr.mrmicky.fastboard.adventure.FastBoard
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.UNDERLINED
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Flag
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import plugin
import util.Sounds.ERROR_DIDGERIDOO
import java.util.UUID

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class ShowStat {

    val pageSize = 14
    val secondsPerPage = 10 // TODO: config rewrite, make this accessible in config

    @Command("showstat|sb <stat>")
    @Permission("tbd.command.showstat")
    fun showStat(css: CommandSourceStack,
             stat: Statistic,
             @Flag("material", aliases = ["m"]) material: Material?,
             @Flag("entity", aliases = ["e"]) entityType: EntityType?,
             @Flag("online", aliases = ["o"]) onlineOnly: Boolean = false) {
        val player = css.sender as? Player ?: return

        val players = if (onlineOnly) {
            Bukkit.getOnlinePlayers().map { it as OfflinePlayer }.toTypedArray()
        } else {
            Bukkit.getServer().offlinePlayers
        }

        val sbEntries = mutableListOf<Pair<Component, Int>>()
        when (stat.type) {
            Statistic.Type.UNTYPED -> {
                sbEntries.addAll(players.map { Pair(text(it.name ?: "Unknown"), it.getStatistic(stat)) }.toMutableList())
            }
            Statistic.Type.ITEM, Statistic.Type.BLOCK -> {
                if (material == null) {
                    player.sendMessage(Formatting.allTags.deserialize("<red>Missing material, please specify using the --material flag."))
                    player.playSound(ERROR_DIDGERIDOO)
                    return
                }
                sbEntries.addAll(players.map { Pair(text(it.name ?: "Unknown"), it.getStatistic(stat, material)) }.toMutableList())
            }
            Statistic.Type.ENTITY -> {
                if (entityType == null) {
                    player.sendMessage(Formatting.allTags.deserialize("<red>Missing entity, please specify using the --entity flag."))
                    player.playSound(ERROR_DIDGERIDOO)
                    return
                }
                sbEntries.addAll(players.map { Pair(text(it.name ?: "Unknown"), it.getStatistic(stat, entityType)) }.toMutableList())
            }
        }

        sbEntries.removeIf { it.second == 0 }

        val sum = sbEntries.sumOf { it.second }
        sbEntries.addFirst(Pair(text("Total").color(BLUE).decorate(UNDERLINED), sum))

        val sorted = sbEntries.sortedByDescending { it.second }

        val statScoreboardRunnable = object : BukkitRunnable() {
            var pageIndex = 0
            override fun run() {
                val pages = sorted.chunked(pageSize)
                if (pageIndex <= pages.lastIndex) {
                    val page = pages[pageIndex].toMutableList()
                    val title = text(stat.name).color(RED).append(text(" (${pageIndex+1}/${pages.size})"))
                    broadcastScoreboardLines(title, page)
                    pageIndex++
                } else {
                    clearScoreboards(20L)
                    this.cancel()
                }
            }
        }
        statScoreboardRunnable.runTaskTimer(plugin, 0L, secondsPerPage * 10L)
    }

    private fun broadcastScoreboardLines(title: Component, lines: List<Pair<Component, Int>>) {
        for (player in Bukkit.getOnlinePlayers()) {
            val board = FastBoard(player)
            board.updateTitle(title)
            val names = lines.map { it.first }
            val scores = lines.map { text(it.second).color(RED) }
            board.updateLines(names, scores)
        }
    }

    private fun clearScoreboards(delay: Long) {
        object : BukkitRunnable() {
            override fun run() {
                for (player in Bukkit.getOnlinePlayers()) {
                    val board = FastBoard(player)
                    board.delete()
                }
            }
        }.runTaskLater(plugin, delay)
    }

}
