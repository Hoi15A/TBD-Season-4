package command

import chat.Formatting.allTags
import fr.mrmicky.fastboard.adventure.FastBoard
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Statistic
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Flag
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import plugin
import util.Sounds.ERROR_DIDGERIDOO
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


@Suppress("unused", "unstableApiUsage")
@CommandContainer
class ShowStat {

    val pageSize = 14
    val secondsPerPage = 7 // TODO: config rewrite, make this accessible in config
    var isActive = false

    @Command("showstat|sb <stat>")
    @Permission("tbd.command.showstat")
    fun showStat(css: CommandSourceStack,
             stat: Statistic,
             @Flag("material", aliases = ["m"]) material: Material?,
             @Flag("entity", aliases = ["e"]) entityType: EntityType?,
             @Flag("online", aliases = ["o"]) onlineOnly: Boolean = false) {
        val player = css.sender as? Player ?: return

        if (isActive) {
            player.sendMessage(allTags.deserialize("<red>Other stats are already being shown, please wait for them to finish."))
            player.playSound(ERROR_DIDGERIDOO)
            return
        }

        val players = if (onlineOnly) {
            Bukkit.getOnlinePlayers().map { it as OfflinePlayer }.toTypedArray()
        } else {
            Bukkit.getServer().offlinePlayers
        }

        val sbEntries = mutableListOf<Pair<Component, Int>>()
        when (stat.type) {
            Statistic.Type.UNTYPED -> {
                sbEntries.addAll(players.map { Pair(formatPlayerName(it), it.getStatistic(stat)) }.toMutableList())
            }
            Statistic.Type.ITEM, Statistic.Type.BLOCK -> {
                if (material == null) {
                    player.sendMessage(allTags.deserialize("<red>Missing material, please specify using the --material flag."))
                    player.playSound(ERROR_DIDGERIDOO)
                    return
                }
                sbEntries.addAll(players.map { Pair(formatPlayerName(it), it.getStatistic(stat, material)) }.toMutableList())
            }
            Statistic.Type.ENTITY -> {
                if (entityType == null) {
                    player.sendMessage(allTags.deserialize("<red>Missing entity, please specify using the --entity flag."))
                    player.playSound(ERROR_DIDGERIDOO)
                    return
                }
                sbEntries.addAll(players.map { Pair(formatPlayerName(it), it.getStatistic(stat, entityType)) }.toMutableList())
            }
        }

        sbEntries.removeIf { it.second == 0 }

        val sum = sbEntries.sumOf { it.second }
        sbEntries.addFirst(Pair(allTags.deserialize("<shadow:black><#ff65aa><u>Total"), sum))

        val sorted = sbEntries.sortedByDescending { it.second }

        isActive = true
        val statScoreboardRunnable = object : BukkitRunnable() {
            var pageIndex = 0
            override fun run() {
                val pages = sorted.chunked(pageSize)
                if (pageIndex <= pages.lastIndex) {
                    val page = pages[pageIndex].toMutableList()

                    val title = allTags.deserialize("<shadow:black><gradient:#FDCFFA:#D78FEE>${snakeCaseToSpaced(stat.name)}${
                        when (stat.type) {
                            Statistic.Type.ITEM, Statistic.Type.BLOCK -> " <gradient:#9B5DE0:#4E56C0>(${snakeCaseToSpaced(material!!.name)})"
                            Statistic.Type.ENTITY -> " <gradient:#9B5DE0:#4E56C0>(${snakeCaseToSpaced(entityType!!.name)})"
                            else -> ""
                        }
                    } <#4E56C0>[<#FDCFFA>${pageIndex+1}/${pages.size}<#4E56C0>]")

                    broadcastScoreboardLines(title, page)
                    pageIndex++
                } else {
                    clearScoreboards(20L)
                    isActive = false
                    this.cancel()
                }
            }
        }
        statScoreboardRunnable.runTaskTimer(plugin, 0L, secondsPerPage * 20L)
    }

    private fun broadcastScoreboardLines(title: Component, lines: List<Pair<Component, Int>>) {
        for (player in Bukkit.getOnlinePlayers()) {
            val board = FastBoard(player)
            board.updateTitle(title)
            val names = lines.map { it.first }
            val scores = lines.map { formatInteger(it.second) }
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

    private fun formatPlayerName(offlinePlayer: OfflinePlayer): Component {
        if (offlinePlayer.name == null) return text("Unknown")

        return if (offlinePlayer.isOnline) {
            allTags.deserialize("<tbdcolour><shadow:black>${offlinePlayer.name}")
        } else {
            allTags.deserialize("<white><shadow:black>${offlinePlayer.name}")
        }
    }

    private fun formatInteger(number: Int): Component {
        val symbols = DecimalFormatSymbols(Locale.forLanguageTag("de-CH"))
        symbols.groupingSeparator = '\''
        val formatter = DecimalFormat("#,##0", symbols)
        return allTags.deserialize("<red><shadow:black>${formatter.format(number)}")
    }

    private fun snakeCaseToSpaced(snakeCaseStr: String): String {
        return snakeCaseStr.split("_").joinToString(" ") { str ->
            str.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
