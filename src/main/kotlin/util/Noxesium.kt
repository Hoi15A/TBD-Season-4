package util

import logger

import org.bukkit.entity.Player

import java.util.*

object Noxesium {
    /** Map of online users playing with Noxesium installed, and the protocol of their version of Noxesium. **/
    private val noxesiumUsers = mutableMapOf<UUID, Int>()

    /** Adds the requested player with their respective protocol version of Noxesium to the noxesiumUsers map. **/
    fun addNoxesiumUser(player : Player, protocolVersion : Int) {
        logger.info("${player.name} passed Noxesium protocol $protocolVersion.")
        if(protocolVersion >= NOXESIUM_MINIMUM_PROTOCOL) {
            noxesiumUsers[player.uniqueId] = protocolVersion
            logger.info("Passed ${player.name} as a Noxesium user.")
        } else {
            logger.warning("Unable to pass ${player.name} as Noxesium user as their protocol version is $protocolVersion when it should be $NOXESIUM_MINIMUM_PROTOCOL or higher.")
        }
    }

    /** Removes the requested player from the noxesiumUsers map. **/
    fun removeNoxesiumUser(player : Player) {
        noxesiumUsers.remove(player.uniqueId)
        logger.info("Removed ${player.name} from Noxesium users.")
    }

    /** Returns the noxesiumUsers map. **/
    fun getNoxesiumUsers() : Map<UUID, Int> {
        return noxesiumUsers
    }

    /** Returns whether an online player is in the noxesiumUsers list. **/
    fun isNoxesiumUser(player : Player) : Boolean {
        return noxesiumUsers.containsKey(player.uniqueId)
    }
}

@Suppress("unused")
/** Channels are only server-bound. **/
enum class NoxesiumChannel(val channel : String) {
    NOXESIUM_V1_CLIENT_INFORMATION_CHANNEL("noxesium-v1:client_info"),
    NOXESIUM_V1_CLIENT_SETTINGS_CHANNEL("noxesium-v1:client_settings"),
    NOXESIUM_V2_CLIENT_INFORMATION_CHANNEL("noxesium-v2:client_info"),
    NOXESIUM_V2_CLIENT_SETTINGS_CHANNEL("noxesium-v2:client_settings")
}

/** Minimum protocol version required of the Noxesium mod. **/
const val NOXESIUM_MINIMUM_PROTOCOL = 2