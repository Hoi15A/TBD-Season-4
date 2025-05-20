package chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import util.Noxesium

object Formatting {
    /** Prefix enum for allowing MiniMessage usage of the <prefix:NAME> tag in messages. **/
    enum class Prefix(val prefixName: String, val value: String) {
        NO_PREFIX("", ""),
        DEV_PREFIX("dev", "\uD001"),
        ADMIN_PREFIX("admin", "\uD002"),
        WARNING_PREFIX("warning", "⚠"),
        SKULL_PREFIX("skull", "☠");

        companion object {
            fun ofName(str : String): Prefix {
                for(p in entries) {
                    if (p.prefixName == str) return p
                }
                return NO_PREFIX
            }
        }
    }

    private val TBD_COLOUR = TagResolver.resolver("tbdcolour", Tag.styling(TextColor.color(255, 156, 237)))
    private val NOTIFICATION_COLOUR = TagResolver.resolver("notifcolour", Tag.styling(TextColor.color(219, 0, 96)))

    val allTags = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.defaults())
                .resolver(Noxesium.skullResolver())
                .resolver(TBD_COLOUR)
                .resolver(NOTIFICATION_COLOUR)
                .resolver(prefix())
                .build()
        )
        .build()

    val restrictedTags = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.color())
                .resolver(StandardTags.decorations())
                .resolver(StandardTags.reset())
                .resolver(StandardTags.rainbow())
                .resolver(StandardTags.pride())
                .resolver(StandardTags.gradient())
                .resolver(StandardTags.shadowColor())
                .resolver(Noxesium.skullResolver())
                .resolver(TBD_COLOUR)
                .build()
        )
        .build()

    val restrictedNoSkullTags = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.color())
                .resolver(StandardTags.decorations())
                .resolver(StandardTags.reset())
                .resolver(StandardTags.rainbow())
                .resolver(StandardTags.pride())
                .resolver(StandardTags.gradient())
                .resolver(StandardTags.shadowColor())
                .resolver(TBD_COLOUR)
                .build()
        )
        .build()

    /** Builds a prefix tag. **/
    private fun prefix() : TagResolver {
        return TagResolver.resolver("prefix") { args, _ ->
            val prefixName = args.popOr("Name not supplied.")
            Tag.inserting(
                Component.text(Prefix.ofName(prefixName.toString()).value)
            )
        }
    }

    val DIVINE_DEATH_MESSAGES = listOf(
        "%s's existence was repurposed",
        "%s had their existence forfeit"
    )
}