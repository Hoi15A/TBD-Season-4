package lore

import chat.Formatting
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Skeleton as MechHost
import org.bukkit.entity.Illusioner as MechShow
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.attribute.Attribute
import java.util.concurrent.ConcurrentHashMap
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.inventory.ItemStack
import plugin

object MannequinMech {
    val mannequinPairs = ConcurrentHashMap<Int, MannequinPair>()
    val bossBars = ConcurrentHashMap<Int, BossBar>()
    val bowEquippedFlags = ConcurrentHashMap<Int, Boolean>()
    var customMaxHealth : Double = 1000.0

    fun createMannequinMech(location: Location) {
        val mechHost = location.world.spawnEntity(location, EntityType.SKELETON) as MechHost
        mechHost.customName(Component.text("Sebs Mecha").color(NamedTextColor.DARK_RED))
        mechHost.apply {
            setAI(false)
            isInvisible = true
            isSilent = true
            isCustomNameVisible = false
            isInvulnerable = true
            isCollidable = false
            isPersistent = true
            setShouldBurnInDay(false)
            getAttribute(Attribute.ATTACK_DAMAGE)?.baseValue = 23.0
            getAttribute(Attribute.FOLLOW_RANGE)?.baseValue = 100.0
            getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue = 0.5
            getAttribute(Attribute.STEP_HEIGHT)?.baseValue = 5.0
            getAttribute(Attribute.WATER_MOVEMENT_EFFICIENCY)?.baseValue = 1.0
            getAttribute(Attribute.JUMP_STRENGTH)?.baseValue = 2.0

            equipment.setItemInMainHand(null)
        }
        val mechShow = location.world.spawnEntity(location, EntityType.ILLUSIONER) as MechShow
        mechShow.customName(Component.text("Sebs Mecha").color(NamedTextColor.DARK_RED))
        mechShow.apply {
            setAI(false)
            isCustomNameVisible = true
            isSilent = true
            isPersistent = true
            isInvulnerable = true
            getAttribute(Attribute.KNOCKBACK_RESISTANCE)?.baseValue = 1.0
            getAttribute(Attribute.ARMOR)?.baseValue = 8.0
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = customMaxHealth
            health = customMaxHealth
            equipment.setHelmet(null)
        }

        val bossBar = createBossBar(mechShow)
        bossBars[mechHost.entityId] = bossBar

        bowEquippedFlags[mechHost.entityId] = false

        val pair = MannequinPair(mechHost, mechShow, bossBar)
        mannequinPairs[mechHost.entityId] = pair

        startGrowthAnimation(pair)
        startSynchronization(pair)
    }

