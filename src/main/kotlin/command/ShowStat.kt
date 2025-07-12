package command

import chat.Formatting
import fr.mrmicky.fastboard.adventure.FastBoard
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Flag
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import util.Sounds.ERROR_DIDGERIDOO
import java.util.UUID

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class ShowStat {

    val boards: Map<UUID, FastBoard> = mapOf()

    @Command("showstat|sb <stat>")
    @Permission("tbd.command.echo")
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

        val sbEntries = mutableListOf<Pair<String, Int>>()
        when (stat.type) {
            Statistic.Type.UNTYPED -> {
                sbEntries.addAll(players.map { Pair(it.name ?: "Unknown", it.getStatistic(stat)) }.toMutableList())
            }
            Statistic.Type.ITEM, Statistic.Type.BLOCK -> {
                if (material == null) {
                    player.sendMessage(Formatting.allTags.deserialize("<red>Missing material, please specify using the --material flag."))
                    player.playSound(ERROR_DIDGERIDOO)
                    return
                }
                sbEntries.addAll(players.map { Pair(it.name ?: "Unknown", it.getStatistic(stat, material)) }.toMutableList())
            }
            Statistic.Type.ENTITY -> {
                if (entityType == null) {
                    player.sendMessage(Formatting.allTags.deserialize("<red>Missing entity, please specify using the --entity flag."))
                    player.playSound(ERROR_DIDGERIDOO)
                    return
                }
                sbEntries.addAll(players.map { Pair(it.name ?: "Unknown", it.getStatistic(stat, entityType)) }.toMutableList())
            }
        }

        sbEntries.removeIf { it.second == 0 }

        val sum = sbEntries.sumOf { it.second }
        sbEntries.addFirst(Pair("Total", sum))

        val sorted = sbEntries.sortedByDescending { it.second }

        for (player in Bukkit.getOnlinePlayers()) {
            val board = FastBoard(player)
            board.updateTitle(text(stat.name).color(NamedTextColor.RED))
            val names = sorted.map { text(it.first) }
            val scores = sorted.map { text(it.second) }
            board.updateLines(names, scores)
        }

        sorted.forEach {
            player.sendMessage(text("${it.first} - ${it.second}"))
        }
    }

}