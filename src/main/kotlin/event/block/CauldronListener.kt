package event.block

import io.papermc.paper.event.entity.EntityInsideBlockEvent
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CauldronListener : Listener {

    @EventHandler
    fun onEntityInsideBlock(event: EntityInsideBlockEvent) {
        val block = event.block
        if (block.type != Material.WATER_CAULDRON) return
        val entity = event.entity
        if (entity !is Item) return
        val stack = entity.itemStack
        if (stack.type != Material.FILLED_MAP) return

        entity.remove()

        val world = block.world
        world.dropItem(entity.location, org.bukkit.inventory.ItemStack(Material.MAP, stack.amount))
    }
}