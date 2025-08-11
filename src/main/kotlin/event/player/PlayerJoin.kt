package event.player

import Config
import ResourcePack
import chat.Formatting
import command.LiveUtil
import logger
import lore.GhostMode
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import util.sha1
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


class PlayerJoin : Listener {
    private val mm = MiniMessage.miniMessage()
    val resourcePacks = mutableListOf<ResourcePackInfo>()

    constructor(config: Config) {
        loadResourcePacks(config.resourcePacks)
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        sendTabList(e.player)

        val resourcePackRequest = ResourcePackRequest.resourcePackRequest()
            .packs(resourcePacks)
            .prompt(mm.deserialize("<gradient:#ff77a8:#ff510c>Please download the required resource packs for TBD</gradient>"))
            .build()

        e.player.sendResourcePacks(resourcePackRequest)
        e.player.sendMessage(mm.deserialize("<red>⚠ <reset>Please <b>do not</b> break loot chests!"))
        if(e.player.name == "Byrtrum") {
            e.joinMessage(null)
            GhostMode.toggleGhostMode(e.player)
        } else {
            e.joinMessage(Formatting.allTags.deserialize("<dark_gray>[<green>+<dark_gray>] <tbdcolour>${e.player.name}<reset> joined the game."))
        }

        if(LiveUtil.isLive(e.player)) {
            e.player.sendMessage("Live mode enabled.")
        }
    }

    private fun sendTabList(audience: Audience) {
        audience.sendPlayerListHeader(mm.deserialize("<newline><newline><newline><newline><newline>     \uF015    <newline>"))
        audience.sendPlayerListFooter(mm.deserialize("<newline><gradient:#ff510c:#ff77a8>  Welcome to TBD Season 4!  <newline>"))
    }

    private fun loadResourcePacks(configPacks: List<ResourcePack>) {
        configPacks.sortedBy { it.priority }.forEach {
            if (it.hash.isEmpty()) {
                logger.info("Hash missing for ${it.uri}, downloading...")
                val hash = getHashForUri(it.uri)
                logger.info("Calculated hash $hash for pack ${it.uri}")
                resourcePacks.add(
                    ResourcePackInfo.resourcePackInfo()
                        .uri(it.uri)
                        .hash(hash)
                        .build()
                )
            } else {
                resourcePacks.add(
                    ResourcePackInfo.resourcePackInfo()
                        .uri(it.uri)
                        .hash(it.hash)
                        .build()
                )
            }
        }
    }

    private fun getHashForUri(packURI: URI): String {
        val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
        val request = HttpRequest.newBuilder(packURI).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())

        if (response.statusCode() != 200) throw RuntimeException("Resourcepack download $packURI failed")

        return sha1(response.body())
    }

}