    private fun startGrowthAnimation(pair: MannequinPair) {
        val scales = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val delayBetweenGrowth = 40L // 2 seconds (20 ticks = 1 second)

        scales.forEachIndexed { index, scale ->
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                // Check if entities are still valid
                if (!pair.mechHost.isValid || !pair.mechShow.isValid) {
                    return@Runnable
                }

                // Update scale for both entities
                pair.mechHost.getAttribute(Attribute.SCALE)?.baseValue = scale
                pair.mechShow.getAttribute(Attribute.SCALE)?.baseValue = scale

                // Optional: Play sound effect
                pair.mechShow.world.playSound(
                    pair.mechShow.location,
                    org.bukkit.Sound.BLOCK_ANVIL_LAND,
                    1.0f,
                    0.5f + (index * 0.2f) // Pitch increases with each growth
                )

                // Optional: Spawn particles
                pair.mechShow.world.spawnParticle(
                    org.bukkit.Particle.EXPLOSION,
                    pair.mechShow.location.add(0.0, scale / 2, 0.0),
                    5,
                    0.5, 0.5, 0.5
                )

                // Optional: Send message on final growth
                if (index == scales.size - 1) {
                    pair.bossBar.players.forEach { player ->
                        player.sendMessage(
                            Formatting.allTags.deserialize("<red><bold>Sebs Mecha has reached full size!")
                        )
                    }
                    pair.mechHost.getAttribute(Attribute.SCALE)?.baseValue = 4.0
                    pair.mechShow.isInvulnerable = false
                    pair.mechHost.setAI(true)

                    pair.bossBar.players.forEach { player -> // Delete after Senate Meeting
                        if (player.name == "Sebiann") {
                            pair.mechHost.addPassenger(player)
                        }
                    }
                }
            }, delayBetweenGrowth * index)
        }
    }

    private fun startSynchronization(pair: MannequinPair) {
        object : BukkitRunnable() {
            override fun run() {
                if (!pair.mechHost.isValid || !pair.mechShow.isValid) {
                    // Cleanup if either entity is gone
                    mannequinPairs.remove(pair.mechHost.entityId)
                    pair.cleanup()
                    cancel()
                    return
                }
                val mechHostLocation = pair.mechHost.location
                val mannequinLocation = pair.mechShow.location

                // Calculate health percentage
                val maxHealth = pair.mechShow.getAttribute(Attribute.MAX_HEALTH)?.value ?: customMaxHealth
                val currentHealth = pair.mechShow.health
                val healthPercentage = (currentHealth / maxHealth) * 100

                // Check if health is at or below 50% and bow hasn't been equipped yet
                if (healthPercentage <= 50.0 && bowEquippedFlags[pair.mechHost.entityId] == false) {
                    equipBowPhase(pair)
                    bowEquippedFlags[pair.mechHost.entityId] = true
                    pair.bossBar.setTitle("Sebs Mecha - RANGED MODE")
                } else {
                    if (bowEquippedFlags[pair.mechHost.entityId] == false) {
                        pair.bossBar.setTitle("Sebs Mecha - MELEE MODE")
                    }
                }

                // Only teleport if positions are significantly different
                if (mechHostLocation.distance(mannequinLocation) > 0.1) {
                    try {
                        pair.mechShow.teleport(mechHostLocation)
                    } catch (e: Exception) {
                        plugin.logger.warning("Teleport error: ${e.message}")
                    }
                }
                updateBossBar(pair)
            }
        }.runTaskTimer(plugin, 1L, 1L)
    }

    private fun equipBowPhase(pair: MannequinPair) {
        val mechHost = pair.mechHost

        // Create enchanted bow
        val bow = ItemStack(Material.BOW)
        val bowMeta = bow.itemMeta

        // Add enchantments for more challenge
        bowMeta.addEnchant(org.bukkit.enchantments.Enchantment.POWER, 50, true)
        bowMeta.addEnchant(org.bukkit.enchantments.Enchantment.FLAME, 1, true)
        bowMeta.addEnchant(org.bukkit.enchantments.Enchantment.INFINITY, 1, true)
        bowMeta.addEnchant(org.bukkit.enchantments.Enchantment.MULTISHOT, 1, true)
        bowMeta.addEnchant(org.bukkit.enchantments.Enchantment.QUICK_CHARGE, 5, true)
        bowMeta.addEnchant(org.bukkit.enchantments.Enchantment.PUNCH, 5, true)

        // Optional: Custom name
        bowMeta.displayName(Component.text("Mecha Cannon").color(NamedTextColor.RED))

        bow.itemMeta = bowMeta

        // Equip the bow
        mechHost.equipment.setItemInMainHand(bow)
        mechHost.equipment.itemInMainHandDropChance = 0.0f // Don't drop on death

        // Announce phase change to nearby players
        pair.bossBar.players.forEach { player ->
            player.sendMessage(
                Formatting.allTags.deserialize("<red><bold>⚠ Sebs Mecha has entered RANGED MODE! ⚠")
            )
            player.playSound(player.location, org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f)
        }
        // Optional: Increase movement speed for ranged combat
        mechHost.getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue = 0.6
    }

    private fun updateBossBar(pair: MannequinPair) {
        val bossBar = pair.bossBar
        val mannequin = pair.mechShow

        // Update progress based on health
        val maxHealth = mannequin.getAttribute(Attribute.MAX_HEALTH)?.value ?: customMaxHealth
        val currentHealth = mannequin.health
        val progress = (currentHealth / maxHealth).coerceIn(0.0, 1.0)

        bossBar.progress = progress

        // Change color based on health percentage
        bossBar.color = when {
            progress > 0.6 -> BarColor.GREEN
            progress > 0.3 -> BarColor.YELLOW
            else -> BarColor.RED
        }

        // Manage player visibility based on distance
        managePlayerVisibility(pair)
    }

    private fun managePlayerVisibility(pair: MannequinPair) {
        val bossBar = pair.bossBar
        val mannequinLocation = pair.mechShow.location
        val viewDistance = 50.0 // Distance at which players can see the boss bar

        Bukkit.getOnlinePlayers().forEach { player ->
            val distance = player.location.distance(mannequinLocation)
            val isPlayerAdded = bossBar.players.contains(player)

            when {
                distance <= viewDistance && !isPlayerAdded -> {
                    bossBar.addPlayer(player)
                }
                distance > viewDistance && isPlayerAdded -> {
                    bossBar.removePlayer(player)
                }
            }
        }
    }

    private fun createBossBar(mechShow: MechShow): BossBar {
        val bossBar = Bukkit.createBossBar(
            "Sebs Mecha", // Title
            BarColor.RED,  // Color
            BarStyle.SOLID // Style<
        )
        bossBar.progress = 1.0

        Bukkit.getOnlinePlayers().forEach { player ->
            if (player.location.distance(mechShow.location) <= 50.0) {
                bossBar.addPlayer(player)
            }
        }

        return bossBar
    }

    fun removeAllMannequins() {
        mannequinPairs.values.forEach { it.cleanup() }
        mannequinPairs.clear()
        bossBars.values.forEach { it.removeAll() }
        bossBars.clear()
    }

    fun removeMannequin(mechHostId: Int): Boolean {
        return mannequinPairs.remove(mechHostId)?.let { pair ->
            pair.cleanup()
            bossBars.remove(mechHostId)
            true
        } ?: false
    }

    // Data class for host-mannequin pairs
    data class MannequinPair(
        val mechHost: MechHost,
        val mechShow: MechShow,
        val bossBar: BossBar
    ) {
        fun cleanup() {
            if (mechHost.isValid) mechHost.remove()
            if (mechShow.isValid) mechShow.remove()
            bossBar.removeAll() // Remove boss bar from all players
        }
    }
}