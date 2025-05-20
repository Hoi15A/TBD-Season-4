package fishing

import chat.Formatting.allTags
import util.Keys.FISH_RARITY
import plugin
import item.ItemRarity
import item.ItemType
import item.SubRarity
import util.Sounds
import logger

import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title

import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import util.Keys.FISH_IS_OBFUSCATED
import util.Keys.FISH_IS_SHADOW
import util.Keys.FISH_IS_SHINY
import util.startsWithVowel

import java.time.Duration
import java.util.*

import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object Fishing {
    fun catchFish(
        player: Player,
        item: Item,
        location: Location,
        forcedFishRarity: FishRarity?,
        forcedFishSubRarity: SubRarity?
    ) {
        val fishRarity = forcedFishRarity ?: FishRarity.getRandomRarity()
        val subRarity = forcedFishSubRarity ?: SubRarity.getRandomSubRarity()

        val caughtByLore =
            if (fishRarity.props.showCatcher || subRarity != SubRarity.NULL) allTags.deserialize("<reset><white>Caught by <yellow>${player.name}<white>.").decoration(TextDecoration.ITALIC, false) else null
        val fishMeta = item.itemStack.itemMeta
        fishMeta.displayName(
            allTags.deserialize("<${fishRarity.itemRarity.colourHex}>${if(subRarity == SubRarity.OBFUSCATED) "<font:alt>" else ""}${item.name}").decoration(TextDecoration.ITALIC, false)
        )
        fishMeta.lore(
            if (caughtByLore == null) {
                listOf(
                    allTags.deserialize("<reset><white>${fishRarity.itemRarity.rarityGlyph}${ItemType.FISH.typeGlyph}")
                        .decoration(TextDecoration.ITALIC, false)
                )
            } else {
                listOf(
                    allTags.deserialize("<reset><white>${fishRarity.itemRarity.rarityGlyph}${if (subRarity != SubRarity.NULL) "<reset><white>${subRarity.subRarityGlyph}${ItemType.FISH.typeGlyph}" else "<reset><white>${ItemType.FISH.typeGlyph}"}")
                        .decoration(TextDecoration.ITALIC, false), caughtByLore
                )
            }
        )
        when(subRarity) {
            SubRarity.NULL -> {}
            SubRarity.SHINY -> {
                fishMeta.setEnchantmentGlintOverride(true)
                fishMeta.persistentDataContainer.set(FISH_IS_SHINY, PersistentDataType.BOOLEAN, true)
            }
            SubRarity.SHADOW -> {
                fishMeta.persistentDataContainer.set(FISH_IS_SHADOW, PersistentDataType.BOOLEAN, true)
            }
            SubRarity.OBFUSCATED -> {
                fishMeta.persistentDataContainer.set(FISH_IS_OBFUSCATED, PersistentDataType.BOOLEAN, true)
            }
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
        if (subRarity != SubRarity.NULL) logger.info("(FISHING) ${player.name} caught $subRarity ${item.name}.")
        if (subRarity == SubRarity.SHINY) shinyEffect(item)
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
                Bukkit.getServer().playSound(Sounds.TRANSCENDENT_CATCH)
                object : BukkitRunnable() {
                    val radius = 5.0
                    val vertices = generateVertices(radius)
                    val edges = getEdges()
                    var angle = 0.0
                    var time = 0
                    override fun run() {
                        if (time++ >= 300) cancel()
                        val rotated = vertices.map { rotateY(it, angle) }
                        rotated.forEach { point ->
                            location.world.spawnParticle(
                                Particle.DUST,
                                location.clone().add(point),
                                0,
                                Particle.DustOptions(Color.RED, 1.5f)
                            )
                        }
                        edges.forEach { (i, j) ->
                            drawEdge(location, rotated[i], rotated[j], 8, Color.RED)
                        }
                        angle += Math.toRadians(4.0)
                    }


                }.runTaskTimer(plugin, 0L, 2L)
            }
            FishRarity.CELESTIAL -> {
                Bukkit.getServer().playSound(Sounds.CELESTIAL_CATCH)
                firework(location, flicker = true, trail = true, fishRarity.itemRarity.colour, FireworkEffect.Type.BALL_LARGE, variedVelocity = false)
                val radius = 12.0
                val step = Math.PI / 16
                for (angle in 0 until 12) {
                    val x = radius * cos(angle * step)
                    val z = radius * sin(angle * step)
                    location.world.strikeLightningEffect(location.clone().add(x, 100.0, z))
                    location.world.spawnParticle(
                        Particle.FLASH,
                        location.clone().add(x, 100.0, z),
                        5, 0.0, 0.0, 0.0, 0.0, null, true
                    )
                    location.world.spawnParticle(
                        Particle.CLOUD,
                        location.clone().add(x, 100.0, z),
                        10, 0.0, 0.0, 0.0, 0.5, null, true
                    )
                }
                for(i in 0..39) {
                    object : BukkitRunnable() {
                        override fun run() {
                            val randomX = Random.nextDouble(location.x - 20.0, location.x + 20.0)
                            val randomZ = Random.nextDouble(location.z - 20.0, location.z + 20.0)
                            var height = location.blockY + 100
                            object : BukkitRunnable() {
                                override fun run() {
                                    if(height > location.blockY) {
                                        firework(Location(location.world, randomX, height.toDouble(), randomZ), flicker = false, trail = false, fishRarity.itemRarity.colour, FireworkEffect.Type.BALL, variedVelocity = false)
                                        height -= 2
                                    } else {
                                        location.world.playSound(Location(location.world, randomX, height.toDouble(), randomZ), "item.totem.use", 0.75f, 0.75f)
                                        location.world.spawnParticle(
                                            Particle.TOTEM_OF_UNDYING,
                                            Location(location.world, randomX, height.toDouble(), randomZ),
                                            250, 0.0, 0.0, 0.0, 0.75, null, true
                                        )
                                        cancel()
                                    }
                                }
                            }.runTaskTimer(plugin, 0L, 4L)
                        }
                    }.runTaskLater(plugin, 0L + (i.toLong() * 15))
                }
            }
            else -> { /* do nothing */ }
        }
    }

    // TRANSCENDENT DODECAHEDRON METHODS
    fun generateVertices(r: Double): List<Vector> {
        val phi = (1 + sqrt(5.0)) / 2
        val a = 1.0 / sqrt(3.0)
        val b = a / phi
        val c = a * phi

        return listOf(
            Vector( a,  a,  a), Vector( a,  a, -a), Vector( a, -a,  a), Vector( a, -a, -a),
            Vector(-a,  a,  a), Vector(-a,  a, -a), Vector(-a, -a,  a), Vector(-a, -a, -a),
            Vector( 0.0,  b,  c), Vector( 0.0,  b, -c), Vector( 0.0, -b,  c), Vector( 0.0, -b, -c),
            Vector( b,  c, 0.0), Vector( b, -c, 0.0), Vector(-b,  c, 0.0), Vector(-b, -c, 0.0),
            Vector( c, 0.0,  b), Vector( c, 0.0, -b), Vector(-c, 0.0,  b), Vector(-c, 0.0, -b)
        ).map { it.multiply(r) }
    }

    fun getEdges(): List<Pair<Int, Int>> = listOf(
        0 to 8, 0 to 12, 0 to 16, 1 to 9, 1 to 12, 1 to 17, 2 to 10, 2 to 13, 2 to 16,
        3 to 11, 3 to 13, 3 to 17, 4 to 8, 4 to 14, 4 to 18, 5 to 9, 5 to 14, 5 to 19,
        6 to 10, 6 to 15, 6 to 18, 7 to 11, 7 to 15, 7 to 19, 8 to 10, 9 to 11,
        12 to 14, 13 to 15, 16 to 17, 18 to 19
    )

    fun rotateY(v: Vector, angle: Double): Vector {
        val cos = cos(angle)
        val sin = sin(angle)
        return Vector(v.x * cos - v.z * sin, v.y, v.x * sin + v.z * cos)
    }

    fun drawEdge(origin: Location, start: Vector, end: Vector, steps: Int, color: Color) {
        val delta = end.clone().subtract(start).multiply(1.0 / steps)
        val world = origin.world
        for (i in 0..steps) {
            val point = start.clone().add(delta.clone().multiply(i))
            world.spawnParticle(
                Particle.DUST,
                origin.clone().add(point),
                0,
                Particle.DustOptions(color, 1.0f)
            )
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

    fun shinyEffect(item: Item) {
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
                if(i >= 100 || item.isDead) {
                    cancel()
                }
                i++
            }
        }.runTaskTimer(plugin, 0L, 5L)
    }

    fun firework(
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
        f.addScoreboardTag("tbd.firework")
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
            f.ticksToDetonate = 0
        }
    }
}