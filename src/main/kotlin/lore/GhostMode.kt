package lore

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable

import plugin
import util.Keys.TRUE_EYE

import kotlin.math.acos

object GhostMode {
    private val ghostPlayers = mutableSetOf<Player>()
    fun toggleGhostMode(player: Player) {
        if(player in ghostPlayers) {
            ghostPlayers.remove(player)
            player.isInvisible = false
        } else {
            ghostPlayers.add(player)
            ghostModeTask(player)
        }
    }

    private fun ghostModeTask(player: Player) {
        object : BukkitRunnable() {
            override fun run() {
                if(ghostPlayers.contains(player) && player.isOnline) {
                    for (viewer in Bukkit.getOnlinePlayers()) {
                        if (viewer != player) {
                            if(player.world == viewer.world) {
                                if(viewer.inventory.itemInMainHand.persistentDataContainer.get(TRUE_EYE, PersistentDataType.BOOLEAN) == true || viewer.inventory.itemInOffHand.persistentDataContainer.get(TRUE_EYE, PersistentDataType.BOOLEAN) == true) {
                                    viewer.showPlayer(plugin, player)
                                } else {
                                    if(viewer.location.distanceSquared(player.location) <= 10000) {
                                        val isPeripheral = isInPeripheralView(viewer, player)
                                        if(isPeripheral) {
                                            viewer.showPlayer(plugin, player)
                                        } else {
                                            viewer.hidePlayer(plugin, player)
                                        }
                                    } else {
                                        viewer.hidePlayer(plugin, player)
                                    }
                                }
                            }

                        }
                    }
                } else {
                    toggleGhostMode(player)
                    cancel()
                }

            }
        }.runTaskTimer(plugin, 0L, 2L)
    }

    private fun isInPeripheralView(viewer: Player, target: Player): Boolean {
        val direction = viewer.location.direction.normalize()
        val toTarget = target.location.toVector().subtract(viewer.location.toVector()).normalize()
        val dotProduct = direction.dot(toTarget)
        val angle = acos(dotProduct)
        val angleDegrees = Math.toDegrees(angle)
        return angleDegrees in 52.5..62.5
    }
}