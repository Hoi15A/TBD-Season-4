package event.block

import fishing.FishRarity
import fishing.Fishing.getSubRarity
import fishing.Fishing.hasSubRarity
import item.SubRarity
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.FurnaceSmeltEvent
import org.bukkit.persistence.PersistentDataType
import util.Keys.FISH_RARITY

class FurnaceSmelt : Listener {

    @EventHandler
    fun onFurnaceSmelt(event: FurnaceSmeltEvent) {
        val fishRarityStr = event.source.persistentDataContainer.get(FISH_RARITY, PersistentDataType.STRING)

        if (fishRarityStr != null) {
            val fishRarity = FishRarity.valueOf(fishRarityStr)
            if (fishRarity.props.retainData || event.source.hasSubRarity()) {
                copyFishData(event, fishRarity)
            }
        }
    }

    private fun copyFishData(event: FurnaceSmeltEvent, fishRarity: FishRarity) {
        val resultMeta = event.result.itemMeta
        event.source.persistentDataContainer.copyTo(resultMeta.persistentDataContainer, true)
        val subRarity = event.source.getSubRarity()
        var nameComponent = event.result.effectiveName().color(TextColor.fromHexString(fishRarity.itemRarity.colourHex))
            .decoration(TextDecoration.ITALIC, false)

        when (subRarity) {
            SubRarity.SHINY -> {
                resultMeta.setEnchantmentGlintOverride(true)
            }

            SubRarity.SHADOW -> {
                nameComponent = nameComponent.color(TextColor.fromHexString("#000000")).shadowColor(event.source.effectiveName().children()[0].shadowColor())
            }

            SubRarity.OBFUSCATED -> {
                nameComponent = nameComponent.font(Key.key("alt"))
            }

            else -> {}
        }

        resultMeta.displayName(nameComponent)
        resultMeta.lore(event.source.lore())
        event.result.setItemMeta(resultMeta)
    }
}
