package item.treasurebag

import item.treasurebag.BagItem.*

/**
 * A pool of items a treasure bag may contain
 */
enum class BagLootPool(val possibleItems: List<BagItem>) {
    ENDER_DRAGON(listOf(
        DRAGON_EGG,
        DRAGON_ELYTRA,
        DRAGON_EYE,
        DRAGON_HEAD,
        DRAGON_PORTAL_FRAME,
        GENERIC_TBD_PLUS_TOKEN
    ))
}