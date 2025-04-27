package command

import chat.ChatUtility
import fishing.FishRarity
import fishing.Fishing
import io.papermc.paper.command.brigadier.CommandSourceStack
import lib.Sounds
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.entity.Bat
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import plugin
import util.secondsToTicks
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class Debug {
    @Command("debug catch <rarity> <shiny>")
    @Permission("tbd.command.debug")
    fun debug(css: CommandSourceStack, @Argument("rarity") rarity: FishRarity, @Argument("shiny") shiny: Boolean) {
        if(css.sender is Player) {
            val player = css.sender as Player
            if(player.gameMode == GameMode.CREATIVE) {
                css.sender.sendMessage(Component.text("Simulating catch of rarity $rarity"))
                val loc = player.location
                object : BukkitRunnable() {
                    override fun run() {
                        val item = loc.world.spawn(loc, Item::class.java)
                        item.itemStack = ItemStack(Material.BEEF, 1)
                        Fishing.catchFish(player, item, item.location, rarity, shiny)
                    }
                }.runTaskLater(plugin, 100L)
            }
        }
    }

    @Command("test soul_death")
    @Permission("tbd.command.debug")
    fun testParticle(css: CommandSourceStack) {
        if(css.sender is Player) {
            val player = css.sender as Player
            soulFlame(player.location)
            soulBats(player.location)
            smokeSpiral(player.location)
        }
    }

    private fun soulFlame(origin: Location) {
        val duration = 30.secondsToTicks()
        object : BukkitRunnable() {
            var ticks = 0
            override fun run() {
                if(ticks > duration) {
                    cancel()
                    return
                }
                if(ticks % 10 == 0) {
                    origin.world.playSound(Sounds.SOUL_HURT)
                }
                origin.world.spawnParticle(
                    Particle.SOUL_FIRE_FLAME,
                    origin.clone().add(0.0, 0.25, 0.0),
                    10, 0.0, 0.0, 0.0, 0.25, null, true
                )
                ticks++
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    private fun soulBats(origin: Location) {
        fun getSoul(location: Location): Bat {
            val bat = location.world.spawnEntity(location, EntityType.BAT) as Bat
            bat.isAwake = true
            bat.isSilent = true
            bat.isInvisible = true
            bat.isInvulnerable = true
            bat.addScoreboardTag("soul.bat.${bat.uniqueId}")
            return bat
        }
        object : BukkitRunnable() {
            val soulAmount = 40
            var timer = 0
            val souls = ArrayList<Bat>()
            override fun run() {
                if (timer <= soulAmount) {
                    val soul = getSoul(origin)
                    souls.add(soul)
                    soul.velocity = Vector(0.0, 0.5, 0.0)
                }
                for (soul in souls) {
                    soul.world.spawnParticle(Particle.SCULK_SOUL, soul.location, 2, 0.0, 0.0, 0.0, 0.0, null, true)
                    soul.fireTicks = 0
                }
                if (timer >= 30.secondsToTicks()) {
                    for (soul in souls) soul.remove()
                    souls.clear()
                    this.cancel()
                } else {
                    timer++
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    private fun smokeSpiral(origin: Location) {
        val duration = 30.secondsToTicks()
        val numArms = 4
        val stepsPerArm = 50
        val maxRadius = 6.0
        val spiralTightness = 3 * PI
        val rotationSpeed = 0.05
        object : BukkitRunnable() {
            var ticks = 0
            var globalAngle = 0.0
            override fun run() {
                if (ticks > duration) {
                    origin.world.playSound(Sounds.SOUL_RELEASE)
                    origin.world.setStorm(true)
                    for(i in origin.blockY.downTo(-63)) {
                        val relativeBlock = origin.world.getBlockAt(origin.blockX, i, origin.blockZ)
                        if(relativeBlock.type in listOf(Material.AIR, Material.FIRE, Material.WATER, Material.LAVA) && relativeBlock.getRelative(BlockFace.DOWN).isSolid) {
                            relativeBlock.type = Material.SCULK_CATALYST
                            origin.world.strikeLightningEffect(relativeBlock.location)
                            break
                        }
                    }
                    ChatUtility.messageAudience(Audience.audience(Bukkit.getOnlinePlayers()), "<dark_gray><i>An otherworldly presence lingers...<reset>", false)
                    cancel()
                    return
                }
                /*if (ticks >= 3.secondsToTicks() && ticks % 5 == 0) {
                    val randomLoc = Location(origin.world, origin.x + Random.nextInt(-15, 15), origin.y + Random.nextInt(-10, 10), origin.z + Random.nextInt(-15, 15))
                    origin.world.spawn(randomLoc, Fireball::class.java).apply {
                        isInvisible = true
                        acceleration = Vector(0, -10, 0)
                        yield = 15f
                    }
                    origin.world.spawn(randomLoc.clone().add(0.0, 1.0, 0.0), ExplosiveMinecart::class.java).apply {
                        explode()
                    }
                }*/
                for (arm in 0 until numArms) {
                    val armOffset = (2 * PI / numArms) * arm
                    for (i in 0..stepsPerArm) {
                        val t = i.toDouble() / stepsPerArm
                        val r = t * maxRadius
                        val localAngle = t * spiralTightness
                        val angle = localAngle + armOffset + globalAngle
                        val x = r * cos(angle)
                        val z = r * sin(angle)
                        val y = 0.25
                        origin.world?.spawnParticle(
                            Particle.SMOKE,
                            origin.clone().add(x, y, z),
                            0, 0.0, 0.0, 0.0, 0.0, null, true
                        )
                    }
                }
                globalAngle += rotationSpeed
                ticks++
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }
}