package command

import chat.Formatting
import io.papermc.paper.command.brigadier.CommandSourceStack
import util.Sounds.RENAME_ITEM
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText

import org.bukkit.Material.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class RenameItem {

    @Command("renameitem <name>")
    @CommandDescription("Renames an item at the cost of 1 lapis.")
    @Permission("tbd.command.renameitem")
    fun renameItem(css: CommandSourceStack, name: Array<String>) {
        val player = css.sender as? Player ?: run { return }

        if (!isHoldingItem(player)) {
            player.sendMessage(Formatting.allTags.deserialize("<red>You need to be holding an item to rename."))
            return
        }

        if (!hasLapisInInventory(player)) {
            player.sendMessage(Formatting.allTags.deserialize("<red>You need 1 lapis in your inventory to rename."))
            return
        }
        val nameComponent = Formatting.restrictedNoSkullTags.deserialize(name.joinToString(" "))

        if (plainText().serialize(nameComponent).length > 50) {
            player.sendMessage(Formatting.allTags.deserialize("<red>The maximum name length is 50 characters."))
            return
        }

        player.inventory.removeItem(ItemStack.of(LAPIS_LAZULI))
        val itemMeta = player.inventory.itemInMainHand.itemMeta
        itemMeta.displayName(nameComponent)
        player.inventory.itemInMainHand.setItemMeta(itemMeta)
        player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>Renamed item!"))
        player.playSound(RENAME_ITEM)
    }

    fun isHoldingItem(player: Player): Boolean {
        return player.inventory.itemInMainHand.type != AIR
    }

    fun hasLapisInInventory(player: Player): Boolean {
        return player.inventory.contains(LAPIS_LAZULI, 1)
    }
}