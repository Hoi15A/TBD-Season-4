package config

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.net.URI

@ConfigSerializable
data class Config(
    val links: List<Link>,
    val resourcePacks: List<ResourcePack>,
    val textureOptions: List<String>,
    val motd: String = "Bro forgot to set the motd, laugh at this user"
)

@ConfigSerializable
data class Link(val component: String, val uri: URI, val order: Int)

@ConfigSerializable
data class ResourcePack(val uri: URI, val hash: String, val priority: Int)