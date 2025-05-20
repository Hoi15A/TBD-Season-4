package item

import fishing.FishRarity
import logger
import kotlin.random.Random

enum class SubRarity(val weight : Double, val subRarityGlyph : String) {
    NULL(99.9706,""),
    SHINY(0.025, "\uE000"),
    SHADOW(0.004, "\uE001"),
    OBFUSCATED(0.0004, "\uE002");

    companion object {
        fun getRandomSubRarity(): SubRarity {
            val totalWeight = FishRarity.entries.sumOf { it.weight }
            val randomValue = Random.nextDouble(totalWeight)

            var cumulativeWeight = 0.0
            for (rarity in SubRarity.entries) {
                cumulativeWeight += rarity.weight
                if (randomValue < cumulativeWeight) {
                    return rarity
                }
            }

            logger.warning("Unreachable code hit! No sub rarity selected")
            return NULL // Should be unreachable but default to special in case of issue
        }
    }
}