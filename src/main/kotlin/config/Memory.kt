package config

import logger
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import plugin
import util.ui.MemoryFilter
import java.io.File

object Memory {
    private lateinit var loader: YamlConfigurationLoader

    fun init(dataFolder: File) {
        val memoriesFile = File(dataFolder, "memories.yml")
        if (!memoriesFile.exists()) {
            plugin.getResource("memories.yml")!!.use { input ->
                memoriesFile.outputStream().use { input.copyTo(it) }
            }
        }
        loader = YamlConfigurationLoader.builder()
            .file(memoriesFile)
            .defaultOptions { options ->
                options.serializers { builder ->
                    builder.register(ItemStack::class.java, ItemStackSerializer)
                }
            }
            .build()
    }

    fun getMemories(filter: MemoryFilter?): List<ItemStack> {
        val key = (filter ?: MemoryFilter.SEASON_FOUR).memoryFilterConfigKey
        return loader.load().node(key).getList(ItemStack::class.java) ?: emptyList()
    }

    fun saveMemory(item: ItemStack, filter: MemoryFilter?) {
        if (item.type == Material.AIR) return
        val currentFilter = filter ?: MemoryFilter.SEASON_FOUR
        val key = currentFilter.memoryFilterConfigKey
        val node = loader.load()
        val current = node.node(key).getList(ItemStack::class.java) ?: emptyList()
        if (current.contains(item)) return
        node.node(key).setList(ItemStack::class.java, current + item)
        loader.save(node)
        logger.info("A memory has been saved to $currentFilter.")
    }
}