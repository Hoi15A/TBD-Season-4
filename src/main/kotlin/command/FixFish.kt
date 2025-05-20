package command

import chat.Formatting
import fishing.FishRarity
import io.papermc.paper.command.brigadier.CommandSourceStack
import item.SubRarity.SHINY
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import util.Keys.FISH_IS_SHINY
import util.Keys.FISH_RARITY

@Suppress("unused", "unstableApiUsage")
@CommandContainer
class FixFish {
    @Command("fixfish")
    @Permission("tbd.command.fixfish")
    fun fixFish(css: CommandSourceStack) {
        val player = css.sender as Player
        val itemStack = player.inventory.itemInMainHand
        val isFish = Tag.ITEMS_FISHES.isTagged(itemStack.type)

        if (!isFish || itemStack.isEmpty) {
            player.sendMessage(Formatting.allTags.deserialize("<red>You are not holding a fish. For eel? On cod? No carp?</red>"))
        } else {
            val rarityFixApplied = fixMissingRarityPDC(itemStack)
            player.sendMessage(Formatting.allTags.deserialize("Rarity fix applied: <blue>$rarityFixApplied</blue>"))

            val fishRarityStr = itemStack.persistentDataContainer.get(FISH_RARITY, PersistentDataType.STRING)
            val isShiny = itemStack.persistentDataContainer.get(FISH_IS_SHINY, PersistentDataType.BOOLEAN)
            if (fishRarityStr == null) {
                player.sendMessage(Formatting.allTags.deserialize("<red>This does not seem to be a custom fish...</red>"))
                return
            }
            sendFishInfo(player, fishRarityStr, isShiny)
            if (isShiny == null) {
                val shinyFixApplied = fixMissingShinyPDC(itemStack)
                player.sendMessage(Formatting.allTags.deserialize("Shiny fix applied: <blue>$shinyFixApplied</blue>"))
            }
        }
    }

    private fun sendFishInfo(player: Player, fishRarityStr: String, shiny: Boolean?) {
        player.sendMessage(
            Formatting.allTags.deserialize(
                """
                    <dark_aqua>This fish has the data:</dark_aqua>
                    - rarity: '$fishRarityStr'
                    - shiny: '$shiny'<newline>
                """.trimIndent()
            )
        )
    }

    /**
     * @return if fix applied
     */
    private fun fixMissingRarityPDC(itemStack: ItemStack): Boolean {
        val fishRarityStr = itemStack.persistentDataContainer.get(FISH_RARITY, PersistentDataType.STRING)
        if (fishRarityStr != null) return false

        val fishMeta = itemStack.itemMeta
        val serializedLore = plainText().serialize(fishMeta.lore()!!.first())

        val rarity = FishRarity.entries.find { serializedLore.contains(it.itemRarity.rarityGlyph) }

        if (rarity != null) {
            fishMeta.persistentDataContainer.set(FISH_RARITY, PersistentDataType.STRING, rarity.name)
            itemStack.setItemMeta(fishMeta)
            return true
        } else {
            return false
        }
    }

    /**
     * @return if fix applied
     */
    private fun fixMissingShinyPDC(itemStack: ItemStack): Boolean {
        val fishMeta = itemStack.itemMeta

        if (fishMeta.hasEnchantmentGlintOverride() &&
            fishMeta.hasLore() &&
            plainText().serialize(fishMeta.lore()!!.first()).contains(SHINY.subRarityGlyph)) {
            fishMeta.persistentDataContainer.set(FISH_IS_SHINY, PersistentDataType.BOOLEAN, true)
            itemStack.setItemMeta(fishMeta)
            return true
        } else {
            return false
        }
    }
}
