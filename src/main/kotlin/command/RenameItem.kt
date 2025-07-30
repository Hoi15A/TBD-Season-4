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
import util.isHoldingItemInMainHand

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class RenameItem {

    @Command("renameitem <name>")
    @CommandDescription("Renames an item at the cost of 1 lapis.")
    @Permission("tbd.command.renameitem")
    fun renameItem(css: CommandSourceStack, name: Array<String>) {
        val player = css.sender as? Player ?: return

        if (!player.isHoldingItemInMainHand()) {
            player.sendMessage(Formatting.allTags.deserialize("<red>You need to be holding an item to rename."))
            return
        }

        if (!hasLapisInInventory(player) && player.gameMode !== org.bukkit.GameMode.CREATIVE) {
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

    @Command("resetitemname")
    @CommandDescription("Resets the item's name to default.")
    @Permission("tbd.command.resetitemname")
    fun resetItemName(css: CommandSourceStack) {
        val player = css.sender as? Player ?: return

        if (!player.isHoldingItemInMainHand()) {
            player.sendMessage(Formatting.allTags.deserialize("<red>You need to be holding an item to reset its name."))
            return
        }

        val itemMeta = player.inventory.itemInMainHand.itemMeta

        if (!itemMeta.hasDisplayName()) {
            player.sendMessage(Formatting.allTags.deserialize("<yellow>This item doesn't have a custom name."))
            return
        }

        itemMeta.displayName(null) // This resets the name to default
        player.inventory.itemInMainHand.setItemMeta(itemMeta)
        player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>Reset item name!"))
        player.playSound(RENAME_ITEM)
    }

    fun hasLapisInInventory(player: Player): Boolean {
        return player.inventory.contains(LAPIS_LAZULI, 1)
    }
}