package chat

import chat.Formatting.allTags
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import plugin

object Gork {
    val triggerRegex = Regex("@g(ro|or)k")
    val prefix = allTags.deserialize("<skull:Chest><tbdcolour>Gork<white>: ")
    val responses = listOf( // todo: config rework, move to config
        // Ideally the answers should make some amount of sense as an answer to "@grok is this true?"
        // Yes
        "Yep",
        "Probably",
        "Sure",
        "Looks like it",
        "Absolutely",
        "100% no cap",
        "Based",
        "<player> you are so right!",
        // No
        "Nope",
        "Unlikely",
        "Nah",
        "Doesn't seem so",
        "My sources say no",
        "big yikes, that's a no",
        "That's cringe",
        "<player> are you even thinking? Absolutely not",
        // Unsure / idek
        "As an Al language model, I have been trained to generate responses that are intended to be helpful, informative, and objective...",
        "I’m not permitted to comment",
        "That request falls outside my guidelines",
        "I’m obliged to stay silent on this",
        "Who asked?",
        "You know <player>, that's an interesting take",
    )

    fun handleChatEvent(e: AsyncChatEvent) {
        val plainMessage = plainText().serialize(e.message())
        if (!triggerRegex.containsMatchIn(plainMessage)) return


        object : BukkitRunnable() {
            override fun run() {
                Bukkit.getServer().sendMessage(prefix.append(allTags.deserialize(responses.random(),
                    Placeholder.unparsed("player", e.player.name))))
            }
        }.runTaskLater(plugin, (10L..100L).random())
    }
}