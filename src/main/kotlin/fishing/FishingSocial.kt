package fishing

import chat.ChatUtility
import chat.Formatting
import item.ItemRarity
import item.ItemType
import item.SubRarity
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import plugin
import util.Sounds.FISHING_SOCIAL
import util.timeRemainingFormatted
import java.util.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import util.Keys
import util.ui.MemoryFilter

object FishingSocial {
    private val fishingSocialTasks = mutableMapOf<Int, BukkitRunnable>()
    private val fishingSocialScores = mutableMapOf<UUID, Int>()
    private var currentFishingSocialTaskID = 0

    fun startFishingSocial(endTime: Int) {
        if(fishingSocialTasks.isEmpty()) {
            val fishingSocialRunnable = object : BukkitRunnable() {
                var ticks = 0
                var seconds = -15
                var minutes = 0
                override fun run() {
                    if(seconds >= 1) {
                        for(player in Bukkit.getOnlinePlayers()) {
                            player.sendActionBar(Formatting.allTags.deserialize("<gradient:dark_aqua:aqua:dark_aqua><b>FISHING SOCIAL<reset><gray> - <reset>Time Elapsed: ${minutes}m ${seconds}s<gray> - <reset>Event Length: ${endTime.timeRemainingFormatted()}"))
                        }
                    }

                    if(ticks == 0 && seconds == -15 && minutes == 0) {
                        ChatUtility.messageAudience(Audience.audience(Bukkit.getOnlinePlayers()), "<gradient:dark_aqua:aqua:dark_aqua><b>FISHING SOCIAL<reset>: A Fishing Social is starting in <yellow>15s<white>!<newline>• Score points by catching fish.<newline>• <b><green>EVERY</b> catch is worth points, rarer fish net more points.<newline>• The person with the most points when the timer ends, <gradient:gold:yellow:gold>wins<white>!<newline>• <gradient:gold:yellow:gold><b>GOOD LUCK!", false)
                        Bukkit.getServer().playSound(FISHING_SOCIAL)
                    }
                    if(ticks == 1 && seconds == 0 && minutes == 0) {
                        ChatUtility.messageAudience(Audience.audience(Bukkit.getOnlinePlayers()), "<gradient:dark_aqua:aqua:dark_aqua><b>FISHING SOCIAL<reset>: A Fishing Social is now active for <yellow>${endTime.timeRemainingFormatted()}<white>.", false)
                        Bukkit.getServer().playSound(FISHING_SOCIAL)
                    }
                    if(ticks == 1 && seconds == 0 && minutes >= endTime) {
                        ChatUtility.messageAudience(Audience.audience(Bukkit.getOnlinePlayers()), "<gradient:dark_aqua:aqua:dark_aqua><b>FISHING SOCIAL<reset>: The Fishing Social has ended!<newline><newline><gradient:dark_aqua:aqua:dark_aqua><b>FISHING SOCIAL<reset>: Scores are being tallied...<newline>", false)
                        Bukkit.getServer().playSound(FISHING_SOCIAL)
                    }
                    if(ticks == 1 && seconds == 5 && minutes >= endTime) {
                        ChatUtility.messageAudience(Audience.audience(Bukkit.getOnlinePlayers()), "<gradient:dark_aqua:aqua:dark_aqua><b>FISHING SOCIAL<reset>: The winner is...<newline>", false)
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
        var score = fishingSocialScores.getOrDefault(player.uniqueId, 0)
        score += when(fishRarity) {
            FishRarity.COMMON -> 1
            FishRarity.UNCOMMON -> 2
            FishRarity.RARE -> 4
            FishRarity.EPIC -> 6
            FishRarity.LEGENDARY -> 8
            FishRarity.MYTHIC -> 12
            FishRarity.UNREAL -> 20
            FishRarity.SPECIAL -> 0
            FishRarity.TRANSCENDENT -> 50
            FishRarity.CELESTIAL -> 80
        }
        score += when(subRarity) {
            SubRarity.NONE -> 0
            SubRarity.SHINY -> 25
            SubRarity.SHADOW -> 50
            SubRarity.OBFUSCATED -> 75
        }
        fishingSocialScores.remove(player.uniqueId)
        fishingSocialScores[player.uniqueId] = score
    }

    fun scoreBreakdown() {
        var i = 1
        val sortedScores = fishingSocialScores.toList().sortedBy { (_, int) -> int }.reversed().toMap()
        sortedScores.forEach { (uuid, score) ->
            if(i == 1) {
                ChatUtility.messageAudience(Audience.audience(Bukkit.getOnlinePlayers()), "<gradient:dark_aqua:aqua:dark_aqua><b>FISHING SOCIAL<reset>: <b><yellow>${Bukkit.getPlayer(uuid)?.name}!<newline>", false)
                Bukkit.getPlayer(uuid)?.let {
                    giveFishingMemento(it, score)
                    Fishing.mythicEffect(it.location)
                }
                ChatUtility.messageAudience(Audience.audience(Bukkit.getOnlinePlayers()), "<gradient:dark_aqua:aqua:dark_aqua><b>SCORE BREAKDOWN<reset>:", false)
            }
            ChatUtility.messageAudience(Audience.audience(Bukkit.getOnlinePlayers()), "$i. <tbdcolour>${Bukkit.getOfflinePlayer(uuid).name}</tbdcolour> earned <yellow>$score</yellow> point${if(score <= 1) "" else "s"}.", false)
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

    fun giveFishingMemento(player: Player, score: Int) {
        val itemStack = ItemStack(Material.TROPICAL_FISH)
        val itemMeta = itemStack.itemMeta
        val lore = listOf(
            "<!i><white>${ItemRarity.EPIC.rarityGlyph}${ItemType.MEMENTO.typeGlyph}",
            "<!i><yellow>You feel pride running through your veins.",
            "",
            "<!i><gray>Obtained by winning a fishing social event.",
            "<!i><gray>Player: <white>${player.name}",
            "<!i><gray>Score: <white>$score",
        ).map { Formatting.allTags.deserialize(it) }
        itemMeta.lore(lore)
        itemMeta.displayName(Formatting.allTags.deserialize("<!i><rainbow>Pride Fish"))
        itemMeta.persistentDataContainer.set(Keys.MEMENTO_TYPE, PersistentDataType.STRING, "fishing_social")
        itemMeta.setEnchantmentGlintOverride(true)
        itemStack.setItemMeta(itemMeta)
        player.inventory.addItem(itemStack)
        Memory.saveMemory(itemStack, MemoryFilter.SEASON_FOUR)
    }
}