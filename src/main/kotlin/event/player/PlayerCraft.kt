package event.player

import com.destroystokyo.paper.event.inventory.PrepareResultEvent
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.CartographyInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import plugin

class PlayerCraft : Listener {

    // This one is for crafting tables
    @EventHandler
    fun onPrepareItemCraft(event: PrepareItemCraftEvent) {
        val inventory = event.inventory
        val matrix = inventory.matrix

        // Check if this is a map duplication attempt
        if (isMapDuplicationAttempt(matrix)) {
            // Check if any filled map in the matrix is marked as uncopyable
            if (hasUncopyableMap(matrix)) {
                // Cancel the craft by setting result to air
                inventory.result = ItemStack(Material.AIR)

                // Optional: Send message to player
                (event.view.player as? Player)?.sendMessage("This map cannot be copied!")
            }
        }
    }

    // This one is for cartography tables
    @EventHandler
    fun onPrepareResult(event: PrepareResultEvent) {
        val inventory = event.inventory
        if (inventory is CartographyInventory) {
            val map = inventory.getItem(0)
            if (map != null && map.type == Material.FILLED_MAP) {
                val meta = map.itemMeta
                if (meta?.persistentDataContainer?.has(uncopyableKey, PersistentDataType.BOOLEAN) == true) {
                    event.result = ItemStack(Material.AIR)
                    (event.view.player as? Player)?.sendMessage("This map cannot be copied!")
                }
            }
        }
    }

    /**
     * Checks if the crafting matrix contains a map duplication recipe
     * (1 filled map + 1 empty map)
     */
    private fun isMapDuplicationAttempt(matrix: Array<out ItemStack?>): Boolean {
        val filledMaps = matrix.count { it?.type == Material.FILLED_MAP }
        val emptyMaps = matrix.count { it?.type == Material.MAP }

        return filledMaps == 1 && emptyMaps == 1
    }

    /**
     * Checks if any filled map in the crafting matrix is marked as uncopyable
     */
    private fun hasUncopyableMap(matrix: Array<out ItemStack?>): Boolean {
        return matrix.any { item ->
            item?.takeIf { it.type == Material.FILLED_MAP }
                ?.let { isMapUncopyable(it) }
                ?: false
        }
    }
    companion object {
        val uncopyableKey = NamespacedKey(plugin, "uncopyable")
        val uncopyablePlayerKey = NamespacedKey(plugin, "uncopyablePlayer")
        private const val LOCK_EMOJI = "\uD83D\uDD12"
        private const val LOCKED_LORE = "§c§l$LOCK_EMOJI UNCOPYABLE"

        fun markMapAsUncopyable(map: ItemStack, player: Player): Boolean {
            if (map.type != Material.FILLED_MAP) return false
            if (isMapUncopyable(map)) return false
            val puuid = player.uniqueId.toString()

            return map.editMeta { meta ->
                meta.persistentDataContainer.set(
                    uncopyableKey,
                    PersistentDataType.BOOLEAN,
                    true
                )
                meta.persistentDataContainer.set(
                    uncopyablePlayerKey,
                    PersistentDataType.STRING,
                    puuid
                )

                // Optional: Add visual indicator in lore
                val lore = meta.lore?.toMutableList() ?: mutableListOf()
                if (!lore.contains(LOCKED_LORE)) {
                    lore.add(LOCKED_LORE)
                    meta.lore = lore
                }
            }
        }

        fun removeUncopyableFlag(map: ItemStack, player: Player): Boolean {
            if (map.type != Material.FILLED_MAP) return false

            val puuid = player.uniqueId.toString()
            val existingPuuid = map.itemMeta?.persistentDataContainer?.get(uncopyablePlayerKey, PersistentDataType.STRING)
            if (existingPuuid != puuid) return false

            return map.editMeta { meta ->
                meta.persistentDataContainer.remove(uncopyableKey)
                meta.persistentDataContainer.remove(uncopyablePlayerKey)

                // Remove visual indicator from lore
                val lore = meta.lore?.toMutableList()
                lore?.remove(LOCKED_LORE)
                meta.lore = lore?.takeIf { it.isNotEmpty() }
            }
        }

        fun isMapUncopyable(map: ItemStack): Boolean {
            return map.itemMeta?.persistentDataContainer?.has(
                uncopyableKey,
                PersistentDataType.BOOLEAN
            ) ?: false
        }
    }
}