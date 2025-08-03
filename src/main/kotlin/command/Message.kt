package command

import chat.Formatting

import io.papermc.paper.command.brigadier.CommandSourceStack

import org.bukkit.Bukkit
import org.bukkit.entity.Player

import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.processing.CommandContainer
import util.Sounds

import java.util.*

private data class LastMessager(val sender: UUID, val timeStamp: Long)

@CommandContainer
@Suppress("unused", "unstableApiUsage")
class Message {
    private val lastConversationPartner = mutableMapOf<UUID, LastMessager>()
    /**
     * Command that allows players to send a direct message to another player.
     * Replacement for the vanilla /msg command in order to allow for replies to function.
     *
     * @param sender Player running the command
     * @param recipient Player that should receive the message
     * @param text The message that should be sent
     */
    @Command("msg|w|m|tell|explain|beg <player> <text>")
    @CommandDescription("Send somebody a private message.")
    fun msg(css: CommandSourceStack, @Argument("player") recipient: Player, @Argument("text") text: Array<String>) {
        if(css.sender is Player) {
            val sender = css.sender as Player
            if (sender == recipient) {
                System.currentTimeMillis() / 1000
                sender.sendMessage(Formatting.restrictedTags.deserialize("<i><tbdcolour>You<white> -> <yellow>Yourself</yellow>: ${text.joinToString(" ")}</i>"))
            } else {
                if(recipient.name == "Byrtrum") {
                    sender.sendMessage(Formatting.restrictedTags.deserialize("<i><tbdcolour>You<white> -> <yellow><obf>********</obf></yellow>: <dark_gray>Yo<obf>u</obf>r m<obf>e</obf>ssage w<obf>a</obf>s lo<obf>s</obf>t...</i>"))
                    sender.playSound(Sounds.ENDER_EYE_PLACE_FAIL_BACKGROUND)
                } else {
                    sendMessage(sender, recipient, text)
                }
            }
        }

    }

    /**
     * Command that allows the player to respond to previous messages, including their own.
     * Timestamps of the last player talked to are stored in [lastConversationPartner]
     *
     * @param sender Player running the command
     * @param text Message to reply with
     */
    @Command("r|reply <text>")
    @CommandDescription("Reply to the last private message you received.")
    fun reply(css: CommandSourceStack, @Argument("text") text: Array<String>) {
        if(css.sender is Player) {
            val sender = css.sender as Player
            val lastSender = lastConversationPartner[sender.uniqueId]
            if (lastSender != null && System.currentTimeMillis() - lastSender.timeStamp <= REPLY_TIMEOUT_SECONDS * 1000) {
                val offlinePlayer = Bukkit.getOfflinePlayer(lastSender.sender)
                if (offlinePlayer.isOnline) {
                    sendMessage(sender, offlinePlayer.player!!, text)
                } else {
                    sender.sendMessage(
                        Formatting.restrictedTags.deserialize("<i><gray>${offlinePlayer.name} is not online :pensive:</gray></i>")
                    )
                }
            } else {
                sender.sendMessage(
                    Formatting.restrictedTags.deserialize("<i><gray>Nobody has messaged you in a while, but don't worry we still love you <3 - TBD Admins</gray></i>")
                )
            }
        }

    }

    /**
     * Send a direct message from one player to another. Then also set [lastConversationPartner] for both players
     * in order for [reply] to function.
     *
     * @param sender Player sending the message
     * @param recipient Player receiving the message
     * @param text
     */
    private fun sendMessage(sender: Player, recipient: Player, text: Array<String>) {
        val message = text.joinToString(" ")

        if(sender.name == "Byrtrum") {
            sender.sendMessage(Formatting.restrictedTags.deserialize("<i><tbdcolour>You</tbdcolour> -> <yellow>${recipient.name}</yellow>: $message</i>"))
            recipient.sendMessage(Formatting.restrictedTags.deserialize("<i><yellow><obf>${sender.name}</obf></yellow> -> <tbdcolour>You</tbdcolour>: $message</i>"))
        } else {
            sender.sendMessage(Formatting.restrictedTags.deserialize("<i><tbdcolour>You</tbdcolour> -> <yellow>${recipient.name}</yellow>: $message</i>"))
            recipient.sendMessage(Formatting.restrictedTags.deserialize("<i><yellow>${sender.name}</yellow> -> <tbdcolour>You</tbdcolour>: $message</i>"))
        }

        lastConversationPartner[recipient.uniqueId] = LastMessager(sender.uniqueId, System.currentTimeMillis())
        lastConversationPartner[sender.uniqueId] = LastMessager(recipient.uniqueId, System.currentTimeMillis())
    }

    companion object {
        private const val REPLY_TIMEOUT_SECONDS = 60 * 60 * 1 // 1 Hour
    }
}