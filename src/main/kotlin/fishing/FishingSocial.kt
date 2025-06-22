package fishing

import chat.Formatting
import item.SubRarity
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import plugin
import util.Sounds.FISHING_SOCIAL
import util.timeRemainingFormatted
import java.util.*

object FishingSocial {
    private val fishingSocialTasks = mutableMapOf<Int, BukkitRunnable>()
    private val fishingSocialScores = mutableMapOf<UUID, Int>()
    private var currentFishingSocialTaskID = 0

    fun startFishingSocial(endTime: Int) {
        if(fishingSocialTasks.isEmpty()) {
            val fishingSocialRunnable = object : BukkitRunnable() {
                var ticks = 0
                var seconds = -10
                var minutes = 0
                override fun run() {
                    for(player in Bukkit.getOnlinePlayers()) {
                        player.sendActionBar(Formatting.allTags.deserialize("<gradient:dark_aqua:aqua:dark_aqua><b>FISHING SOCIAL<reset><gray> - <reset>Time Remaining: ${endTime.timeRemainingFormatted()}<gray> - <reset>Time Elapsed: ${minutes}m ${seconds}s"))
                    }
                    if(ticks == 0 && seconds == -10 && minutes == 0) {
                        Bukkit.broadcast(Formatting.allTags.deserialize("<gradient:dark_aqua:aqua:dark_aqua><b>FISHING SOCIAL<reset>: A Fishing Social is starting in <yellow>10s<white>!<newline>• Score points by catching fish.<newline>• <b><green>EVERY</b> catch is worth points, rarer fish net more points.<newline>• The person with the most points when the timer ends, <gradient:gold:yellow:gold>wins<white>!<newline>• <gradient:gold:yellow:gold><b>GOOD LUCK!"))
                        Bukkit.getServer().playSound(FISHING_SOCIAL)
                    }
                    if(ticks == 1 && seconds == 0 && minutes == 0) {
                        Bukkit.broadcast(Formatting.allTags.deserialize("<gradient:dark_aqua:aqua:dark_aqua><b>FISHING SOCIAL<reset>: A Fishing Social is now active for <yellow>${endTime.timeRemainingFormatted()}<white>."))
                        Bukkit.getServer().playSound(FISHING_SOCIAL)
                    }
                    if(ticks == 1 && seconds == 0 && minutes >= endTime) {
                        Bukkit.broadcast(Formatting.allTags.deserialize("<gradient:dark_aqua:aqua:dark_aqua><b>FISHING SOCIAL<reset>: The Fishing Social has ended!<newline><newline><gradient:dark_aqua:aqua:dark_aqua><b>FISHING SOCIAL<reset>: Scores are being tallied...<newline>"))
                        Bukkit.getServer().playSound(FISHING_SOCIAL)
                    }
                    if(ticks == 1 && seconds == 5 && minutes >= endTime) {
                        Bukkit.broadcast(Formatting.allTags.deserialize("<gradient:dark_aqua:aqua:dark_aqua><b>FISHING SOCIAL<reset>: The winner is...<newline>"))
                        Bukkit.getServer().playSound(FISHING_SOCIAL)
                        scoreBreakdown()
                        reset()
                    }
                    if(ticks >= 20) {
                        ticks = 0
                        seconds++
                    }
                    if(seconds >= 60) {
                        seconds = 0
                        minutes++
                    }
                    ticks++
                }
            }
            fishingSocialRunnable.runTaskTimer(plugin, 0L, 1L)
            currentFishingSocialTaskID = fishingSocialRunnable.taskId
            fishingSocialTasks[fishingSocialRunnable.taskId] = fishingSocialRunnable
        }
    }

    fun stopFishingSocial() {
        reset()
    }

    fun addScore(player: Player, fishRarity: FishRarity, subRarity: SubRarity) {
        fishingSocialScores.putIfAbsent(player.uniqueId, 0)
        var score = 0
        when(fishRarity) {
            FishRarity.COMMON -> score += 1
            FishRarity.UNCOMMON -> score += 2
            FishRarity.RARE -> score += 4
            FishRarity.EPIC -> score += 6
            FishRarity.LEGENDARY -> score += 8
            FishRarity.MYTHIC -> score += 12
            FishRarity.UNREAL -> score += 20
            FishRarity.SPECIAL -> {}
            FishRarity.TRANSCENDENT -> score += 50
            FishRarity.CELESTIAL -> score += 80
        }
        when(subRarity) {
            SubRarity.NONE -> {}
            SubRarity.SHINY -> score += 25
            SubRarity.SHADOW -> score += 50
            SubRarity.OBFUSCATED -> score += 75
        }
        val currentScore = fishingSocialScores[player.uniqueId]!!
        val newScore = currentScore + score
        fishingSocialScores.remove(player.uniqueId)
        fishingSocialScores[player.uniqueId] = newScore
    }

    fun scoreBreakdown() {
        var i = 1
        val sortedScores = fishingSocialScores.toList().sortedBy { (_, int) -> int }.reversed().toMap()
        sortedScores.forEach { (uuid, score) ->
            if(i == 1) {
                Bukkit.broadcast(Formatting.allTags.deserialize("<gradient:dark_aqua:aqua:dark_aqua><b>FISHING SOCIAL<reset>: <b><yellow>${Bukkit.getPlayer(uuid)?.name}!<newline>"))
                Bukkit.getPlayer(uuid)?.let { Fishing.mythicEffect(it.location) }
                Bukkit.broadcast(Formatting.allTags.deserialize("<gradient:dark_aqua:aqua:dark_aqua><b>SCORE BREAKDOWN<reset>:"))
            }
            Bukkit.broadcast(Formatting.allTags.deserialize("$i. <tbdcolour>${Bukkit.getOfflinePlayer(uuid).name}</tbdcolour> earned <yellow>$score</yellow> point${if(score <= 1) "" else "s"}."))
            i++
        }
    }

    fun reset() {
        fishingSocialTasks.forEach { (_, bukkitRunnable) -> bukkitRunnable.cancel()}
        fishingSocialTasks.clear()
        fishingSocialScores.clear()
    }

    fun isActive(): Boolean {
        return fishingSocialTasks.isNotEmpty()
    }
}