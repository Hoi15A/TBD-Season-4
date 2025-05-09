package fishing

import chat.Formatting.allTags
import util.Keys.FISH_RARITY
import plugin
import item.ItemRarity
import item.ItemType
import item.SubRarity
import lib.Sounds
import logger

import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title

import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import util.Keys.FISH_IS_SHINY
import util.startsWithVowel

import java.time.Duration
import java.util.*

import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object Fishing {
    fun catchFish(
        player: Player,
        item: Item,
        location: Location,
        forcedFishRarity: FishRarity?,
        forcedFishShiny: Boolean?
    ) {
        val fishRarity = forcedFishRarity ?: FishRarity.getRandomRarity()
        val isShiny = forcedFishShiny ?: SubRarity.isShiny()

        val caughtByLore =
            if (fishRarity.props.showCatcher || isShiny) allTags.deserialize("<reset><white>Caught by <yellow>${player.name}<white>.")
                .decoration(TextDecoration.ITALIC, false) else null
        val fishMeta = item.itemStack.itemMeta
        fishMeta.displayName(
            allTags.deserialize("<${fishRarity.itemRarity.colourHex}>${item.name}").decoration(TextDecoration.ITALIC, false)
        )
        fishMeta.lore(
            if (caughtByLore == null) {
                listOf(
                    allTags.deserialize("<reset><white>${fishRarity.itemRarity.rarityGlyph}${ItemType.FISH.typeGlyph}")
                        .decoration(TextDecoration.ITALIC, false)
                )
            } else {
                listOf(
                    allTags.deserialize("<reset><white>${fishRarity.itemRarity.rarityGlyph}${if (isShiny) "<reset><white>${SubRarity.SHINY.subRarityGlyph}${ItemType.FISH.typeGlyph}" else "<reset><white>${ItemType.FISH.typeGlyph}"}")
                        .decoration(TextDecoration.ITALIC, false), caughtByLore
                )
            }
        )
        if (isShiny) {
            fishMeta.setEnchantmentGlintOverride(true)
            fishMeta.persistentDataContainer.set(FISH_IS_SHINY, PersistentDataType.BOOLEAN, true)
        }
        fishMeta.persistentDataContainer.set(FISH_RARITY, PersistentDataType.STRING, fishRarity.name)
        item.itemStack.setItemMeta(fishMeta)

        player.sendActionBar(
            allTags.deserialize("Caught <${fishRarity.itemRarity.colourHex}><b>${fishRarity.itemRarity.name.uppercase()}</b> ")
                .append(item.itemStack.effectiveName()).append(allTags.deserialize("<reset>."))
        )

        if (fishRarity.props.sendGlobalMsg) catchText(player, item, fishRarity)
        if (fishRarity.props.sendGlobalTitle) catchTitle(player, item, fishRarity)
        if (fishRarity.props.isAnimated) catchAnimation(player, item, location.add(0.0, 1.75, 0.0), fishRarity)
        if (fishRarity in listOf(FishRarity.LEGENDARY, FishRarity.MYTHIC, FishRarity.UNREAL)) logger.info("(FISHING) ${player.name} caught $fishRarity ${item.name}.")
        if (isShiny) shinyEffect(item)
    }

    private fun catchText(catcher: Player, item: Item, fishRarity: FishRarity) {
        Bukkit.getServer().sendMessage(
            playerCaughtFishComponent(fishRarity, catcher, item)
        )
    }

    private fun catchTitle(catcher: Player, item: Item, fishRarity: FishRarity) {
        Bukkit.getServer().showTitle(
            Title.title(
                allTags.deserialize("<${fishRarity.itemRarity.colourHex}><b>${fishRarity.itemRarity.rarityName.uppercase()}<reset>"),
                playerCaughtFishComponent(fishRarity, catcher, item),
                Title.Times.times(Duration.ofMillis(250L), Duration.ofSeconds(3L), Duration.ofMillis(250L))
            )
        )
    }

    private fun playerCaughtFishComponent(
        fishRarity: FishRarity,
        catcher: Player,
        item: Item
    ) = allTags.deserialize(
        "<tbdcolour>${catcher.name}<reset> caught a${
            if (fishRarity.itemRarity.rarityName.startsWithVowel()) "n " else " "
        }<${fishRarity.itemRarity.colourHex}><b>${fishRarity.name}</b> ${
            item.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }<reset>."
    )

    private fun catchAnimation(catcher: Player, item: Item, location: Location, fishRarity: FishRarity) {
        when (fishRarity) {
            FishRarity.RARE -> {
                firework(
                    location,
                    flicker = false,
                    trail = false,
                    fishRarity.itemRarity.colour,
                    FireworkEffect.Type.BURST,
                    false
                )
            }
            FishRarity.EPIC -> {
                catcher.playSound(Sounds.EPIC_CATCH)
                firework(
                    location,
                    flicker = false,
                    trail = false,
                    fishRarity.itemRarity.colour,
                    FireworkEffect.Type.BALL,
                    false
                )
                epicEffect(location)
            }
            FishRarity.LEGENDARY -> {
                Bukkit.getServer().playSound(Sounds.LEGENDARY_CATCH)
                for (i in 0..2) {
                    object : BukkitRunnable() {
                        override fun run() {
                            firework(
                                location,
                                flicker = true,
                                trail = true,
                                fishRarity.itemRarity.colour,
                                FireworkEffect.Type.BALL_LARGE,
                                false
                            )
                        }
                    }.runTaskLater(plugin, i * 2L)
                }
                legendaryEffect(location)
            }
            FishRarity.MYTHIC -> {
                Bukkit.getServer().playSound(Sounds.MYTHIC_CATCH)
                for (i in 0..15) {
                    object : BukkitRunnable() {
                        override fun run() {
                            firework(
                                location,
                                flicker = true,
                                trail = false,
                                fishRarity.itemRarity.colour,
                                if (i <= 11) FireworkEffect.Type.BALL_LARGE else FireworkEffect.Type.BALL,
                                false
                            )
                        }
                    }.runTaskLater(plugin, i * 2L)
                }
                for (i in 0..60) {
                    object : BukkitRunnable() {
                        override fun run() {
                            firework(
                                location,
                                i % 2 == 0,
                                i % 3 == 0,
                                fishRarity.itemRarity.colour,
                                if (i % 2 == 0) FireworkEffect.Type.BALL_LARGE else FireworkEffect.Type.BALL,
                                true
                            )
                        }
                    }.runTaskLater(plugin, (i * 3L) + 30L)
                }
            }
            FishRarity.UNREAL -> {
                val server = Bukkit.getServer()
                server.playSound(Sounds.UNREAL_CATCH)
                server.playSound(Sounds.UNREAL_CATCH_SPAWN)
                val previousDayTime = catcher.world.time
                val previousFullTime = catcher.world.fullTime
                if (catcher.world.time < 6000) catcher.world.time += 6000 - catcher.world.time
                if (catcher.world.time > 6000) catcher.world.time -= 6000 + catcher.world.time
                for (i in 0..15) {
                    object : BukkitRunnable() {
                        val startLoc = item.location.clone()
                        override fun run() {
                            startLoc.world.spawnParticle(
                                Particle.SONIC_BOOM,
                                startLoc.add(0.0, i.toDouble(), 0.0),
                                1,
                                0.0,
                                0.0,
                                0.0,
                                0.0
                            )
                            if (i == 15) {
                                unrealEffect(startLoc)
                                startLoc.world.spawnParticle(
                                    Particle.SOUL_FIRE_FLAME,
                                    startLoc,
                                    100,
                                    0.0,
                                    0.0,
                                    0.0,
                                    0.35
                                )
                                startLoc.world.spawnParticle(Particle.SCULK_SOUL, startLoc, 100, 0.0, 0.0, 0.0, 0.40)
                                for (player in Bukkit.getOnlinePlayers()) player.playSound(Sounds.UNREAL_CATCH_SPAWN_BATS)
                            }
                        }
                    }.runTaskLater(plugin, i * 2L)
                }
                for (i in 0..19) {
                    object : BukkitRunnable() {
                        override fun run() {
                            catcher.world.strikeLightningEffect(
                                item.location.set(
                                    item.location.x,
                                    -64.0,
                                    item.location.z
                                )
                            )
                            if (i % 2 == 0) {
                                catcher.world.time += 12000
                                item.isGlowing = true
                            } else {
                                catcher.world.time -= 12000
                                item.isGlowing = false
                            }
                            if (i == 19) {
                                catcher.world.fullTime = previousFullTime
                                catcher.world.time = previousDayTime
                            }
                        }
                    }.runTaskLater(plugin, i * 15L)
                }
            }
            FishRarity.TRANSCENDENT -> {

            }
            FishRarity.CELESTIAL -> {

            }
            else -> { /* do nothing */ }
        }
    }

    private fun epicEffect(location: Location) {
        object : BukkitRunnable() {
            var radius = 0.0
            override fun run() {
                if (radius > 4.0) {
                    cancel()
                    return
                }
                val step = Math.PI / 16
                for (angle in 0 until 32) {
                    val x = radius * cos(angle * step)
                    val z = radius * sin(angle * step)
                    val particleLocation = location.clone().add(x, -0.75, z)
                    location.world.spawnParticle(Particle.WITCH, particleLocation, 1, 0.0, 0.0, 0.0, 0.0)
                }
                radius += 0.2
            }
        }.runTaskTimer(plugin, 0L, 2L)
    }

    private fun legendaryEffect(location: Location) {
        val effectLoc = location.clone()
        for (i in 0..3) {
            object : BukkitRunnable() {
                override fun run() {
                    effectLoc.world.playSound(Sounds.LEGENDARY_CATCH_EXPLODE)
                    effectLoc.world.spawnParticle(
                        Particle.EXPLOSION,
                        effectLoc.add(
                            Random.nextDouble(-0.25, 0.25),
                            Random.nextDouble(-0.25, 0.25),
                            Random.nextDouble(-0.25, 0.25)
                        ),
                        1,
                        0.0,
                        0.0,
                        0.0,
                        0.0
                    )
                }
            }.runTaskLater(plugin, (i * 4L) + 35L)
        }

        for (i in 0..20) {
            object : BukkitRunnable() {
                override fun run() {
                    for (r in 0..3) {
                        for (j in 0 until 32) {
                            val angle = 2 * Math.PI * j / 32
                            val x = r * cos(angle)
                            val z = r * sin(angle)
                            val particleLocation = location.clone().add(x, -1.5, z)
                            location.world?.spawnParticle(Particle.FLAME, particleLocation, 1, 0.0, 0.0, 0.0, 0.0)
                        }
                    }
                }
            }.runTaskLater(plugin, i * 2L)
        }
    }

    private fun unrealEffect(location: Location) {
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
            val soulAmount = 20
            var timer = 0
            val souls = ArrayList<Bat>()
            override fun run() {
                if (timer <= soulAmount) {
                    val soul = getSoul(location)
                    souls.add(soul)
                    soul.velocity = Vector(0.0, 0.15, 0.0)
                }
                for (soul in souls) soul.world.spawnParticle(Particle.SCULK_SOUL, soul.location, 2, 0.0, 0.0, 0.0, 0.0)
                if (timer >= 14 * 20) {
                    for (soul in souls) soul.remove()
                    souls.clear()
                    this.cancel()
                } else {
                    timer++
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)

        object : BukkitRunnable() {
            var i = 0
            var loc = location.clone()
            var radius = 0.0
            var y = 0.0
            override fun run() {
                val x = radius * cos(y)
                val z = radius * sin(y)
                if (i % 2 == 0) firework(
                    Location(location.world, loc.x + x, loc.y + y, loc.z + z),
                    flicker = false,
                    trail = false,
                    ItemRarity.UNREAL.colour,
                    FireworkEffect.Type.BALL,
                    false
                )

                y += if (y >= 2.0) 0.1 else 0.05
                radius += if (radius >= 1.5) 0.08 else 0.15

                if (y >= 25) cancel()
                i++
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    private fun shinyEffect(item: Item) {
        Bukkit.getServer().playSound(Sounds.SHINY_CATCH)
        object : BukkitRunnable() {
            var i = 0
            override fun run() {
                if(i % 2 == 0) {
                    item.location.world.spawnParticle(
                        Particle.ELECTRIC_SPARK,
                        item.location.clone().add(0.0, 0.5, 0.0),
                        10, 0.25, 0.25, 0.25, 0.0
                    )
                }
                if(i >= 40 || item.isDead) {
                    cancel()
                }
                i++
            }
        }.runTaskTimer(plugin, 0L, 5L)
    }

    private fun firework(
        location: Location,
        flicker: Boolean,
        trail: Boolean,
        color: Color,
        fireworkType: FireworkEffect.Type,
        variedVelocity: Boolean
    ) {
        val f: Firework = location.world.spawn(
            Location(location.world, location.x, location.y + 1.0, location.z),
            Firework::class.java
        )
        f.addScoreboardTag("tbd.fishing.firework")
        val fm = f.fireworkMeta
        fm.addEffect(
            FireworkEffect.builder()
                .flicker(flicker)
                .trail(trail)
                .with(fireworkType)
                .withColor(color)
                .build()
        )
        if (variedVelocity) {
            fm.power = 1
            f.fireworkMeta = fm
            val direction = Vector(
                Random.nextDouble(-0.005, 0.005),
                Random.nextDouble(0.25, 0.35),
                Random.nextDouble(-0.005, 0.005)
            ).normalize()
            f.velocity = direction
        } else {
            fm.power = 0
            f.fireworkMeta = fm
            f.ticksToDetonate = 1
        }
    }
}