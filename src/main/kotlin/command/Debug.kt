package command

import fishing.FishRarity
import fishing.Fishing

import io.papermc.paper.command.brigadier.CommandSourceStack
import item.SubRarity

import net.kyori.adventure.text.Component
import org.bukkit.GameMode

import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import plugin

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
}