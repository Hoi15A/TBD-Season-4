package event.player

import chat.Formatting
import item.ItemRarity
import item.ItemType
import item.treasurebag.BagItem
import java.net.URI
import java.util.UUID
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
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import util.Keys

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

            val portalFrameState = clickedBlock.blockData as EndPortalFrame
            if(portalFrameState.hasEye()) return
            if(item.asOne() == BagItem.DRAGON_EYE.itemStack.asOne()) return
            giveMemento(event)
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

    fun giveMemento(event: PlayerInteractEvent) {
        val player = event.player
        val trueEye = event.item!!
        val baseLore = listOf("<white><!i>${ItemRarity.EPIC.rarityGlyph}${ItemType.MEMENTO.typeGlyph}", "<!i><yellow>You feel a strange energy emerging from within.").map { Formatting.allTags.deserialize(it) }
        val obtainedLore = listOf("", "<!i><grey>Placed by: <white>${event.player.name}").map { Formatting.allTags.deserialize(it) }
        val newLore = baseLore + trueEye.itemMeta.lore()!![2] + obtainedLore
        val memento = ItemStack(Material.PLAYER_HEAD)
        val mementoMeta = memento.itemMeta as SkullMeta
        val mementoProfile = Bukkit.createProfile(Bukkit.getOfflinePlayer(UUID.fromString("c4400882-3580-45da-a6a0-ec98865a5435")).uniqueId)
        val mementoTexture = mementoProfile.textures
        mementoTexture.skin = URI("http://textures.minecraft.net/texture/d39f1c0ddcf53833bac5fbf57715f7c253eefd2872ff27e4a893be30529bc685").toURL()
        mementoProfile.setTextures(mementoTexture)
        mementoMeta.playerProfile = mementoProfile
        mementoMeta.lore(newLore)
        mementoMeta.displayName(Formatting.allTags.deserialize("<!i><${ItemRarity.EPIC.colourHex}>Remnant of a True Eye"))
        mementoMeta.persistentDataContainer.set(Keys.MEMENTO_TYPE, PersistentDataType.STRING, "true_eye_memento")
        memento.itemMeta = mementoMeta
        player.inventory.addItem(memento)
        Memory.saveMemory(memento)
    }
}