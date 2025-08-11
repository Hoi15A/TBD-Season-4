import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import util.ui.MemoryFilter
import java.io.File
import java.net.URI

@ConfigSerializable
data class Config(
    val links: List<Link>,
    val resourcePacks: List<ResourcePack>
)

@ConfigSerializable
data class Link(val component: String, val uri: URI, val order: Int)

@ConfigSerializable
data class ResourcePack(val uri: URI, val hash: String, val priority: Int)


/** TODO: Memory config overhaul
 * Not part of the configurate model, will cause problems if events that receive the config per parameter from main
 * want to access it as it does not exist in the model. Also does not use configurate. Should be reworked into a
 * separate configurate memento config. Also missing default values in the resources default config.
 * https://github.com/SpongePowered/Configurate/wiki/Object-Mapper
 */
object Memory {
    private const val MEMORY_PATH = "memories"
    private val configFile = File(plugin.dataFolder, "config.yml")
    fun getMemories(filter: MemoryFilter?): List<ItemStack> {
        plugin.config.load(configFile)

        val memorySuffix = filter?.memoryFilterConfigSuffix ?: MemoryFilter.SEASON_FOUR.memoryFilterConfigSuffix
        val rawMemories = plugin.config.getList(MEMORY_PATH.plus(memorySuffix)) ?: emptyList()

        val loadedMemories = rawMemories.mapNotNull { it as? ItemStack }.toMutableList()

        if(loadedMemories.contains(ItemStack(Material.AIR).asOne())) {
            logger.warning("Memories contain illegal entries, please action.")
        }
        return loadedMemories
    }

    fun saveMemory(item: ItemStack, filter: MemoryFilter?) {
        if(item.type == Material.AIR) return
        val currentFilter = filter ?: MemoryFilter.SEASON_FOUR
        val memorySuffix = currentFilter.memoryFilterConfigSuffix
        val rawMemories = plugin.config.getList(MEMORY_PATH.plus(memorySuffix)) ?: emptyList()
        if(rawMemories.contains(item)) return
        val newMemoriesList = rawMemories + item
        plugin.config.set(MEMORY_PATH.plus(memorySuffix), newMemoriesList)
        plugin.config.save(configFile)
        logger.info("A memory has been saved to ${currentFilter}.")
    }
}
