package command

import chat.ChatUtility
import util.Timer
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import util.timeRemainingFormatted

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class Timer {
    @Command("timer start <time>")
    @Permission("tbd.command.timer")
    fun timerStart(css: CommandSourceStack, @Argument("time") time: Int){
        if(time in 1..120) {
            if(css.sender is Player) {
                val player = css.sender as Player
                ChatUtility.broadcastDev("Timer <dark_gray>(${time.timeRemainingFormatted()})</dark_gray> started by ${player.name}.", false)
                Timer.startTimer(time)
            }
        }
    }

    @Command("timer stop")
    @Permission("tbd.command.timer")
    fun timerStop(css: CommandSourceStack){
        if(css.sender is Player) {
            val player = css.sender as Player
            ChatUtility.broadcastDev("Timer stopped by ${player.name}.", false)
            Timer.reset()
        }
    }

}