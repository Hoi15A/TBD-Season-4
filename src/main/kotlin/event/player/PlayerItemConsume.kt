package event.player

import chat.Formatting.allTags
import fishing.FishRarity
import fishing.Fishing.hasSubRarity
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import util.Keys.FISH_RARITY
import util.startsWithVowel

class PlayerItemConsume : Listener {

    @EventHandler
    fun playerItemConsume(event: PlayerItemConsumeEvent) {
        val fishRarity = event.item.persistentDataContainer.get(FISH_RARITY, PersistentDataType.STRING)
        if (fishRarity != null) {
            consumeFish(event, fishRarity)
        }
    }

    fun consumeFish(event: PlayerItemConsumeEvent, rarityStr: String) {
        val rarity = FishRarity.valueOf(rarityStr)
        if (rarity.props.sendGlobalMsg || event.item.hasSubRarity()) {
            val component = playerConsumeFishComponent(rarity, event.player, event.item)
            Bukkit.getServer().sendMessage(component)
        }
    }

    private fun playerConsumeFishComponent(
        fishRarity: FishRarity,
        catcher: Player,
        item: ItemStack
    ) = allTags.deserialize(
        "<tbdcolour>${catcher.name}<reset> ate a${
            if (fishRarity.itemRarity.rarityName.startsWithVowel()) "n " else " "
        }<${fishRarity.itemRarity.colourHex}><b>${fishRarity.name}</b> "
    ).append(item.effectiveName().hoverEvent(item))

}