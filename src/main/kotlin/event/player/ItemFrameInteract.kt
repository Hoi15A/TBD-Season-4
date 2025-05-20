package event.player

import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.event.player.PlayerInteractEntityEvent

object ItemFrameInteract {
    fun itemframeInteractEvent(event: PlayerInteractEntityEvent) {
        val itemFrame = event.rightClicked as? ItemFrame ?: return
        if (event.player.isSneaking && event.player.inventory.itemInMainHand.type === Material.AIR) {
            val newVisibility = !itemFrame.isVisible()
            itemFrame.setVisible(newVisibility)
            event.setCancelled(true)
        }
    }
}