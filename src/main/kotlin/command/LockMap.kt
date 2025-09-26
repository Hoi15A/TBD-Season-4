package command

import chat.Formatting
import event.player.PlayerCraft
import io.papermc.paper.command.brigadier.CommandSourceStack
import util.Sounds.RENAME_ITEM
import org.bukkit.GameMode
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
class LockMap {

    @Command("lockmap")
    @CommandDescription("Makes it that you can't copy the map.")
    @Permission("tbd.command.lockmap")
    fun lockMap(css: CommandSourceStack) {
        val player = css.sender as? Player ?: return

        if (!player.isHoldingItemInMainHand() || player.inventory.itemInMainHand.type != FILLED_MAP) {
            player.sendMessage(Formatting.allTags.deserialize("<red>You need to be holding a filled map to lock."))
            return
        }

        if (!hasWaxInInventory(player) && player.gameMode !== GameMode.CREATIVE) {
            player.sendMessage(Formatting.allTags.deserialize("<red>You need 1 honeycomb in your inventory to lock."))
            return
        }

        if (PlayerCraft.markMapAsUncopyable(player.inventory.itemInHand, player)) {
            player.inventory.removeItem(ItemStack.of(HONEYCOMB))
            player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>Locked Map!"))
            player.playSound(RENAME_ITEM)
        } else {
            player.sendMessage(Formatting.allTags.deserialize("<red>This map is already locked."))
        }
    }

    @Command("unlockmap")
    @CommandDescription("Resets the item's name to default.")
    @Permission("tbd.command.lockmap")
    fun unlockMap(css: CommandSourceStack) {
        val player = css.sender as? Player ?: return

        if (!player.isHoldingItemInMainHand() || player.inventory.itemInMainHand.type != FILLED_MAP) {
            player.sendMessage(Formatting.allTags.deserialize("<red>You need to be holding an item to reset its name."))
            return
        }

        if (PlayerCraft.removeUncopyableFlag(player.inventory.itemInHand, player)) {
            player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>Unlocked Map!"))
            player.playSound(RENAME_ITEM)
        } else {
            player.sendMessage(Formatting.allTags.deserialize("<red>This map is not locked, or you're not the owner."))
        }
    }

    fun hasWaxInInventory(player: Player): Boolean {
        return player.inventory.contains(HONEYCOMB, 1)
    }
}