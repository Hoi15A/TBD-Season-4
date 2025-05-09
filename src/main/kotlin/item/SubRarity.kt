package item

import kotlin.random.Random

enum class SubRarity(val subRarityName : String, val subRarityGlyph : String) {
    SHINY("Shiny", "\uE000"),
    SHADOW("Shadow", "\uE001"),
    OBFUSCATED("Obfuscated", "\uE002");

    companion object {
        fun isShiny(): Boolean {
            return Random.nextInt(0, 4000) == 1
        }
    }
}