package util

import chat.Formatting
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import plugin
import util.Sounds.PLING
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object Timer {
    private val timerTasks = mutableMapOf<Int, BukkitRunnable>()
    private var currentTimerID = 0

    fun startTimer(totalTime: Int) {
        if(timerTasks.isEmpty()) {
            val timerRunnable = object : BukkitRunnable() {
                var seconds = 15
                var minutes = totalTime
                override fun run() {
                    if(minutes == totalTime && seconds >= 1) {
                        for(player in Bukkit.getOnlinePlayers()) {
                            player.sendActionBar(Formatting.allTags.deserialize("<b>TIMER<reset><gray> - <reset>Start in: ${seconds}s<gray>"))
                        }
                    }
                    if(minutes != totalTime || minutes == totalTime && seconds == 0) {
                        for(player in Bukkit.getOnlinePlayers()) {
                            player.sendActionBar(Formatting.allTags.deserialize("<b>TIMER<reset><gray> - <reset>Time: ${minutes}m ${seconds}s<gray>"))
                        }
                    }
                    if(seconds == 15 && minutes == totalTime) {
                        Bukkit.getServer().sendMessage(Formatting.allTags.deserialize( "<b>TIMER<reset>: The time starts in <yellow>${seconds}s<white>!"))
                        Bukkit.getServer().playSound(PLING)
                    }
                    if(seconds == 0 && minutes == totalTime) {
                        Bukkit.getServer().sendMessage(Formatting.allTags.deserialize( "<b>TIMER<reset>: A timer is now active for <yellow>${totalTime.timeRemainingFormatted()}<white>."))
                        Bukkit.getServer().playSound(PLING)
                    }
                    if(seconds == 0 && minutes == 0) {
                        Bukkit.getServer().sendMessage(Formatting.allTags.deserialize( "<b>TIMER<reset>: The time has ended!"))
                        Bukkit.getServer().playSound(PLING)
                        reset()
                    }
                    if(seconds <= 0) {
                        seconds = 60
                        minutes--
                    }
                    seconds--
                }
            }
            timerRunnable.runTaskTimer(plugin, 0L, 20L)
            currentTimerID = timerRunnable.taskId
            timerTasks[timerRunnable.taskId] = timerRunnable

        }
    }
    fun reset() {
        timerTasks.forEach { (_, bukkitRunnable) -> bukkitRunnable.cancel()}
        timerTasks.clear()
    }
}