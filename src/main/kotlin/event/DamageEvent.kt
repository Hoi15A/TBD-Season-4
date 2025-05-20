package event

import org.bukkit.entity.Firework
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class DamageEvent: Listener {
    @EventHandler
    private fun onFishingFireworkDamage(e: EntityDamageByEntityEvent) {
        if(e.damager is Firework) {
            if(e.damager.scoreboardTags.contains("tbd.firework")) {
                e.isCancelled = true
            }
        }
    }
}