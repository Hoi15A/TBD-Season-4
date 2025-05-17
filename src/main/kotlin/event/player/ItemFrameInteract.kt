package event.player

import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemFrame
import org.bukkit.event.player.PlayerInteractEntityEvent

object ItemFrameInteract {

    fun itemframeInteractEvent(event: PlayerInteractEntityEvent) {
        val entity: Entity? = event.rightClicked

        if (entity is ItemFrame) {
            val itemFrame = entity

            if (event.getPlayer().isSneaking() && event.getPlayer().getInventory().getItemInMainHand().getType() === Material.AIR) {
                val newVisibility = !itemFrame.isVisible()
                itemFrame.setVisible(newVisibility)
                event.setCancelled(true)
            }
        }
    }
}