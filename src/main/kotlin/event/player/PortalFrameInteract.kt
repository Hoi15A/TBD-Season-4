package event.player

import item.treasurebag.BagItem
import org.bukkit.Material
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
                    block.type = Material.AIR
                    clickedBlock.type = Material.AIR
                    placedFrames.remove(block.location)
                    clickedBlock.world.persistentDataContainer.set(PLAYER_PLACED_END_PORTAL_FRAMES, LocationArrayDataType(), placedFrames.toTypedArray())
                    block.location.world.playSound(Sounds.FRAME_EYE_BREAK)
                    block.location.world.playSound(Sounds.FRAME_BREAK)
                }
            } else {
                clickedBlock.location.world.dropItem(clickedBlock.location, BagItem.DRAGON_PORTAL_FRAME.itemStack)
                clickedBlock.location.world.playSound(Sounds.FRAME_EYE_BREAK)
                clickedBlock.location.world.playSound(Sounds.FRAME_BREAK)
                placedFrames.remove(clickedBlock.location)
                clickedBlock.world.persistentDataContainer.set(PLAYER_PLACED_END_PORTAL_FRAMES, LocationArrayDataType(), placedFrames.toTypedArray())
                clickedBlock.type = Material.AIR
            }
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