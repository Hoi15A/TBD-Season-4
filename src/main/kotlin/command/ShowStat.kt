package command

import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Flag
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class ShowStat {

    @Command("showstat <stat>")
    @Permission("tbd.command.echo")
    fun showStat(css: CommandSourceStack,
             stat: Statistic,
             @Flag("material", aliases = ["m"]) material: Material?,
             @Flag("entity", aliases = ["e"]) entityType: EntityType?) {
        val player = css.sender as? Player ?: return

        when (stat.type) {
            Statistic.Type.UNTYPED -> {
                //player.sendMessage(Component.text("stat: $stat value: ${player.getStatistic(stat)}"))
            }
            Statistic.Type.ITEM -> {
            }
            Statistic.Type.BLOCK -> {
            }
            Statistic.Type.ENTITY -> {
            }
        }
        //player.sendMessage(Component.text("$stat, $material, $entityType"))

        val lbPlayers = Bukkit.getServer().offlinePlayers
            .map { Pair(it.name, it.getStatistic(stat)) }.toMutableList()
        val sum = lbPlayers.sumOf { it.second }
        lbPlayers.addFirst(Pair("Total", sum))

        val res = lbPlayers.sortedByDescending { it.second }
        res.forEach {
            player.sendMessage(Component.text("${it.first} - ${it.second}"))
        }
    }

}