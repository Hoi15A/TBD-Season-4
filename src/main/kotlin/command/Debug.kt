package command

import chat.Formatting
import fishing.FishRarity
import fishing.Fishing
import io.papermc.paper.command.brigadier.CommandSourceStack
import item.SubRarity
import item.treasurebag.BagType
import item.treasurebag.TreasureBag
import logger
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import plugin
import util.Keys
import util.isHoldingItemInMainHand

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class Debug {
    @Command("debug catch <rarity> <subrarity>")
    @Permission("tbd.command.debug")
    fun debug(css: CommandSourceStack, @Argument("rarity") rarity: FishRarity, @Argument("subrarity") subrarity: SubRarity) {
        if(css.sender is Player) {
            val player = css.sender as Player
            if(player.gameMode == GameMode.CREATIVE) {
                css.sender.sendMessage(Component.text("Simulating catch of rarity $rarity"))
                val loc = player.location
                object : BukkitRunnable() {
                    override fun run() {
                        val item = loc.world.spawn(loc, Item::class.java)
                        item.itemStack = ItemStack(Material.BEEF, 1)
                        Fishing.catchFish(player, item, item.location, rarity, subrarity)
                    }
                }.runTaskLater(plugin, 100L)
            }
        }
    }

    @Command("debug simulate <count>")
    @Permission("tbd.command.debug")
    fun debugSimulateCatches(css: CommandSourceStack, @Argument("count") count: Int) {
        val catches = mutableListOf<FishRarity>()
        for (i in 0..count) {
            catches.add(FishRarity.getRandomRarity())
        }
        logger.info("RARITY SIMULATION RESULTS:")
        for(rarity in FishRarity.entries) {
            logger.info("${rarity.name}: ${catches.filter { r -> r == rarity }.size}")
        }

        val catchesSR = mutableListOf<SubRarity>()
        for (i in 0..count) {
            catchesSR.add(SubRarity.getRandomSubRarity())
        }
        logger.info("SUB RARITY SIMULATION RESULTS:")
        for(rarity in SubRarity.entries) {
            logger.info("${rarity.name}: ${catchesSR.filter { r -> r == rarity }.size}")
        }
    }

    @Command("debug treasure_bag <type>")
    @Permission("tbd.command.debug")
    fun debugTreasureBag(css: CommandSourceStack, @Argument("type") type: BagType) {
        if(css.sender is Player) {
            val player = css.sender as Player
            player.inventory.addItem(TreasureBag.create(type))
        }
    }

    @Command("debug migrateMemento")
    @Permission("tbd.command.debug")
    fun migrateMemento(css: CommandSourceStack) {
        val player = css.sender as Player
        if(!player.isHoldingItemInMainHand()) {
            player.sendMessage(Formatting.allTags.deserialize("<red>You need to be holding an item to migrate memento data.</red>"))
            return
        }

        val itemStack = player.inventory.itemInMainHand
        val legacyKey = NamespacedKey(plugin, "pdc.type.memento_type")

        if(!itemStack.persistentDataContainer.has(legacyKey)) {
            player.sendMessage(Formatting.allTags.deserialize("<red>This item does not have legacy memento data.</red>"))
            return
        }

        val value = itemStack.persistentDataContainer.get(legacyKey, PersistentDataType.STRING)!!
        itemStack.editPersistentDataContainer {
            it.remove(legacyKey)
            it.set(Keys.MEMENTO_TYPE, PersistentDataType.STRING, value)
        }
        player.sendMessage(Formatting.allTags.deserialize("<green>Memento data migrated successfully!</green>"))
    }
}