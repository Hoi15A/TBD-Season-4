package event.player

import event.player.CampfireInteract.campfireInteractEvent
import event.player.ItemFrameInteract.itemframeInteractEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

class PlayerInteract : Listener {
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        when (event.action) {
            Action.RIGHT_CLICK_BLOCK -> {
                if (event.clickedBlock != null) {
                    campfireInteractEvent(event)
                }
            }
            else -> {
                // DONT CARE/ADD OTHER CASES
            }
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEntityEvent) {
        if (!event.hand.equals(org.bukkit.inventory.EquipmentSlot.HAND)) { // The reason for this is really funny
            itemframeInteractEvent(event)
        }
    }
}