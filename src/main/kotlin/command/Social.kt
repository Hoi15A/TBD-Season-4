package command

import chat.ChatUtility
import fishing.FishingSocial
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import util.timeRemainingFormatted

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class Social {
    @Command("fishingsocial start <time>")
    @Permission("tbd.command.social")
    fun fishingSocialStart(css: CommandSourceStack, @Argument("time") time: Int) {
        if(time in 1..120) {
            if(css.sender is Player) {
                val player = css.sender as Player
                ChatUtility.broadcastDev("Fishing Social <dark_gray>(${time.timeRemainingFormatted()})</dark_gray> started by ${player.name}.", false)
                FishingSocial.startFishingSocial(time)
            }
        }
    }

    @Command("fishingsocial stop")
    @Permission("tbd.command.social")
    fun fishingSocialStop(css: CommandSourceStack) {
        if(css.sender is Player) {
            val player = css.sender as Player
            ChatUtility.broadcastDev("Fishing Social stopped by ${player.name}.", false)
            FishingSocial.stopFishingSocial()
        }
    }
}
