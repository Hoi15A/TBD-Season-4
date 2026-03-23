package config

import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type
import java.util.Base64

object ItemStackSerializer : TypeSerializer<ItemStack> {
    override fun deserialize(type: Type, node: ConfigurationNode): ItemStack {
        val encoded = node.string ?: throw SerializationException(type, "Expected Base64 string for ItemStack")
        return try {
            val bytes = Base64.getDecoder().decode(encoded)
            ItemStack.deserializeBytes(bytes)
        } catch (e: IllegalArgumentException) {
            throw SerializationException(
                type,
                "Failed to decode Base64 ItemStack from node at path ${node.path()} with value '$encoded'",
                e
            )
        }
    }

    override fun serialize(type: Type, obj: ItemStack?, node: ConfigurationNode) {
        if (obj == null) node.set(null)
        else node.set(Base64.getEncoder().encodeToString(obj.serializeAsBytes()))
    }
}