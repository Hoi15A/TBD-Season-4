package fishing

import item.ItemRarity
import logger
import kotlin.random.Random

/**
 * Special fishing related properties for the rarities, all default to false
 *
 * @property showCatcher If the name of the catcher should be put on the fish
 * @property isAnimated If the rarity has an animation
 * @property sendGlobalMsg If a catch of this rarity should send a global message
 * @property sendGlobalTitle If a catch of this rarity should send a global title
 * @property retainData If ItemMeta should be retained on cooking for this rarity
 * @property showFishNumber If the number of fish caught should be shown in the lore
 */
data class RarityProperties(
    val showCatcher: Boolean = false,
    val isAnimated: Boolean = false,
    val sendGlobalMsg: Boolean = false,
    val sendGlobalTitle: Boolean = false,
    val retainData: Boolean = false,
    val showFishNumber: Boolean = false,
)

/**
 * @param weight Weight in % out of 100.0
 * @param itemRarity Item rarity used for display purposes
 * @param props Special display properties of the rarity
 */
enum class FishRarity(val weight: Double, val itemRarity: ItemRarity, val props: RarityProperties) {
    COMMON(47.7125, ItemRarity.COMMON, RarityProperties()),
    UNCOMMON(34.0, ItemRarity.UNCOMMON, RarityProperties()),
    RARE(12.0, ItemRarity.RARE, RarityProperties(isAnimated = true)),
    EPIC(5.0, ItemRarity.EPIC, RarityProperties(isAnimated = true, sendGlobalMsg = true)),
    LEGENDARY(
        1.0, ItemRarity.LEGENDARY, RarityProperties(
            isAnimated = true,
            sendGlobalMsg = true,
            showCatcher = true,
            retainData = true
        )
    ),
    MYTHIC(
        0.2, ItemRarity.MYTHIC, RarityProperties(
            isAnimated = true,
            sendGlobalMsg = true,
            sendGlobalTitle = true,
            showCatcher = true,
            retainData = true
        )
    ),
    UNREAL(
        0.05, ItemRarity.UNREAL, RarityProperties(
            isAnimated = true,
            sendGlobalMsg = true,
            sendGlobalTitle = true,
            showCatcher = true,
            retainData = true,
            showFishNumber = true
        )
    ),
    SPECIAL(
        0.0, ItemRarity.SPECIAL, RarityProperties(
            sendGlobalMsg = true,
            sendGlobalTitle = true,
            showCatcher = true,
            retainData = true,
            showFishNumber = true
        )
    ),
    TRANSCENDENT(
        0.025, ItemRarity.TRANSCENDENT, RarityProperties(
            isAnimated = true,
            sendGlobalMsg = true,
            sendGlobalTitle = true,
            showCatcher = true,
            retainData = true,
            showFishNumber = true
        )
    ),
    CELESTIAL(
        0.0125, ItemRarity.CELESTIAL, RarityProperties(
            isAnimated = true,
            sendGlobalMsg = true,
            sendGlobalTitle = true,
            showCatcher = true,
            retainData = true,
            showFishNumber = true
        )
    );

    companion object {
        fun getRandomRarity(): FishRarity {
            val totalWeight = entries.sumOf { it.weight }
            val randomValue = Random.nextDouble(totalWeight)

            var cumulativeWeight = 0.0
            for (rarity in entries) {
                cumulativeWeight += rarity.weight
                if (randomValue < cumulativeWeight) {
                    return rarity
                }
            }

            logger.warning("Unreachable code hit! No rarity selected")
            return SPECIAL // Should be unreachable but default to special in case of issue
        }
    }
}
