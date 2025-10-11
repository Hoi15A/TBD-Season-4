package item.treasurebag

import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BundleMeta
import kotlin.random.Random

object TreasureBag {
    fun create(type: BagType): ItemStack {
        val treasureBag = ItemStack(type.bagMaterial)
        val bagMeta = treasureBag.itemMeta as BundleMeta

        for (item in type.lootPool.possibleItems) {
            if (!itemRollSuccess(item.pctChanceToRoll)) continue
            val amount = item.amountRange.random()
            val stack = item.itemStack.clone()
            stack.amount = amount
            bagMeta.addItem(stack)
        }

        bagMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
        bagMeta.displayName(type.displayName)
        bagMeta.lore(type.loreLines)
        treasureBag.itemMeta = bagMeta
        return treasureBag
    }

    private fun itemRollSuccess(chancePct: Int): Boolean {
        require(chancePct in 0..100) { "Percentage must be between 0 and 100" }
        val roll = Random.nextInt(0, 100)
        return roll < chancePct
    }
}
