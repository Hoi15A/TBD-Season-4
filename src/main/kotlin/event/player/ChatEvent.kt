package event.player

import plugin
import chat.GlobalRenderer
import chat.Gork
import chat.VisualChat

import io.papermc.paper.event.player.AsyncChatEvent

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatEvent: Listener {
    @EventHandler
    private fun onChat(e: AsyncChatEvent) {
        e.renderer(GlobalRenderer)
        Bukkit.getScheduler().runTask(plugin, Runnable { VisualChat.visualiseMessage(e.message(), e.player) })
        Gork.handleChatEvent(e)
    }
}