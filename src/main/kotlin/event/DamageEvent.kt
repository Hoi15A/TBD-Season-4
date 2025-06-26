package event

import lore.Divinity
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.persistence.PersistentDataType
import util.Keys.DIVINITY_CHAINS

class DamageEvent: Listener {
    @EventHandler
    private fun onFishingFireworkDamage(e: EntityDamageByEntityEvent) {
        if(e.damager is Firework) {
            if(e.damager.scoreboardTags.contains("tbd.firework")) {
                e.isCancelled = true
            }
        }
        if(e.entity is Player && e.damager is Player) {
            val player = e.entity as Player
            val damager = e.damager as Player
            if(Divinity.chainedPlayers.containsKey(player) && damager.inventory.itemInMainHand.persistentDataContainer.get(DIVINITY_CHAINS, PersistentDataType.BOOLEAN) == true) {
                Divinity.unchainPlayer(player)
            }
        }
    }
}