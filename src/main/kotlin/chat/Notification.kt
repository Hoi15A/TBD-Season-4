package chat

import chat.Formatting.allTags
import util.Sounds
import logger
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.Title.Times

import org.bukkit.Bukkit

import java.time.Duration

object Notification {
    /** Sends an announcement to all online players. **/
    fun announceServer(
        title: String, subtitle: String, sound: Sound = Sounds.SERVER_ANNOUNCEMENT, times: Times = Times.times(
            Duration.ofSeconds(1.toLong()),
            Duration.ofSeconds(6.toLong()),
            Duration.ofSeconds(1.toLong())
        )
    ) {
        logger.info("Announcement: $subtitle")

        val online = Audience.audience(Bukkit.getOnlinePlayers())
        online.sendMessage(allTags.deserialize("<newline>$title: $subtitle<newline>"))
        online.playSound(sound)
        online.showTitle(
            Title.title(
                allTags.deserialize(title),
                allTags.deserialize(subtitle),
                times
            )
        )
    }

    fun announceChat(message: String) {
        val online = Audience.audience(Bukkit.getOnlinePlayers())
        online.sendMessage(allTags.deserialize(message).color(TextColor.fromHexString("#32FF82")))
        online.playSound(Sounds.LORE_ANNOUNCEMENT)
    }
}