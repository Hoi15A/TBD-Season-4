package event.player

import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.entity.Sniffer

object SnifferInteract {
    fun snifferInteractEvent(event: PlayerInteractEntityEvent) {
        val sniffer = event.rightClicked as? Sniffer ?: return
        val player = event.player

        if (sniffer.passengers.isNotEmpty()) return
        if (player.vehicle != null) return

        sniffer.addPassenger(player);
    }
}