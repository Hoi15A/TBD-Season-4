package event.block

import chat.Formatting.allTags
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.persistence.PersistentDataType
import util.Keys
import util.Sounds.ERROR_DIDGERIDOO
import util.secondsToTicks

class BlockPlace : Listener {

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val isUnplaceable = event.itemInHand.persistentDataContainer.getOrDefault(Keys.ITEM_IS_UNPLACEABLE, PersistentDataType.BOOLEAN, false)
        if (isUnplaceable) {
            event.player.setCooldown(event.itemInHand, 5.secondsToTicks())
            event.player.sendMessage(allTags.deserialize("<i><gradient:#EA7300:#A62C2C>A mysterious force prevents you from placing the ").append(event.itemInHand.effectiveName()))
            event.player.playSound(ERROR_DIDGERIDOO)
            event.isCancelled = true
        }
    }
}
