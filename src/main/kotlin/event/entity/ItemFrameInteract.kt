package event.entity

import chat.Formatting.allTags
import item.ItemRarity
import item.ItemType
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.ItemFrame
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import java.io.File

object ItemFrameInteract {
    fun itemFrameInteractEvent(event: PlayerInteractEntityEvent) {
        val itemFrame = event.rightClicked as? ItemFrame ?: return
        val player = event.player
        if (player.inventory.itemInMainHand.type == Material.SLIME_BALL) scavengerHunt(event)
        if (!player.isSneaking) return
        when (player.inventory.itemInMainHand.type) {
            Material.AIR -> {
                val newVisibility = !itemFrame.isVisible
                itemFrame.isVisible = newVisibility
                event.isCancelled = true
            }

            Material.HONEYCOMB -> {
                if (itemFrame.isFixed) {
                    // Do nothing if item frame is already fixed
                    return
                }
                itemFrame.isFixed = true
                val amount = player.inventory.itemInMainHand.amount
                val location = itemFrame.location.toBlockLocation().add(0.5, 0.5, 0.5)
                itemFrame.location.world.spawnParticle(Particle.WAX_ON, location, 10, 0.5, 0.5, 0.5)
                player.inventory.itemInMainHand.amount = amount - 1
                event.isCancelled = true
            }

            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE -> {
                if(!itemFrame.isFixed) {
                    // Do nothing if item frame is not fixed
                    return
                }
                itemFrame.isFixed = false
                val location = itemFrame.location.toBlockLocation().add(0.5, 0.5, 0.5)
                itemFrame.location.world.spawnParticle(Particle.WAX_OFF, location, 10, 0.5, 0.5, 0.5)
                val itemMeta = player.inventory.itemInMainHand.itemMeta as Damageable
                val newDamage = itemMeta.damage + 1
                itemMeta.damage = newDamage
                player.inventory.itemInMainHand.itemMeta = itemMeta
                // Break axe if it reaches max damage
                if(itemMeta.damage.toShort() == player.inventory.itemInMainHand.type.maxDurability) {
                    player.inventory.setItemInMainHand(ItemStack(Material.AIR))
                }
                event.isCancelled = true
            }

            else -> {
                // Ignore other interactions for now
            }
        }
    }

    fun scavengerHunt(event: PlayerInteractEntityEvent) {
        val file = File("plugins/tbdseason4/scavengerhunt.yml")

        if (file.exists().not()) return

        if (event.player.world.environment != org.bukkit.World.Environment.NORMAL) return

        val world = event.player.world
        var matchingCount = 0

        val targetFrames = setOf(
            Location(world, 14863.5, 57.5, -284.0),
            Location(world, 14863.5, 56.5, -284.0),
            Location(world, 14863.5, 55.5, -284.0),
            Location(world, 14864.5, 57.5, -284.0),
            Location(world, 14864.5, 56.5, -284.0),
            Location(world, 14864.5, 55.5, -284.0)
        )

        for (location in targetFrames) {
            // Get all item frames at this location
            val nearbyEntities = world.getNearbyEntities(location, 0.5, 0.5, 0.5)
            val itemFrame = nearbyEntities.filterIsInstance<ItemFrame>().firstOrNull()

            if (itemFrame != null) {
                val item = itemFrame.item
                val displayName = item.itemMeta?.displayName

                // Check if the item name starts with "hello"
                if (displayName?.startsWith("\uF007", ignoreCase = true) == true) {
                    matchingCount++
                }
            }
        }

        if (matchingCount == 6) {
            for (location in targetFrames) {
                // Get all item frames at this location
                val nearbyEntities = world.getNearbyEntities(location, 3.0, 3.0, 3.0)
                val itemFrame = nearbyEntities.filterIsInstance<ItemFrame>().firstOrNull()
                itemFrame?.remove()
            }

            file.delete()

            event.player.sendMessage("Congratulations! You placed all the Map pieces!")
            event.player.inventory.itemInMainHand.subtract()
            val reward = ItemStack(Material.SLIME_BALL)
            val rewardMeta = reward.itemMeta
            rewardMeta.displayName(
                allTags.deserialize("<${ItemRarity.MYTHIC.colourHex}>Mysterious Slime Ball").decoration(TextDecoration.ITALIC, false)
            )
            val baseLore = mutableListOf(allTags.deserialize("<reset><white>${ItemRarity.MYTHIC.rarityGlyph}${ItemType.MEMENTO.typeGlyph}").decoration(TextDecoration.ITALIC, false), allTags.deserialize("<reset><yellow>A special reward for completing the #20 Newspaper").decoration(TextDecoration.ITALIC, false), allTags.deserialize("<reset><yellow>It might not be useful now.").decoration(TextDecoration.ITALIC, false), allTags.deserialize("<reset><yellow>But someday, it may be of use.").decoration(TextDecoration.ITALIC, false))
            rewardMeta.lore(baseLore)
            rewardMeta.setEnchantmentGlintOverride(true)

            reward.itemMeta = rewardMeta
            event.player.give(reward)
        }
    }
}