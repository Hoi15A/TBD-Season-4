package command

import io.papermc.paper.command.brigadier.CommandSourceStack

import lore.GhostMode

import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class Ghost {
    @Command("ghost")
    @Permission("tbd.command.ghost")
    fun ghost(css: CommandSourceStack) {
        if(css.sender is Player) {
            val player = css.sender as Player
            GhostMode.toggleGhostMode(player)
        }
    }
}