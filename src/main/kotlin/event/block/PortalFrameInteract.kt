package event.block

import item.treasurebag.BagItem
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import util.Keys.PLAYER_PLACED_END_PORTAL_FRAMES
import util.Sounds
import util.pdc.LocationArrayDataType
import kotlin.math.max
import kotlin.math.min

class PortalFrameInteract: Listener {
    @EventHandler
    fun portalFrameBreakEvent(event: PlayerInteractEvent) {
        if (!event.action.isRightClick) return
        if (!listOf(Material.END_PORTAL_FRAME).contains(event.clickedBlock?.type)) return
        if (event.item != null) return
        if (!event.player.isSneaking) return
        if (!event.clickedBlock?.world?.persistentDataContainer?.has(PLAYER_PLACED_END_PORTAL_FRAMES)!!) return

        val clickedBlock = event.clickedBlock!!
        val placedFrames = clickedBlock.world.persistentDataContainer.get(PLAYER_PLACED_END_PORTAL_FRAMES, LocationArrayDataType())?.toMutableList() ?: mutableListOf()
        if (placedFrames.contains(clickedBlock.location)) {
            val nearbyPortalBlocks = findNearbyPortalBlocks(clickedBlock)
            if(nearbyPortalBlocks.any { it.type == Material.END_PORTAL }) {
                nearbyPortalBlocks.forEach {
                        block ->
                    if(block.type == Material.END_PORTAL_FRAME) {
                        placedFrames.remove(block.location)
                        block.world.persistentDataContainer.set(PLAYER_PLACED_END_PORTAL_FRAMES, LocationArrayDataType(), placedFrames.toTypedArray())
                        block.location.world.dropItem(block.location.add(0.0, 0.0, 0.0), BagItem.DRAGON_PORTAL_FRAME.itemStack)
                    }
                    block.type = Material.AIR
                    block.location.world.spawnParticle(Particle.REVERSE_PORTAL, block.location.add(0.5, 0.5, 0.5), 50, 0.0, 0.0, 0.0, 0.1, null, true)
                }
            } else
            placedFrames.remove(clickedBlock.location)
            clickedBlock.world.persistentDataContainer.set(PLAYER_PLACED_END_PORTAL_FRAMES, LocationArrayDataType(), placedFrames.toTypedArray())
            clickedBlock.location.world.dropItem(clickedBlock.location.add(0.5, 0.0, 0.5), BagItem.DRAGON_PORTAL_FRAME.itemStack)
            clickedBlock.type = Material.AIR
            clickedBlock.location.world.playSound(Sounds.FRAME_EYE_BREAK)
            clickedBlock.location.world.playSound(Sounds.FRAME_BREAK)
            clickedBlock.location.world.spawnParticle(Particle.REVERSE_PORTAL, clickedBlock.location.add(0.5, 0.5, 0.5), 50, 0.0, 0.0, 0.0, 0.1, null, true)
        }
    }

    fun findNearbyPortalBlocks(block: Block): Set<Block> {
        val cornerA = block.location.add(4.0, 0.0, 4.0)
        val cornerB = block.location.subtract(4.0, 0.0, 4.0)
        val minX = min(cornerA.blockX, cornerB.blockX)
        val minZ = min(cornerA.blockZ, cornerB.blockZ)
        val maxX = max(cornerA.blockX, cornerB.blockX)
        val maxZ = max(cornerA.blockZ, cornerB.blockZ)

        val blocks = mutableSetOf<Block>()

        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                if (block.x == x && block.z == z) continue
                val currBlock = block.world.getBlockAt(x, block.y, z)
                if (currBlock.type in listOf(Material.END_PORTAL_FRAME, Material.END_PORTAL)) {
                    blocks.add(block.world.getBlockAt(x, block.y, z))
                }
            }
        }
        return blocks
    }

    @EventHandler
    fun portalFramePlaceEvent(event: BlockPlaceEvent) {
        if (event.itemInHand.asOne() == BagItem.DRAGON_PORTAL_FRAME.itemStack) {
            val placedFrames = event.blockPlaced.world.persistentDataContainer.get(PLAYER_PLACED_END_PORTAL_FRAMES, LocationArrayDataType())?.toMutableList() ?: mutableListOf()
            placedFrames.add(event.blockPlaced.location)
            event.blockPlaced.world.persistentDataContainer.set(PLAYER_PLACED_END_PORTAL_FRAMES, LocationArrayDataType(), placedFrames.toTypedArray())
        }
    }
}