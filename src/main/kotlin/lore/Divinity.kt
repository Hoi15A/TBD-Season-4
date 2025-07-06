package lore

import org.bukkit.*
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import plugin
import util.Sounds

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

    fun banishPlayer(player: Player, banished: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 8 * 20, 0, false, false))
        player.teleport(player.location.setRotation(player.yaw, 0f))
        Bukkit.dispatchCommand(player, "function animated_java:byrtanimation/summon {args: {animation: 'animation.model.sword_lift', start_animation: true}}")
        player.inventory.setItemInMainHand(null)
        object : BukkitRunnable() {
            var ticks = 0
            var seconds = 0
            override fun run() {
                // Sword out
                if(seconds == 0 && ticks == 5) {
                    banished.world.playSound(Sounds.DIVINIFICATION_BLADE_OUT)
                }
                // Beam start
                if(seconds == 1 && ticks == 0) {
                    banished.world.playSound(Sounds.DIVINIFICATION_BEAM_START)
                }
                // Beam charging
                if(seconds in 1..4 && ticks % 2 == 0) {
                    banished.world.playSound(Sounds.DIVINIFICATION_CHARGING)
                }
                // Banishment
                if(seconds == 4 && ticks == 12) {
                    unchainPlayer(banished)
                    banished.damage(Double.MAX_VALUE, player)
                    banished.world.spawnParticle(Particle.SOUL, banished.location.clone().add(0.0, 1.0, 0.0), 1000, 0.0, 0.0, 0.0, 1.0, null, true)
                    banished.world.spawnParticle(Particle.END_ROD, banished.location.clone().add(0.0, 1.0, 0.0), 1000, 0.0, 0.0, 0.0, 1.0, null, true)
                    banished.world.playSound(Sounds.DIVINIFICATION_DEATH)
                    banished.world.playSound(Sounds.DIVINIFICATION_BEAM_EXTINGUISH)
                }

                // End of animation
                if(seconds == 8 && ticks == 0) {
                    for (world in Bukkit.getWorlds()) {
                        for(itemDisplay in world.getEntitiesByClass(ItemDisplay::class.java)) {
                            if(itemDisplay.scoreboardTags.contains("aj.byrtanimation.root")) {
                                player.teleport(itemDisplay)
                                itemDisplay.remove()
                            }
                            if(itemDisplay.scoreboardTags.contains("aj.byrtanimation.node")) {
                                itemDisplay.remove()
                            }
                        }
                    }
                    cancel()
                }
                ticks++
                if(ticks >= 20) {
                    ticks = 0
                    seconds++
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
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