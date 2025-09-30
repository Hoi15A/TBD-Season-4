package command

import chat.Formatting
import io.papermc.paper.command.brigadier.CommandSourceStack
import lore.MannequinMech
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class SpawnMech {
    @Command("spawnmech spawn")
    @CommandDescription("Spawns a Mech")
    @Permission("tbd.command.spawnmech")
    fun spawnMech(css: CommandSourceStack) {
        val player = css.sender as? Player ?: return
        if (player.name !in listOf("Sebiann", "W40K")) {
            player.sendMessage(Formatting.allTags.deserialize("<red>You do not have permission to use this command."))
            return
        }

        MannequinMech.createMannequinMech(player.location)
        player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>Mannequin spawned"))
    }

    @Command("spawnmech list")
    @CommandDescription("List all active Mechs")
    @Permission("tbd.command.spawnmech")
    fun listMech(css: CommandSourceStack) {
        val player = css.sender as? Player ?: return

        val count = MannequinMech.mannequinPairs.size
        player.sendMessage(Formatting.allTags.deserialize("<yellow>Active mannequins: <tbdcolour>$count"))

        if (count > 0) {
            MannequinMech.mannequinPairs.values.forEachIndexed { index, pair ->
                val loc = pair.mechHost.location
                val name = pair.mechShow.customName() ?: "Unnamed"
                player.sendMessage(
                    Formatting.allTags.deserialize(
                        "<gray>${index + 1}. <white>$name <gray>at <tbdcolour>${loc.blockX}, ${loc.blockY}, ${loc.blockZ}"
                    )
                )
            }
        }
    }

    @Command("spawnmech clear")
    @CommandDescription("Remove all Mechs")
    @Permission("tbd.command.spawnmech")
    fun clearAllMech(css: CommandSourceStack) {
        val player = css.sender as? Player ?: return

        MannequinMech.removeAllMannequins()
        player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>All mannequins cleared!"))
    }
}