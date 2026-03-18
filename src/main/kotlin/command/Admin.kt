package command

import chat.ChatUtility

import io.papermc.paper.command.brigadier.CommandSourceStack

import org.bukkit.entity.Player

import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class Admin {
    @Command("ac <text>")
    @CommandDescription("Sends the specified message to admin chat.")
    @Permission("tbd.cmd.admin_chat")
    fun adminChat(css: CommandSourceStack, @Argument("text") text: Array<String>) {
        if(css.sender is Player) {
            val player = css.sender as Player
            ChatUtility.broadcastAdmin("<skull:${player.name}> <dark_red>${player.name}<white>: ${text.joinToString(" ")}", false)
        }
    }

    @Command("dc <text>")
    @CommandDescription("Sends the specified message to dev chat.")
    @Permission("tbd.cmd.dev_chat")
    fun devChat(css: CommandSourceStack, @Argument("text") text: Array<String>) {
        if(css.sender is Player) {
            val player = css.sender as Player
            ChatUtility.broadcastDev("<skull:${player.name}> <gold>${player.name}<white>: ${text.joinToString(" ")}", false)
        }
    }
}