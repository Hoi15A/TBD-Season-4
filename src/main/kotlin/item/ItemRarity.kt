package item

import net.tbdsmp.tbdseason4.type.Rarity
import org.bukkit.Color

enum class ItemRarity(val rarityName: String, val rarityGlyph: String, val colour: Color, val colourHex: String) {
    COMMON("Common", "\uF001", Color.fromRGB(255, 255, 255), "#ffffff"),
    UNCOMMON("Uncommon", "\uF002", Color.fromRGB(14, 209, 69), "#0ed145"),
    RARE("Rare", "\uF003", Color.fromRGB(0, 168, 243), "#00a8f3"),
    EPIC("Epic", "\uF004", Color.fromRGB(184, 61, 186), "#b83dba"),
    LEGENDARY("Legendary", "\uF005", Color.fromRGB(255, 127, 39), "#ff7f27"),
    MYTHIC("Mythic", "\uF006", Color.fromRGB(255, 51, 116), "#ff3374"),
    SPECIAL("Special", "\uF007", Color.fromRGB(236, 28, 36), "#ec1c24"),
    UNREAL("Unreal", "\uF008", Color.fromRGB(134, 102, 230), "#8666e6"),
    TRANSCENDENT("Transcendent", "\uE004", Color.fromRGB(199, 10, 23), "#c70a17"),
    CELESTIAL("Celestial", "\uE005", Color.fromRGB(245, 186, 10), "#f5ba0a");
}

fun Rarity.convertRarity(): ItemRarity {
    return when(this) {
        Rarity.COMMON -> ItemRarity.COMMON
        Rarity.UNCOMMON -> ItemRarity.UNCOMMON
        Rarity.RARE -> ItemRarity.RARE
        Rarity.EPIC -> ItemRarity.EPIC
        Rarity.LEGENDARY -> ItemRarity.LEGENDARY
        Rarity.MYTHIC -> ItemRarity.MYTHIC
        Rarity.UNKNOWN__ -> ItemRarity.SPECIAL
    }
}