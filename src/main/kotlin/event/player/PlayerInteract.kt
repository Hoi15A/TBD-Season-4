package event.player

import event.player.CampfireInteract.campfireInteractEvent
import lore.Divinity
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import util.isHoldingItemInMainHand

class PlayerInteract : Listener {
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        when (event.action) {
            Action.RIGHT_CLICK_BLOCK -> {
                if (event.clickedBlock != null) {
                    campfireInteractEvent(event)
                }
            }
            Action.RIGHT_CLICK_AIR -> {
                if(event.player.isHoldingItemInMainHand()) {
                    if(event.player.inventory.itemInMainHand.itemMeta.itemModel == NamespacedKey("minecraft", "blade")) {
                        if(!event.player.hasCooldown(event.player.inventory.itemInMainHand.type)) {
                            val raycastHitPlayer = raycast(event.player, 7.5, 0.1)
                            if(raycastHitPlayer != null) {
                                if(Divinity.chainedPlayers.containsKey(raycastHitPlayer)) {
                                    event.player.setCooldown(event.player.inventory.itemInMainHand.type, 10 * 20)
                                    Divinity.banishPlayer(event.player, raycastHitPlayer)
                                }

                            }
                        }
                    }
                }
            }
            else -> {
                // DONT CARE/ADD OTHER CASES
            }
        }
    }

    private fun raycast(player: Player, range: Double, raySize: Double): Player? {
        val raycast = player.world.rayTraceEntities(player.eyeLocation, player.location.direction, range, raySize) { entity: Entity -> entity != player }
        return if(raycast != null) {
            if(raycast.hitEntity is Player) {
                raycast.hitEntity as Player
            } else {
                null
            }
        } else {
            null
        }
    }
}