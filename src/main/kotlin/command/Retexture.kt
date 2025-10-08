package command

import chat.Formatting
import config
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.suggestion.Suggestions
import org.incendo.cloud.context.CommandContext

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class Retexture() {
    @Command("retexture <texture> [equippable]")
    @CommandDescription("Retexture item in hand.")
    @Permission("tbd.command.retexture")
    fun retexture(
        css: CommandSourceStack,
        @Argument(value = "texture", suggestions = "textureOptions") texture: String,
        @Argument(value = "equippable") @Default("false") equippable: Boolean
    ) {
        val player = css.sender as? Player ?: return
        val item = player.inventory.itemInMainHand

        if (texture !in config.textureOptions) {
            player.sendMessage(Formatting.allTags.deserialize("<red>Invalid texture option."))
            player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>Valid options: ${config.textureOptions.joinToString(", ")}"))
            return
        }
        if (item.type.isAir) {
            player.sendMessage(Formatting.allTags.deserialize("<red>You must be holding an item!"))
            return
        }
        val meta = item.itemMeta ?: run {
            player.sendMessage(Formatting.allTags.deserialize("<red>This item cannot be retextured!"))
            return
        }
        val key = NamespacedKey("tbdsmp", texture)
        meta.itemModel = key
        if (equippable) {
            val equippable = ItemStack.of(Material.LEATHER_HELMET).itemMeta.equippable
            meta.setEquippable(equippable)
        }
        item.itemMeta = meta
        player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>Applied texture: $texture"))
    }

    @Command("retexture reset")
    @CommandDescription("Reset item texture to default.")
    @Permission("tbd.command.retexture")
    fun reset(css: CommandSourceStack) {
        val player = css.sender as? Player ?: return
        val item = player.inventory.itemInMainHand

        if (item.type.isAir) {
            player.sendMessage(Formatting.allTags.deserialize("<red>You must be holding an item!"))
            return
        }

        val meta = item.itemMeta ?: run {
            player.sendMessage(Formatting.allTags.deserialize("<red>This item cannot be reset!"))
            return
        }

        meta.setEquippable(null)
        meta.itemModel = null
        item.itemMeta = meta

        player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>Item texture reset to default."))
    }

    @Suggestions("textureOptions")
    fun textureOptions(
        context: CommandContext<CommandSourceStack>,
        input: String
    ): List<String> {
        return config.textureOptions
    }
}