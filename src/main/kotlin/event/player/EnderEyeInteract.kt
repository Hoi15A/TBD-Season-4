package event.player

import chat.Formatting
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.data.type.EndPortalFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import util.Keys.END_PORTAL_FRAMES_WITH_EYE
import util.Keys.TRUE_EYE
import util.Sounds.ENDER_EYE_PLACE_FAIL
import util.Sounds.ENDER_EYE_PLACE_FAIL_BACKGROUND
import util.Sounds.FRAME_EYE_BREAK
import util.pdc.LocationArrayDataType
import util.secondsToTicks
import kotlin.math.max
import kotlin.math.min

class EnderEyeInteract: Listener {
    @EventHandler
    fun enderEyeInteractEvent(event: PlayerInteractEvent) {
        if (!event.action.isRightClick) return
        if (!listOf(Material.END_PORTAL_FRAME).contains(event.clickedBlock?.type)) return
        if (event.item == null) return
        if (event.item!!.type != Material.ENDER_EYE) return
        if (event.item!!.type == Material.ENDER_EYE && event.player.hasCooldown(Material.ENDER_EYE)) return
        val item = event.item!!

        val isTrueEye = item.persistentDataContainer.get(TRUE_EYE, PersistentDataType.BOOLEAN) ?: false
        val clickedBlock = event.clickedBlock!!
        if (!isTrueEye) {
            event.isCancelled = true
            val player = event.player

            if (player.getCooldown(item) != 0) return
            player.setCooldown(item, 10.secondsToTicks())
            player.sendActionBar(Formatting.allTags.deserialize("<red>These types of Eye of Ender don't seem to work in this universe..."))
            clickedBlock.world.playSound(ENDER_EYE_PLACE_FAIL, clickedBlock.location.x, clickedBlock.location.y, clickedBlock.location.z)
            clickedBlock.world.playSound(ENDER_EYE_PLACE_FAIL_BACKGROUND, clickedBlock.location.x, clickedBlock.location.y, clickedBlock.location.z)
            player.velocity = player.location.direction.normalize().multiply(-1).multiply(0.5)
        } else {
            val framesWithEyes = clickedBlock.world.persistentDataContainer.get(END_PORTAL_FRAMES_WITH_EYE, LocationArrayDataType())?.toMutableList() ?: mutableListOf()
            framesWithEyes.add(clickedBlock.location.toBlockLocation())
            clickedBlock.world.persistentDataContainer.set(END_PORTAL_FRAMES_WITH_EYE, LocationArrayDataType(), framesWithEyes.toTypedArray())
            shatterExistingEyes(event, framesWithEyes.toList())
        }
    }

    private fun shatterExistingEyes(event: PlayerInteractEvent, framesWithEyes: List<Location>) {
        val clickedPortalFrame = event.clickedBlock!!
        val frames = findNearbyPortalFrames(clickedPortalFrame)
        var didBreakEye = false
        frames.forEach {
            val portalFrameState = it.blockData as EndPortalFrame

            if (portalFrameState.hasEye() && !framesWithEyes.contains(it.location.toBlockLocation())) {
                portalFrameState.setEye(false)
                it.blockData = portalFrameState
                it.world.spawnParticle(Particle.REVERSE_PORTAL, it.location.add(0.5, 1.0, 0.5), 50, 0.0, 0.0, 0.0, 0.1, null, true)
                it.world.playSound(FRAME_EYE_BREAK, it.location.x, it.location.y, it.location.z)
                didBreakEye = true
            }
        }
        if (didBreakEye) {
            event.player.sendMessage(Formatting.allTags.deserialize("<dark_purple>Some of the eyes may not have been strong enough..."))
        }
    }

    fun findNearbyPortalFrames(block: Block): Set<Block> {
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
                if (currBlock.type == Material.END_PORTAL_FRAME) {
                    blocks.add(block.world.getBlockAt(x, block.y, z))
                }
            }
        }
        return blocks
    }
}