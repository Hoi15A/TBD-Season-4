package event.player

import event.entity.ItemFrameInteract.itemFrameInteractEvent
import event.entity.SnifferInteract.snifferInteractEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

class PlayerInteractEntity : Listener{
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEntityEvent) {
        if (event.hand.equals(org.bukkit.inventory.EquipmentSlot.HAND)) { // The reason for this is really funny
            itemFrameInteractEvent(event)
            snifferInteractEvent(event)
        }
    }
}
