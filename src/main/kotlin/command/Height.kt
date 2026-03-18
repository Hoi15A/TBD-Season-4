package command

import chat.Formatting
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class Height {

    @Command("height <cm>")
    @CommandDescription("Set your in-game scale based on your real-life height in cm.")
    @Permission("tbd.command.height")
    fun heightSelf(css: CommandSourceStack, @Argument("cm") cm: Int) {
        val player = css.sender as? Player ?: return
        applyHeight(player, cm, player)
    }

    @Command("height <cm> <player>")
    @CommandDescription("Set another player's in-game scale based on a height in cm.")
    @Permission("tbd.command.height.others")
    fun heightOther(css: CommandSourceStack, @Argument("cm") cm: Int, @Argument("player") target: Player) {
        val sender = css.sender as? Player ?: return
        applyHeight(sender, cm, target)
    }

    private fun applyHeight(sender: Player, cm: Int, target: Player) {
        if (cm !in 120..250) {
            sender.sendMessage(Formatting.allTags.deserialize("<red>Height must be between 120 and 250 cm."))
            return
        }

        val scale = cm / 180.0
        val attribute = target.getAttribute(Attribute.SCALE) ?: return
        attribute.baseValue = scale

        target.sendMessage(Formatting.allTags.deserialize("<tbdcolour>Your height has been set to <white>${cm}cm<tbdcolour>."))
        if (sender != target) {
            sender.sendMessage(Formatting.allTags.deserialize("<tbdcolour>Set <white>${target.name}<tbdcolour>'s height to <white>${cm}cm<tbdcolour>."))
        }
    }
}
