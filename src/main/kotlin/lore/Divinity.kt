package lore

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import plugin

object Divinity {
    val chainedPlayers = mutableMapOf<Player, Chains>()
    fun chainPlayer(player: Player) {
        val chain1 = player.world.spawn(player.location.clone().add(0.0, 0.8, 0.0), ItemDisplay::class.java).apply {
            setItemStack(getChainItemStack())
            viewRange = 2.0f
            brightness = Display.Brightness(15, 15)
            transformation = Transformation(Vector3f(0.0f, 0.0f, 0.0f), Quaternionf(0.6f, 0.0f, 0.0f, 0.8f), Vector3f(1f, 1f, 1f), Quaternionf(0.45f, 0.0f, 0.0f, 1f))
        }
        val chain2 = player.world.spawn(player.location.clone().add(0.0, 0.8, 0.0), ItemDisplay::class.java).apply {
            setItemStack(getChainItemStack())
            viewRange = 2.0f
            brightness = Display.Brightness(15, 15)
            transformation = Transformation(Vector3f(0.0f, 0.0f, 0.0f), Quaternionf(0.45f, 0.0f, 0.0f, 1f), Vector3f(1f, 1f, 1f), Quaternionf(0.6f, 0.0f, 0.0f, 0.8f))
        }
        chainedPlayers[player] = Chains(chain1, chain2)
        object : BukkitRunnable() {
            override fun run() {
                if(chainedPlayers.containsKey(player)) {
                    if(player.isOnline) {
                        chain1.setRotation(if(chain1.yaw in -180f..180f) chain1.yaw + 7f else -180f, 0f)
                        chain2.setRotation(if(chain2.yaw in -180f..180f) chain2.yaw - 7f else 180f, 0f)
                    } else {
                        chainedPlayers.remove(player)
                    }
                } else {
                    chain1.remove()
                    chain2.remove()
                    cancel()
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    fun unchainPlayer(player: Player) {
        chainedPlayers.remove(player)
    }

    fun clearChains() {
        chainedPlayers.clear()
        for (world in Bukkit.getWorlds()) {
            for (itemDisplay in world.getEntitiesByClass(ItemDisplay::class.java)) {
                if(itemDisplay.itemStack.itemMeta.itemModel == NamespacedKey("minecraft", "divine_chains")) {
                    itemDisplay.remove()
                }
            }
        }
    }

    private fun getChainItemStack(): ItemStack {
        val modelItem = ItemStack(Material.ECHO_SHARD, 1)
        val modelItemMeta = modelItem.itemMeta
        modelItemMeta.itemModel = NamespacedKey("minecraft", "divine_chains")
        modelItem.itemMeta = modelItemMeta
        return modelItem
    }
}

data class Chains(val chain1: ItemDisplay, val chain2: ItemDisplay)