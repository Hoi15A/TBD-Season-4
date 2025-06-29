package event.player

import lore.Divinity
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent


class PlayerMovement: Listener {
    @EventHandler
    private fun onMove(e: PlayerMoveEvent) {
        if(Divinity.chainedPlayers.containsKey(e.player)) {
            val to: Location = e.from
            to.pitch = e.to.pitch
            to.yaw = e.to.yaw
            e.to = to
            e.isCancelled = true
        }
    }
}