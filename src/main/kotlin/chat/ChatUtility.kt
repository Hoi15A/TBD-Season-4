package chat

import plugin
import chat.Formatting.allTags
import chat.Formatting.restrictedTags
import command.LiveUtil
import util.Sounds

import io.papermc.paper.chat.ChatRenderer

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.`object`.ObjectContents
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

import org.bukkit.Bukkit
import org.bukkit.GameEvent
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation

import org.joml.Vector3f

object ChatUtility {
    /** Sends a message to the specified audience. **/
    fun messageAudience(recipient: Audience, message: String, restricted: Boolean, vararg placeholders: TagResolver) {
        val resolvers = mutableListOf<TagResolver>()
        for(p in placeholders) {
            resolvers.add(p)
        }

        recipient.sendMessage(formatMessage(message, restricted, TagResolver.resolver(resolvers)))
    }

    /** Formats a message, which can produce different results depending on if restricted or not. **/
    fun formatMessage(message: String, restricted: Boolean, vararg placeholders: TagResolver): Component {
        val resolvers = mutableListOf<TagResolver>()
        for(p in placeholders) {
            resolvers.add(p)
        }

        return if (restricted) {
            restrictedTags.deserialize(message, TagResolver.resolver(resolvers))
        } else {
            allTags.deserialize(message, TagResolver.resolver(resolvers))
        }
    }

    /** Sends a message to the admin channel which includes all online admins. **/
    fun broadcastAdmin(rawMessage: String, isSilent: Boolean) {
        val admin = Audience.audience(Bukkit.getOnlinePlayers()).filterAudience { (it as Player).hasPermission("tbd.group.admin") }
        admin.sendMessage(allTags.deserialize("<gray>[<reset><prefix:admin><gray>]<reset> $rawMessage"))
        if(!isSilent) {
            admin.playSound(Sounds.ADMIN_MESSAGE)
        }
    }

    /** Sends a message to the dev channel which includes all online devs. **/
    fun broadcastDev(rawMessage: String, isSilent: Boolean) {
        val dev = Audience.audience(Bukkit.getOnlinePlayers()).filterAudience { (it as Player).hasPermission("tbd.group.dev") }
        dev.sendMessage(allTags.deserialize("<gray>[<reset><prefix:dev><gray>]<reset> $rawMessage"))
        if(!isSilent) {
            dev.playSound(Sounds.ADMIN_MESSAGE)
        }
    }
}

object GlobalRenderer : ChatRenderer {
    override fun render(source: Player, sourceDisplayName: Component, message: Component, viewer: Audience): Component {
        val playerHead = Component.`object`(ObjectContents.playerHead(source.uniqueId))
        val plainMessage = PlainTextComponentSerializer.plainText().serialize(message)
        return playerHead
            .append(Component.text(" "))
            .append(allTags.deserialize("${if(LiveUtil.livePlayers.contains(source.uniqueId)) "<prefix:live> " else ""}<tbdcolour>${if(source.name == "Byrtrum" || source.name == "fish_25") "<obfuscated>${source.name}" else source.name}<reset>: ")
                .append(if(source.hasPermission("tbd.group.admin")) allTags.deserialize(plainMessage) else restrictedTags.deserialize(plainMessage)))
    }
}

object VisualChat {
    const val VISUAL_CHAT_MESSAGE_ENTITY_LIFETIME = 120
    const val VISUAL_CHAT_MESSAGE_ENTITY_SHRINK_TIME = 80
    fun visualiseMessage(message: Component, sender: Player) {
        if(!sender.isInvisible) {
            clearChatEntities(sender)
            val parsedMessage = allTags.deserialize("<reset><bold>${PlainTextComponentSerializer.plainText().serialize(message)}")
            val messageEntity = sender.location.world.spawn(sender.eyeLocation, TextDisplay::class.java).apply {
                transformation = Transformation(Vector3f(0.0f, 1f, 0.0f), this.transformation.leftRotation, Vector3f(1f, 1f, 1f), this.transformation.rightRotation)
                alignment = TextDisplay.TextAlignment.CENTER
                billboard = Display.Billboard.CENTER
                teleportDuration = 2
                text(parsedMessage)
                addScoreboardTag("tbd.entity.visual_chat:${sender.uniqueId}")
            }
            sender.world.sendGameEvent(sender, GameEvent.EXPLODE, sender.location.toVector())
            object : BukkitRunnable() {
                var i = 0
                override fun run() {
                    if(!sender.isOnline || sender.isDead || i >= VISUAL_CHAT_MESSAGE_ENTITY_LIFETIME) {
                        messageEntity.remove()
                        this.cancel()
                    }
                    if(i >= VISUAL_CHAT_MESSAGE_ENTITY_SHRINK_TIME) {
                        messageEntity.transformation = Transformation(Vector3f(0.0f, 1f, 0.0f), messageEntity.transformation.leftRotation, messageEntity.transformation.scale.sub(0.025f, 0.025f, 0.025f), messageEntity.transformation.rightRotation)
                    }
                    messageEntity.teleport(sender.eyeLocation)
                    i++
                }
            }.runTaskTimer(plugin, 0L, 1L)
        }
    }

    private fun clearChatEntities(player: Player) {
        for(world in Bukkit.getWorlds()) {
            for(messageEntity in world.getEntitiesByClass(TextDisplay::class.java)) {
                if(messageEntity.scoreboardTags.contains("tbd.entity.visual_chat:${player.uniqueId}")) {
                    messageEntity.remove()
                }
            }
        }
    }

    fun clearChatEntities() {
        for(world in Bukkit.getWorlds()) {
            for(messageEntity in world.getEntitiesByClass(TextDisplay::class.java)) {
                for(tag in messageEntity.scoreboardTags) {
                    if(tag.contains("tbd.entity.visual_chat")) {
                        messageEntity.remove()
                    }
                }
            }
        }
    }
}
