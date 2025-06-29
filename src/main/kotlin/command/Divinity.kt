package command

import chat.Formatting
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import util.Keys.DIVINITY_CHAINS
import util.Sounds.DIVINIFY_ITEM
import util.isHoldingItemInMainHand

@CommandContainer
@Suppress("unused", "unstableApiUsage")
class Divinity {
    @Command("divinity enchant")
    @Permission("tbd.command.divinify")
    fun divinify(css: CommandSourceStack) {
        if(css.sender is Player) {
            val player = css.sender as Player
            if(player.isHoldingItemInMainHand()) {
                val itemMeta = player.inventory.itemInMainHand.itemMeta
                val lore = itemMeta.lore() ?: mutableListOf()
                lore.add(Formatting.allTags.deserialize(DIVINE_LORE))
                itemMeta.lore(lore)
                itemMeta.persistentDataContainer.set(DIVINITY_CHAINS, PersistentDataType.BOOLEAN, true)
                player.inventory.itemInMainHand.setItemMeta(itemMeta)
                player.playSound(DIVINIFY_ITEM)
            }
        }
    }

    @Command("divinity disenchant")
    @Permission("tbd.command.dedivinify")
    fun dedivinify(css: CommandSourceStack) {
        if(css.sender is Player) {
            val player = css.sender as Player
            if(player.isHoldingItemInMainHand()) {
                val itemMeta = player.inventory.itemInMainHand.itemMeta
                val lore = itemMeta.lore()!!.filterNot { plainText().serialize(it).contains(DIVINE_EMOJI) }
                itemMeta.lore(lore)
                itemMeta.persistentDataContainer.remove(DIVINITY_CHAINS)
                player.inventory.itemInMainHand.setItemMeta(itemMeta)
                player.playSound(DIVINIFY_ITEM)
            }
        }
    }

    companion object {
        private const val DIVINE_EMOJI = "❁"
        private const val DIVINE_LORE = "<i:false><#bdab09>${DIVINE_EMOJI} <gradient:#ccba1b:#cfbb0a>[ <font:illageralt>DIVINE</font> ]"
    }
}