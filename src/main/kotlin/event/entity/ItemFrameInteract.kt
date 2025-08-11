package event.entity

import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.ItemFrame
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

object ItemFrameInteract {
    fun itemFrameInteractEvent(event: PlayerInteractEntityEvent) {
        val itemFrame = event.rightClicked as? ItemFrame ?: return
        val player = event.player
        if (!player.isSneaking) return
        when (player.inventory.itemInMainHand.type) {
            Material.AIR -> {
                val newVisibility = !itemFrame.isVisible
                itemFrame.isVisible = newVisibility
                event.isCancelled = true
            }

            Material.HONEYCOMB -> {
                if (itemFrame.isFixed) {
                    // Do nothing if item frame is already fixed
                    return
                }
                itemFrame.isFixed = true
                val amount = player.inventory.itemInMainHand.amount
                val location = itemFrame.location.toBlockLocation().add(0.5, 0.5, 0.5)
                itemFrame.location.world.spawnParticle(Particle.WAX_ON, location, 10, 0.5, 0.5, 0.5)
                player.inventory.itemInMainHand.amount = amount - 1
                event.isCancelled = true
            }

            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE -> {
                if(!itemFrame.isFixed) {
                    // Do nothing if item frame is not fixed
                    return
                }
                itemFrame.isFixed = false
                val location = itemFrame.location.toBlockLocation().add(0.5, 0.5, 0.5)
                itemFrame.location.world.spawnParticle(Particle.WAX_OFF, location, 10, 0.5, 0.5, 0.5)
                val itemMeta = player.inventory.itemInMainHand.itemMeta as Damageable
                val newDamage = itemMeta.damage + 1
                itemMeta.damage = newDamage
                player.inventory.itemInMainHand.itemMeta = itemMeta
                // Break axe if it reaches max damage
                if(itemMeta.damage.toShort() == player.inventory.itemInMainHand.type.maxDurability) {
                    player.inventory.setItemInMainHand(ItemStack(Material.AIR))
                }
                event.isCancelled = true
            }

            else -> {
                // Ignore other interactions for now
            }
        }
    }
}