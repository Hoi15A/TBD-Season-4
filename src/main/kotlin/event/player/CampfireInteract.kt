package event.player

import fishing.FishRarity
import fishing.Fishing.hasSubRarity
import util.Sounds.CAMPFIRE_DISALLOW_FISH_COOK
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.data.type.Campfire
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import util.Keys.FISH_RARITY
import util.secondsToTicks

object CampfireInteract {
    private val mm = MiniMessage.miniMessage()

    fun campfireInteractEvent(event: PlayerInteractEvent) {
        if (!listOf(Material.CAMPFIRE, Material.SOUL_CAMPFIRE).contains(event.clickedBlock!!.type)) return
        if (event.item == null) return
        val item = event.item!!
        if (!Tag.ITEMS_FISHES.isTagged(item.type)) return

        val fishRarityStr = item.persistentDataContainer.get(FISH_RARITY, PersistentDataType.STRING) ?: return

        val fishRarity = FishRarity.valueOf(fishRarityStr)
        if (fishRarity.props.retainData || item.hasSubRarity()) {
            event.isCancelled = true
            val player = event.player
            val block = event.clickedBlock!!
            if (player.getCooldown(item) != 0) return
            player.setCooldown(item, 10.secondsToTicks())
            player.sendActionBar(
                mm.deserialize(
                    "<red>The <fish> is too powerful to be cooked on a campfire!</red>",
                    Placeholder.component("fish", item.effectiveName())
                )
            )
            block.world.playSound(CAMPFIRE_DISALLOW_FISH_COOK, block.location.x, block.location.y, block.location.z)
            player.velocity = player.location.direction.normalize().multiply(-1).multiply(0.5)
            val campfireData = block.blockData as Campfire
            campfireData.isLit = false
            block.blockData = campfireData
        }
    }
}