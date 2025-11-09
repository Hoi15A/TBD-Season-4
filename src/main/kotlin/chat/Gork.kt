package chat

import chat.Formatting.allTags
import command.TrueEyePrinter
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import plugin

object Gork {
    private var shouldGorkSpawnEye = true
    private val triggerRegex = Regex("@g(\\w\\w)k")
    private val prefix = allTags.deserialize("<skull:Chest> <tbdcolour>Gork<white>: ")
    private val responses = listOf( // todo: config rework, move to config
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
        "Hell yeah!",
        "There is not a doubt in my mind",
        "That's actually my favourite thing to do",
        "good juju",
        "YOLO",
        // No
        "Nope",
        "Unlikely",
        "Nah",
        "Doesn't seem so",
        "My sources say no",
        "big yikes, that's a no",
        "That's cringe",
        "<player> are you even thinking? Absolutely not",
        "Computer says no",
        "Don't know about that one mate",
        "erm actually <player>, that is factually incorrect <skull:MHF_Skeleton>",
        "bad juju",
        // Unsure / idek
        "As an Al language model, I have been trained to generate responses that are intended to be helpful, informative, and objective...",
        "I’m not permitted to comment",
        "That request falls outside my guidelines",
        "I’m obliged to stay silent on this",
        "Who asked?",
        "You know <player>, that's an interesting take",
        "bruh <skull:MHF_Zombie>",
        "According to all known laws of aviation, <player> should not be able to fly",
        "Honestly, I don't care",
        "ok garmin",
        "uwu <player>"
    )

    fun handleChatEvent(e: AsyncChatEvent) {
        val plainMessage = plainText().serialize(e.message())
        if (!triggerRegex.containsMatchIn(plainMessage)) return

        object : BukkitRunnable() {
            override fun run() {
                val response = responses.random()

                if(response == "ok garmin" && shouldGorkSpawnEye && (0..4).random() == 0) {
                    shouldGorkSpawnEye = false

                    object : BukkitRunnable() {
                        override fun run() {
                            Bukkit.getServer().sendMessage(prefix.append(allTags.deserialize("true eye spawnen",
                                Placeholder.unparsed("player", e.player.name))))

                            object : BukkitRunnable() {
                                override fun run() {
                                    TrueEyePrinter.printEye(e.player, e.player.location.add(0.0, 1.0, 0.0), "<i><gray>Obtained from Gork.")
                                }
                            }.runTaskLater(plugin, (30L..70L).random())

                        }
                    }.runTaskLater(plugin, (100L..300L).random())
                }
                /** Send Gork parsed message **/
                Bukkit.getServer().sendMessage(prefix.append(allTags.deserialize(response, Placeholder.unparsed("player", e.player.name))))
            }
        }.runTaskLater(plugin, (10L..100L).random())
    }
}