package config

import org.bukkit.configuration.file.YamlConfiguration
import plugin
import java.io.File

object APIKeys {
    fun getIslandAPIKey(): String {
        val folder = plugin.dataFolder
        if (!folder.exists()) folder.mkdirs()
        val keysFile = File(folder, "api_keys.yml")
        if (!keysFile.exists()) keysFile.createNewFile()
        val keysFileConfiguration = YamlConfiguration.loadConfiguration(keysFile)
        if (keysFileConfiguration.get("island-api") == null) keysFileConfiguration.set("island-api", "INSERT_KEY_HERE")
        keysFileConfiguration.save(keysFile)
        return keysFileConfiguration.get("island-api") as String
    }
}