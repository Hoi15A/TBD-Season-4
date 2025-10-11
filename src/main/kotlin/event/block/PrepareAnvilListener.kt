package event.block

import command.RenameItem
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent

class PrepareAnvilListener : Listener {

    @EventHandler
    fun onPrepareAnvil(event: PrepareAnvilEvent) {
        event.result?.let {
            if(RenameItem().hasBlockedTags(it)) {
                event.result = null
            }
        }
    }
}