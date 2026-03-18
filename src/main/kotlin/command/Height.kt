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

    companion object {
        const val MIN_HEIGHT = 120
        const val MAX_HEIGHT = 250
        const val DEFAULT_HEIGHT = 180
    }

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

    @Command("height reset")
    @CommandDescription("Reset your in-game scale to default.")
    @Permission("tbd.command.height")
    fun heightResetSelf(css: CommandSourceStack) {
        val player = css.sender as? Player ?: return
        applyHeight(player, DEFAULT_HEIGHT, player)
    }

    @Command("height reset <player>")
    @CommandDescription("Reset another player's in-game scale to default.")
    @Permission("tbd.command.height.others")
    fun heightResetOther(css: CommandSourceStack, @Argument("player") target: Player) {
        val sender = css.sender as? Player ?: return
        applyHeight(sender, DEFAULT_HEIGHT, target)
    }

    private fun applyHeight(sender: Player, cm: Int, target: Player) {
        if (cm !in MIN_HEIGHT..MAX_HEIGHT) {
            sender.sendMessage(Formatting.allTags.deserialize("<red>Height must be between $MIN_HEIGHT and $MAX_HEIGHT cm."))
            return
        }

        val scale = cm / DEFAULT_HEIGHT.toDouble()
        val attribute = target.getAttribute(Attribute.SCALE) ?: return
        attribute.baseValue = scale

        target.sendMessage(Formatting.allTags.deserialize("<tbdcolour>Your height has been set to <white>${cm}cm<tbdcolour>."))
        if (sender != target) {
            sender.sendMessage(Formatting.allTags.deserialize("<tbdcolour>Set <white>${target.name}<tbdcolour>'s height to <white>${cm}cm<tbdcolour>."))
        }
    }
}
