package command

import chat.Formatting
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.GameMode
import org.bukkit.Material.*
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import util.Sounds.RENAME_ITEM
import util.isHoldingItemInMainHand

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class Totemify {

    @Command("totemify")
    @CommandDescription("Turns the held item into a Totem at the cost of 1 Totem.")
    @Permission("tbd.command.totemify")
    fun totemifyItem(css: CommandSourceStack) {
        val player = css.sender as? Player ?: return

        if (!player.isHoldingItemInMainHand() || player.inventory.itemInMainHand.amount != 1){
            player.sendMessage(Formatting.allTags.deserialize("<red>You need to be holding a single item to totemify."))
            return
        }

        if (player.inventory.itemInMainHand.type == TOTEM_OF_UNDYING || isItemTotemified(player.inventory.itemInMainHand)) {
            player.sendMessage(Formatting.allTags.deserialize("<red>You can't totemify something that's already Totemified."))
            return
        }

        if (!hasTotemInInventory(player) && player.gameMode !== GameMode.CREATIVE) {
            player.sendMessage(Formatting.allTags.deserialize("<red>You need 1 Totem of Undying in your inventory to totemify."))
            return
        }

        val slot = player.inventory.first(TOTEM_OF_UNDYING)
        if (player.gameMode !== GameMode.CREATIVE) {
            player.inventory.getItem(slot)?.amount -= 1
        }
        val deathProtection = io.papermc.paper.datacomponent.item.DeathProtection.deathProtection().build()
        val newItem = player.inventory.itemInMainHand
        val itemMeta = newItem.itemMeta
        itemMeta.lore(listOf(
            Formatting.allTags.deserialize("<tbdcolour>☥ Death Protection Active")
        ))
        newItem.itemMeta = itemMeta
        newItem.setData(DataComponentTypes.DEATH_PROTECTION, deathProtection)
        player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>Totemified item!"))
        player.playSound(RENAME_ITEM)
    }

    fun hasTotemInInventory(player: Player): Boolean {
        return player.inventory.contains(TOTEM_OF_UNDYING, 1)
    }

    fun isItemTotemified(item: org.bukkit.inventory.ItemStack): Boolean {
        val deathProtection = item.getData(DataComponentTypes.DEATH_PROTECTION)
        return deathProtection != null
    }
}