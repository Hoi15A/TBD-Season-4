package command

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import util.ui.TBDInterface
import util.ui.TBDInterfaceType

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class Island {
    @Command("exchange")
    @Permission("tbd.command.exchange")
    fun debugTreasureBag(css: CommandSourceStack) {
        if(css.sender is Player) {
            TBDInterface(css.sender as Player, TBDInterfaceType.ISLAND_EXCHANGE)
        }
    }
}