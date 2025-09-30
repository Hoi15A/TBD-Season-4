import chat.VisualChat
import com.noxcrew.interfaces.InterfacesListeners
import event.block.*
import event.entity.*
import event.player.*
import io.papermc.paper.command.brigadier.CommandSourceStack
import lore.Divinity
import lore.MannequinMech
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import util.NoxesiumChannel
import util.messenger.BrandMessenger
import util.messenger.NoxesiumMessenger
import java.io.File

@Suppress( "unstableApiUsage")
class SeasonFourPlugin : JavaPlugin() {
    private lateinit var commandManager: PaperCommandManager<CommandSourceStack>
    private lateinit var config: Config

    override fun onEnable() {
        this.logger.info("We are so back.")
        readConfig()
        setupEvents()
        registerCommands()
        registerMessengers()
        VisualChat.clearChatEntities()
        InterfacesListeners.install(plugin)
        MannequinMech.removeAllMannequins()
    }

    override fun onDisable() {
        this.logger.info("It is so over.")
        Bukkit.getServer().scoreboardManager.mainScoreboard.teams.forEach { team -> if(team.name.contains("tbd.true_eye.")) team.unregister() }
        VisualChat.clearChatEntities()
        Divinity.clearChains()
        MannequinMech.removeAllMannequins()
    }

    private fun setupEvents() {
        server.pluginManager.registerEvents(PlayerFish(), this)
        server.pluginManager.registerEvents(PlayerJoin(config), this)
        server.pluginManager.registerEvents(PlayerQuit(), this)
        server.pluginManager.registerEvents(ChatEvent(), this)
        server.pluginManager.registerEvents(FurnaceSmelt(), this)
        server.pluginManager.registerEvents(PlayerInteract(), this)
        server.pluginManager.registerEvents(PlayerInteractEntity(), this)
        server.pluginManager.registerEvents(DeathEvent(), this)
        server.pluginManager.registerEvents(DamageEvent(), this)
        server.pluginManager.registerEvents(EnderEyeInteract(), this)
        server.pluginManager.registerEvents(PlayerItemConsume(), this)
        server.pluginManager.registerEvents(PlayerMovement(), this)
        server.pluginManager.registerEvents(PortalFrameInteract(), this)
        server.pluginManager.registerEvents(DragonDeathEvent(), this)
        server.pluginManager.registerEvents(PrepareAnvilListener(), this)
        server.pluginManager.registerEvents(BlockPlace(), this)
        server.pluginManager.registerEvents(PlayerCraft(), this)
        server.pluginManager.registerEvents(CrafterListener(), this)
        server.pluginManager.registerEvents(MechTargetingListener(), this) // Delete after Senate Meeting
    }

    private fun registerCommands() {
        commandManager = PaperCommandManager.builder()
            .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
            .buildOnEnable(this)

        val annotationParser = AnnotationParser(commandManager, CommandSourceStack::class.java)
        annotationParser.parseContainers()
    }

    private fun readConfig() {
        if (!dataFolder.exists()) dataFolder.mkdir()
        val configFile = File(dataFolder, "config.yml")

        if (!configFile.exists()) {
            getResource("config.yml").use { inputStream ->
                configFile.outputStream().use { outputStream ->
                    inputStream!!.copyTo(outputStream)
                }
            }
        }

        val loader = YamlConfigurationLoader.builder()
            .file(configFile)
            .defaultOptions { options ->
                options.serializers { builder ->
                    builder.registerAnnotatedObjects(objectMapperFactory())
                }
            }
            .build()

        val node = loader.load()
        config = node.get(Config::class)!!
        logger.info("Loaded configuration.")
    }

    private fun registerMessengers() {
        logger.info("Registering plugin messengers.")
        messenger.registerIncomingPluginChannel(this, "minecraft:brand", BrandMessenger())
        messenger.registerIncomingPluginChannel(this, NoxesiumChannel.NOXESIUM_V1_CLIENT_INFORMATION_CHANNEL.channel, NoxesiumMessenger())
        messenger.registerIncomingPluginChannel(this, NoxesiumChannel.NOXESIUM_V2_CLIENT_INFORMATION_CHANNEL.channel, NoxesiumMessenger())
    }
}

val plugin = Bukkit.getPluginManager().getPlugin("tbdseason4")!!
val logger = plugin.logger
val messenger = Bukkit.getMessenger()