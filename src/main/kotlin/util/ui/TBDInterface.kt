package util.ui

import Memory
import chat.Formatting
import com.noxcrew.interfaces.drawable.Drawable
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.Element
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.grid.GridPoint
import com.noxcrew.interfaces.grid.GridPositionGenerator
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.pane.Pane
import com.noxcrew.interfaces.transform.builtin.PaginationButton
import com.noxcrew.interfaces.transform.builtin.PaginationTransformation
import kotlinx.coroutines.runBlocking
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import util.Sounds.INTERFACE_INTERACT

class TBDInterface(player: Player, interfaceType: TBDInterfaceType) {
    init {
        when(interfaceType) {
            TBDInterfaceType.MEMORY_ARCHIVE -> {
                runBlocking {
                    val memoryInterface = createMemoryInterface(player, interfaceType)
                    memoryInterface.open(player)
                }
            }
        }
    }

    private fun createMemoryInterface(player: Player, interfaceType: TBDInterfaceType) = buildChestInterface {
        val memories = Memory.getMemories().sortedBy { it.type.name }
        titleSupplier = { Formatting.allTags.deserialize("<!i><b><tbdcolour><shadow:#0>${interfaceType.interfaceName}") }
        rows = 6
        /** Apply pagination transform **/
        addTransform(PaginatedMemoryMenu(memories))
        /** Add overview item **/
        withTransform { pane, _ ->
            val infoMenuItem = ItemStack(Material.NETHER_STAR)
            val infoMenuItemMeta = infoMenuItem.itemMeta
            infoMenuItemMeta.displayName(Formatting.allTags.deserialize("<!i><tbdcolour>${interfaceType.interfaceName}"))
            infoMenuItemMeta.lore(listOf(
                Formatting.allTags.deserialize("<!i><white>Here you can find important items"),
                Formatting.allTags.deserialize("<!i><white>from the current season."),
                Formatting.allTags.deserialize("<!i><white>"),
                Formatting.allTags.deserialize("<!i><#d64304><prefix:warning> <#f26427>Info:"),
                Formatting.allTags.deserialize("<!i><#f26427>• New applicable items are added automatically."),
                Formatting.allTags.deserialize("<!i><#f26427>• Use</#f26427> <tbdcolour>/memory save</tbdcolour> <#f26427>to save older applicable items.")
            ))
            infoMenuItem.itemMeta = infoMenuItemMeta
            pane[0,4] = StaticElement(Drawable.Companion.drawable(infoMenuItem))
        }
        /** Add close menu button **/
        withTransform { pane, _ ->
            val closeMenuItem = ItemStack(Material.BARRIER)
            val closeMenuItemMeta = closeMenuItem.itemMeta
            closeMenuItemMeta.displayName(Formatting.allTags.deserialize("<!i><red>Close Menu"))
            closeMenuItem.itemMeta = closeMenuItemMeta
            pane[5,4] = StaticElement(Drawable.Companion.drawable(closeMenuItem)) {
                player.playSound(INTERFACE_INTERACT)
                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN)
            }
        }
        /** Draw central information item if the interface cannot be populated **/
        if(memories.isEmpty()) {
            withTransform { pane, _ ->
                val noMemoriesMenuItem = ItemStack(Material.BARRIER)
                val noMemoriesMenuItemMeta = noMemoriesMenuItem.itemMeta
                noMemoriesMenuItemMeta.displayName(Formatting.allTags.deserialize("<!i><red>The server has no memories... <gray>:pensive:"))
                noMemoriesMenuItemMeta.lore(listOf(
                    Formatting.allTags.deserialize("<i><dark_gray>They were wiped by the elite in 2007.")
                ))
                noMemoriesMenuItem.itemMeta = noMemoriesMenuItemMeta
                pane[2,4] = StaticElement(Drawable.Companion.drawable(noMemoriesMenuItem))
            }
        }
        /** Fill border with blank items **/
        withTransform { pane, _ ->
            val borderItem = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
                itemMeta = itemMeta.apply {
                    isHideTooltip = true
                }
            }
            val borderElement = StaticElement(drawable(borderItem))
            for(column in 0..8) {
                if(pane[0, column] == null) {
                    pane[0, column] = borderElement
                }
                if(pane[5, column] == null) {
                    pane[5, column] = borderElement
                }
            }
        }
    }
}

class PaginatedMemoryMenu(items: List<ItemStack>): PaginationTransformation<Pane, ItemStack>(
    positionGenerator = GridPositionGenerator { buildList {
            for(row in 1..4) {
                for(col in 0..8) {
                    add(GridPoint(row, col))
                }
            }
        }},
    items,
    back = PaginationButton(
        position = GridPoint(5, 2),
        drawable = drawable(ItemStack(Material.ARROW).apply { itemMeta = itemMeta.apply { displayName(Formatting.allTags.deserialize("<!i><tbdcolour>Back")) } }),
        increments = mapOf(Pair(ClickType.LEFT, -1)),
        clickHandler = { player -> player.playSound(INTERFACE_INTERACT) }
    ),
    forward = PaginationButton(
        position = GridPoint(5, 6),
        drawable = drawable(ItemStack(Material.ARROW).apply { itemMeta = itemMeta.apply { displayName(Formatting.allTags.deserialize("<!i><tbdcolour>Next")) } }),
        increments = mapOf(Pair(ClickType.LEFT, 1)),
        clickHandler = { player -> player.playSound(INTERFACE_INTERACT) }
    )
) {
    override suspend fun drawElement(index: Int, element: ItemStack): Element {
        return StaticElement(drawable(if(element.type == Material.AIR)
                ItemStack(Material.BARRIER).apply {
                val itemMeta = this.itemMeta
                itemMeta.displayName(Formatting.allTags.deserialize("<!i><red>An error occurred when loading this item."))
                this.itemMeta = itemMeta
            } else element)
        )
    }
}

enum class TBDInterfaceType(val interfaceName: String) {
    MEMORY_ARCHIVE("TBD SMP Memory Archive")
}