package command

import chat.Formatting
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import util.Keys.CURRENCY_HASH
import util.Sounds.ERROR_DIDGERIDOO
import util.Sounds.MINT_CURRENCY
import util.Sounds.PLING
import util.isHoldingItemInMainHand
import util.sha256

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class Currency {

    @Command("currency mint <secret>")
    @CommandDescription("Turn the held item into a forgery proof currency using a provided secret.")
    @Permission("tbd.command.currency.mint")
    fun mint(css: CommandSourceStack, secret: String) {
        val player = css.sender as? Player ?: return

        if (!player.isHoldingItemInMainHand()) {
            player.sendMessage(Formatting.allTags.deserialize("<red>Bro air is not a currency. Who are you, Nestlé?"))
            return
        }

        val itemMeta = player.inventory.itemInMainHand.itemMeta

        val existingHash = itemMeta.persistentDataContainer.get(CURRENCY_HASH, PersistentDataType.STRING)
        if (existingHash != null) {
            player.sendMessage(Formatting.allTags.deserialize("<red>Imagine taking a tenner, scribbling some new serial number on it, then bringing it to the central bank. This is what you are trying to do..."))
            return
        }

        val hash = sha256(secret.toByteArray())
        val shortHash = hash.take(16)


        val lore = itemMeta.lore() ?: mutableListOf()
        lore.add(Formatting.allTags.deserialize(CURRENCY_LORE, Placeholder.component("hash", Component.text(shortHash))))
        itemMeta.lore(lore)
        itemMeta.persistentDataContainer.set(CURRENCY_HASH, PersistentDataType.STRING, hash)
        player.inventory.itemInMainHand.setItemMeta(itemMeta)

        player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>Minted some currency!"))
        player.playSound(MINT_CURRENCY)
    }

    @Command("currency deface")
    @CommandDescription("Remove all currency data from the held item.")
    @Permission("tbd.command.currency.deface")
    fun deface(css: CommandSourceStack) {
        val player = css.sender as? Player ?: return

        if (!player.isHoldingItemInMainHand()) {
            player.sendMessage(Formatting.allTags.deserialize("<red>This dingus trying to rip up air right here..."))
            return
        }

        val itemMeta = player.inventory.itemInMainHand.itemMeta

        val existingHash = itemMeta.persistentDataContainer.get(CURRENCY_HASH, PersistentDataType.STRING)
        if (existingHash == null) {
            player.sendMessage(Formatting.allTags.deserialize("<red>This is not currency, might as well be some rocks."))
            return
        }

        val lore = itemMeta.lore()!!.filterNot { plainText().serialize(it).contains(LOCK_EMOJI) }
        itemMeta.lore(lore)
        itemMeta.persistentDataContainer.remove(CURRENCY_HASH)
        player.inventory.itemInMainHand.setItemMeta(itemMeta)

        player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>Defaced some currency!"))
        player.playSound(MINT_CURRENCY)
    }

    @Command("currency validate <secret>")
    @CommandDescription("Validate if the held item was minted using the provided secret.")
    @Permission("tbd.command.currency.validate")
    fun validate(css: CommandSourceStack, secret: String) {
        val player = css.sender as? Player ?: return

        if (!player.isHoldingItemInMainHand()) {
            player.sendMessage(Formatting.allTags.deserialize("<red>Yes, grab your magnifying glass and look at air Sherlock."))
            return
        }

        val itemMeta = player.inventory.itemInMainHand.itemMeta

        val existingHash = itemMeta.persistentDataContainer.get(CURRENCY_HASH, PersistentDataType.STRING)
        if (existingHash == null) {
            player.sendMessage(Formatting.allTags.deserialize("<red>Stanley was so bad at following directions it was incredible he wasn’t fired years ago."))
            return
        }
        val validationHash = sha256(secret.toByteArray())

        if (validationHash == existingHash) {
            player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>The currency <green>MATCHES <tbdcolour>the given secret."))
            player.playSound(PLING)
        } else {
            player.sendMessage(Formatting.allTags.deserialize("<tbdcolour>The currency <red>DOES NOT MATCH <tbdcolour>the given secret."))
            player.playSound(ERROR_DIDGERIDOO)
        }
    }

    companion object {
        private const val LOCK_EMOJI = "\uD83D\uDD12"
        private const val CURRENCY_LORE = "<i:false><#007550>$LOCK_EMOJI <gradient:#004f09:#63ad63>[ <font:illageralt><hash></font> ]"
    }
}
