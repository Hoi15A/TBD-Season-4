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
import org.bukkit.persistence.PersistentDataType
import util.Keys.NOXESIUM_IMMOVABLE
import util.Sounds.INTERFACE_INTERACT

class TBDInterface(player: Player, interfaceType: TBDInterfaceType) {
    init {
        when(interfaceType) {
            TBDInterfaceType.MEMORY_ARCHIVE -> {
                runBlocking {
                    createMemoryInterface(player, interfaceType, MemoryFilter.SEASON_FOUR)
                }
            }
        }
    }

    private suspend fun createMemoryInterface(player: Player, interfaceType: TBDInterfaceType, interfaceFilter: MemoryFilter) = buildChestInterface {
        val memories = if(interfaceFilter != MemoryFilter.SEASON_THREE) Memory.getMemories(interfaceFilter).sortedBy { it.type.name } else Memory.getMemories(interfaceFilter)
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
                Formatting.allTags.deserialize("<!i><dark_gray>•<#f26427> New applicable items are added automatically."),
                Formatting.allTags.deserialize("<!i><dark_gray>•<#f26427> Use</#f26427> <tbdcolour>/memory save</tbdcolour> <#f26427>to save older applicable items.")
            ))
            infoMenuItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
            infoMenuItem.itemMeta = infoMenuItemMeta
            pane[0,4] = StaticElement(Drawable.Companion.drawable(infoMenuItem))
        }
        /** Add filter control **/
        withTransform { pane, _ ->
            val filterMenuItem = ItemStack(Material.DETECTOR_RAIL)
            val filterMenuItemMeta = filterMenuItem.itemMeta
            filterMenuItemMeta.displayName(Formatting.allTags.deserialize("<!i><tbdcolour>Memory Filter"))
            val filterItemLore = mutableListOf(
                Formatting.allTags.deserialize("<!i><gray>Changes what items are shown depending"),
                Formatting.allTags.deserialize("<!i><gray>on which filter is selected. Defaults"),
                Formatting.allTags.deserialize("<!i><gray>to the current season."),
                Formatting.allTags.deserialize("<!i>"),
                Formatting.allTags.deserialize("<!i><gray>Current Filter:")
            )
            for(filterType in MemoryFilter.entries) {
                filterItemLore.add(Formatting.allTags.deserialize("<!i><dark_gray>• ${if(filterType == interfaceFilter) "<tbdcolour>> " else "<dark_gray>"}${filterType.memoryFilterName}"))
            }
            filterMenuItemMeta.lore(filterItemLore)
            filterMenuItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
            filterMenuItem.itemMeta = filterMenuItemMeta
            pane[5,3] = StaticElement(Drawable.Companion.drawable(filterMenuItem)) {
                player.playSound(INTERFACE_INTERACT)
                when(interfaceFilter) {
                    MemoryFilter.SEASON_ONE -> newMemoryInterface(player, TBDInterfaceType.MEMORY_ARCHIVE, MemoryFilter.SEASON_TWO)
                    MemoryFilter.SEASON_TWO -> newMemoryInterface(player, TBDInterfaceType.MEMORY_ARCHIVE, MemoryFilter.SEASON_THREE)
                    MemoryFilter.SEASON_THREE -> newMemoryInterface(player, TBDInterfaceType.MEMORY_ARCHIVE, MemoryFilter.SEASON_FOUR)
                    MemoryFilter.SEASON_FOUR -> newMemoryInterface(player, TBDInterfaceType.MEMORY_ARCHIVE, MemoryFilter.SEASON_ONE)
                }
            }
        }
        /** Add close menu button **/
        withTransform { pane, _ ->
            val closeMenuItem = ItemStack(Material.BARRIER)
            val closeMenuItemMeta = closeMenuItem.itemMeta
            closeMenuItemMeta.displayName(Formatting.allTags.deserialize("<!i><red>Close Menu"))
            closeMenuItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
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
                noMemoriesMenuItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
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
                    persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
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
    }.open(player)

    private fun newMemoryInterface(player: Player, interfaceType: TBDInterfaceType, interfaceFilter: MemoryFilter) {
        runBlocking {
            createMemoryInterface(player, interfaceType, interfaceFilter)
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
        drawable = drawable(ItemStack(Material.ARROW).apply { itemMeta = itemMeta.apply {
            displayName(Formatting.allTags.deserialize("<!i><tbdcolour>Back"))
            persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
        } }),
        increments = mapOf(Pair(ClickType.LEFT, -1)),
        clickHandler = { player -> player.playSound(INTERFACE_INTERACT) }
    ),
    forward = PaginationButton(
        position = GridPoint(5, 6),
        drawable = drawable(ItemStack(Material.ARROW).apply { itemMeta = itemMeta.apply {
            displayName(Formatting.allTags.deserialize("<!i><tbdcolour>Next"))
            persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
        } }),
        increments = mapOf(Pair(ClickType.LEFT, 1)),
        clickHandler = { player -> player.playSound(INTERFACE_INTERACT) }
    )
) {
    override suspend fun drawElement(index: Int, element: ItemStack): Element {
        return StaticElement(drawable(if(element.type == Material.AIR)
                ItemStack(Material.BARRIER).apply {
                val itemMeta = this.itemMeta
                itemMeta.displayName(Formatting.allTags.deserialize("<!i><red>An error occurred when loading this item."))
                itemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
                this.itemMeta = itemMeta
            } else element.apply { this.itemMeta.apply { persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true) } })
        )
    }
}

enum class TBDInterfaceType(val interfaceName: String) {
    MEMORY_ARCHIVE("TBD SMP Memory Archive")
}

enum class MemoryFilter(val memoryFilterName: String, val memoryFilterConfigSuffix: String) {
    SEASON_ONE("Season 1", ".season_one"),
    SEASON_TWO("Season 2", ".season_two"),
    SEASON_THREE("Season 3", ".season_three"),
    SEASON_FOUR("Season 4", ".season_four")
}