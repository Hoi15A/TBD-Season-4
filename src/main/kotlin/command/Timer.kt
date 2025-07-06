package command

import chat.ChatUtility
import chat.Formatting
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import plugin
import util.Sounds.PLING
import util.timeRemainingFormatted

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class Timer {
    @Command("timer start <time> [countdown]")
    @Permission("tbd.command.timer")
    fun timerStart(css: CommandSourceStack, @Argument("time") time: Int, @Argument("countdown") @Default("-1") countdown: Int) {
        if(time in 1..60) {
            if(css.sender is Player) {
                val player = css.sender as Player
                ChatUtility.broadcastDev("Timer <dark_gray>(${time.timeRemainingFormatted()})</dark_gray> started by ${player.name}.", false)
                TimerFunction.startTimer(time, countdown)
            }
        }
    }
}

object TimerFunction {
    fun startTimer(timeInMinutes: Int, countdown: Int) {
            val timerRunnable = object : BukkitRunnable() {
                var countdownTime = countdown
                var timeInSeconds = timeInMinutes * 60
                val timeInSecondsTotal = timeInSeconds
                val timerBossBar = BossBar.bossBar(Component.text("Timer"), 0F, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
                var countdownUpwards = 0

                override fun run() {
                    if(countdownTime >= 0) {
                        timerBossBar.name(Formatting.allTags.deserialize("<b>TIMER<reset><gray> - <reset>Start in: ${countdownTime}s<gray>"))
                        timerBossBar.progress(countdownUpwards.toFloat() / countdown.toFloat())
                        for(player in Bukkit.getOnlinePlayers()) {
                            player.showBossBar(timerBossBar)
                        }
                        countdownTime--
                        countdownUpwards++
                    }
                    if(countdownTime <= -1) {
                        timerBossBar.name(Formatting.allTags.deserialize("<b>TIMER<reset><gray> - <reset>Time: ${timeInSeconds / 60 % 60}m ${timeInSeconds % 60}s<gray>"))
                        timerBossBar.progress(timeInSeconds.toFloat() / timeInSecondsTotal.toFloat())
                        for(player in Bukkit.getOnlinePlayers()) {
                            player.showBossBar(timerBossBar)
                        }
                        timeInSeconds--
                    }
                    if(countdownTime == countdown && countdownTime > -1) {
                        Bukkit.getServer().sendMessage(Formatting.allTags.deserialize( "<b>TIMER<reset>: The time starts in <yellow>${countdown}s<white>!"))
                        Bukkit.getServer().playSound(PLING)
                    }
                    if(countdownTime == -1) {
                        Bukkit.getServer().sendMessage(Formatting.allTags.deserialize( "<b>TIMER<reset>: A timer is now active for <yellow>${timeInMinutes}m<white>."))
                        Bukkit.getServer().playSound(PLING)
                        countdownTime--
                    }
                    if(timeInSeconds == 0) {
                        Bukkit.getServer().sendMessage(Formatting.allTags.deserialize( "<b>TIMER<reset>: The time has ended!"))
                        Bukkit.getServer().playSound(PLING)
                        this.cancel()
                        for(player in Bukkit.getOnlinePlayers()) {
                            player.hideBossBar(timerBossBar)
                        }
                    }
                }
            }
            timerRunnable.runTaskTimer(plugin, 0L, 20L)
    }
}