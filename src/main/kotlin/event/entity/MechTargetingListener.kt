package event.entity

import lore.MannequinMech
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.entity.Skeleton as MechHost
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTargetEvent
import plugin

// Delete file after Senate Meeting
class MechTargetingListener : Listener {

    @EventHandler
    fun onEntityTarget(event: EntityTargetEvent) {
        val entity = event.entity
        val target = event.target as? Player ?: return

        // Check if this is one of our mechs
        val mechPair = MannequinMech.mannequinPairs.values.find { it.mechHost == entity }
            ?: return

        // Check if target should be ignored
        if (target.name == "Sebiann" || target.name == "W40K" || target.name == "Wosqe") {
            event.isCancelled = true

            // Find alternative target
            findAlternativeTarget(mechPair.mechHost)
        }
    }

    private fun findAlternativeTarget(mechHost: MechHost) {
        val nearbyPlayers = mechHost.location.world.players
            .filter { it.gameMode == GameMode.SURVIVAL }
            .filter { it.location.distance(mechHost.location) <= 100.0 }
            .filter { it.name != "Sebiann" && it.name != "W40K" && it.name != "Wosqe" }
            .minByOrNull { it.location.distance(mechHost.location) }

        mechHost.target = nearbyPlayers
    }
}