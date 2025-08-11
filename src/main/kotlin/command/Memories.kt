package command

import Memory
import chat.Formatting
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Material
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import util.Keys.DIVINITY_CHAINS
import util.Keys.MEMENTO_TYPE
import util.Keys.TRUE_EYE
import util.Sounds
import util.ui.TBDInterface
import util.ui.MemoryFilter
import util.ui.TBDInterfaceType

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class Memories {
    @Command("memory view")
    fun memoryView(css: CommandSourceStack) {
        if(css.sender is Player) {
            val player = css.sender as Player
            TBDInterface(player, TBDInterfaceType.MEMORY_ARCHIVE)
        }
    }

    @Command("memory save")
    fun memoryAdd(css: CommandSourceStack) {
        if(css.sender is Player) {
            val player = css.sender as Player
            saveMemory(player, MemoryFilter.SEASON_FOUR)
        }
    }

    @Command("memory save <filter>")
    @Permission("tbd.command.memory")
    fun memoryAdd(css: CommandSourceStack, @Argument("filter") filter: MemoryFilter) {
        if(css.sender is Player) {
            val player = css.sender as Player
            saveMemory(player, filter)
        }
    }

    private fun saveMemory(player: Player, filter: MemoryFilter) {
        if(player.hasPermission("tbd.command.memory")
            || player.inventory.itemInMainHand.persistentDataContainer.has(TRUE_EYE)
            || player.inventory.itemInMainHand.persistentDataContainer.has(DIVINITY_CHAINS)
            || player.inventory.itemInMainHand.persistentDataContainer.has(MEMENTO_TYPE)) {
            if(player.inventory.itemInMainHand.type != Material.AIR) {
                val memory = player.inventory.itemInMainHand
                if(Memory.getMemories(filter).contains(memory.asOne())) {
                    player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>✨</tbdcolour> This memory <red>already exists</red>."))
                    player.playSound(Sounds.MEMORY_ALREADY_EXISTS)
                } else {
                    Memory.saveMemory(memory.asOne(), filter)
                    player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>✨</tbdcolour> ").append(memory.effectiveName().hoverEvent(memory)).append(Formatting.allTags.deserialize(" <reset>has been <green>memorised</green>.")))
                    player.playSound(Sounds.MEMORY_SAVE)
                }
            } else {
                player.sendMessage(Formatting.allTags.deserialize("<red><prefix:warning></red> <#f26427>You are not even holding an item, you numpty."))
                player.playSound(Sounds.MEMORY_INVALID)
            }
        } else {
            if(player.inventory.itemInMainHand.type == Material.AIR) {
                player.sendMessage(Formatting.allTags.deserialize("<red><prefix:warning></red> <#f26427>You are not even holding an item, you numpty."))
                player.playSound(Sounds.MEMORY_INVALID)
            } else {
                player.sendMessage(Formatting.allTags.deserialize("<red><prefix:warning></red> <#f26427>This item cannot be added as a memory."))
                player.playSound(Sounds.MEMORY_INVALID)
            }

        }
    }
}