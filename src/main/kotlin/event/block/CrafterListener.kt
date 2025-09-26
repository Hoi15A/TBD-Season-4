package event.block

import event.player.PlayerCraft
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.CrafterCraftEvent


class CrafterListener : Listener {

    @EventHandler
    fun onCrafterCraft(event: CrafterCraftEvent) {
        val result = event.result
        if (result.type == Material.FILLED_MAP && PlayerCraft.isMapUncopyable(result)) {
            event.isCancelled = true
        }
    }
}