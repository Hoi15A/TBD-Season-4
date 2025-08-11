package command

import chat.Formatting.allTags
import fishing.Fishing
import io.papermc.paper.command.brigadier.CommandSourceStack
import item.ItemRarity
import item.ItemType
import util.Sounds
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import plugin
import util.Keys.TRUE_EYE
import util.secondsToTicks
import util.ui.MemoryFilter
import java.time.Duration
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class TrueEye {
    @Command("trueeye <position> [lore]")
    @Permission("tbd.command.eye")
    fun mintEye(css: CommandSourceStack, position: Location, @Greedy lore: String?) {
        if(css.sender is Player) {
            val player = css.sender as Player
            val trueEye = ItemStack(Material.ENDER_EYE)
            val trueEyeMeta = trueEye.itemMeta
            position.world = player.world
            trueEyeMeta.displayName(
                allTags.deserialize("<${ItemRarity.EPIC.colourHex}>True Eye of Ender").decoration(TextDecoration.ITALIC, false)
            )
            val baseLore = mutableListOf(allTags.deserialize("<reset><white>${ItemRarity.EPIC.rarityGlyph}${ItemType.CONSUMABLE.typeGlyph}").decoration(TextDecoration.ITALIC, false), allTags.deserialize("<reset><yellow>One of the ancient eyes.").decoration(TextDecoration.ITALIC, false))
            if(lore != null) {
                baseLore.add(allTags.deserialize(lore))
            }
            trueEyeMeta.lore(baseLore)
            trueEyeMeta.setEnchantmentGlintOverride(true)
            trueEyeMeta.persistentDataContainer.set(TRUE_EYE, PersistentDataType.BOOLEAN, true)
            trueEye.itemMeta = trueEyeMeta

            object : BukkitRunnable() {
                var radius = 6.0
                var pitch = 0.0f
                override fun run() {
                    if (radius < 0.0) {
                        val enderEyeTeam = Bukkit.getServer().scoreboardManager.mainScoreboard.registerNewTeam("tbd.true_eye.${UUID.randomUUID()}")
                        enderEyeTeam.color(NamedTextColor.DARK_PURPLE)

                        val trueEyeEntity = position.world.spawn(position, Item::class.java)
                        trueEyeEntity.itemStack = trueEye
                        trueEyeEntity.setGravity(false)
                        trueEyeEntity.isGlowing = true
                        trueEyeEntity.velocity = Vector().zero()
                        trueEyeEntity.pickupDelay = 10.secondsToTicks()
                        trueEyeEntity.setWillAge(false)
                        enderEyeTeam.addEntity(trueEyeEntity)

                        trueEyeEntity.location.world.strikeLightningEffect(position)
                        Fishing.firework(position, flicker = false, trail = false, ItemRarity.EPIC.colour, FireworkEffect.Type.BALL_LARGE, false)

                        val eyeSummonMessage = allTags.deserialize("<#32FF82>A <${ItemRarity.EPIC.colourHex}>True Eye of Ender<#32FF82> has awoken...")

                        Bukkit.getServer().showTitle(
                            Title.title(
                                allTags.deserialize(""),
                                eyeSummonMessage,
                                Title.Times.times(Duration.ofMillis(250L), Duration.ofSeconds(5L), Duration.ofMillis(250L))
                            )
                        )
                        Bukkit.getServer().sendMessage(eyeSummonMessage)
                        position.world.spawnParticle(Particle.FIREWORK, position, 300, 0.0, 0.0, 0.0, 1.0, null, true)
                        position.world.spawnParticle(Particle.END_ROD, position, 300, 0.0, 0.0, 0.0, 1.0, null, true)
                        Fishing.shinyEffect(trueEyeEntity)
                        Bukkit.getServer().playSound(Sounds.TRUE_EYE_SPAWN)

                        Memory.saveMemory(trueEye, MemoryFilter.SEASON_FOUR)

                        cancel()
                        return
                    }
                    val step = Math.PI / 16
                    for (angle in 0 until 32) {
                        val x = radius * cos(angle * step)
                        val z = radius * sin(angle * step)
                        val particleLocation = position.clone().add(x, -0.75, z)
                        position.world.spawnParticle(Particle.WITCH, particleLocation, 1, 0.0, 0.0, 0.0, 0.0, null, true)
                        position.world.spawnParticle(Particle.PORTAL, particleLocation, 1, 0.0, 0.0, 0.0, 0.0, null, true)
                    }
                    position.world.playSound(position, "entity.enderman.teleport", 4f, pitch)
                    position.world.playSound(position, "entity.ender_eye.death", 4f, pitch)
                    pitch += 0.0625f
                    radius -= 0.2
                }
            }.runTaskTimer(plugin, 0L, 2L)
        }
    }
}