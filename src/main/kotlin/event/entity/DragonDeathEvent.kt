package event.entity

import item.treasurebag.BagType
import item.treasurebag.TreasureBag

import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class DragonDeathEvent: Listener {
    @EventHandler
    private fun onDragonDeath(event: EntityDeathEvent) {
        if(event.entity is EnderDragon) {
            val treasureBagEntity = event.entity.world.spawn(event.entity.location, Item::class.java)
            treasureBagEntity.itemStack = TreasureBag.create(BagType.DRAGON_SCALE)
            treasureBagEntity.isGlowing = true
            treasureBagEntity.isCustomNameVisible = true
            treasureBagEntity.customName(treasureBagEntity.itemStack.effectiveName())
        }
    }
}