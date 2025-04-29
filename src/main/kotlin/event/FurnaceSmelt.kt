package event

import fishing.FishRarity
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

        if (fishRarityStr != null) {// TODO infinite smelting bug, need to cancel in certain situations where output is already filled
            val fishRarity = FishRarity.valueOf(fishRarityStr)
            val isShiny = event.source.itemMeta.hasEnchantmentGlintOverride()
            if (fishRarity.props.retainData || isShiny) {
                copyFishData(event, fishRarity)
            }
        }
    }

    private fun copyFishData(event: FurnaceSmeltEvent, fishRarity: FishRarity) {
        val resultMeta = event.result.itemMeta
        resultMeta.displayName(
            event.result.effectiveName().color(TextColor.fromHexString(fishRarity.itemRarity.colourHex))
                .decoration(TextDecoration.ITALIC, false)
        )
        resultMeta.lore(event.source.lore())
        var glint = false
        if (event.source.itemMeta.hasEnchantmentGlintOverride()) {
            glint = event.source.itemMeta.enchantmentGlintOverride
        }
        resultMeta.setEnchantmentGlintOverride(glint)
        event.result.setItemMeta(resultMeta)
    }
}
