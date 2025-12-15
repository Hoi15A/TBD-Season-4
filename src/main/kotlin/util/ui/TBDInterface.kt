package util.ui

import Memory
import chat.Formatting
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.Element
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.grid.GridPoint
import com.noxcrew.interfaces.grid.GridPositionGenerator
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.pane.Pane
import com.noxcrew.interfaces.transform.builtin.PaginationButton
import com.noxcrew.interfaces.transform.builtin.PaginationTransformation
import command.Flexing
import item.ItemRarity
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import util.Keys.NOXESIUM_IMMOVABLE
import util.Sounds
import util.api.IslandAPI
import util.api.IslandAssetType
import util.api.Listings
import java.text.NumberFormat
import java.time.Instant

class TBDInterface(player: Player, interfaceType: TBDInterfaceType) {
    init {
        when(interfaceType) {
            TBDInterfaceType.MEMORY_ARCHIVE -> {
                runBlocking {
                    TBDInterfaces.createMemoryInterface(
                        player,
                        interfaceType,
                        MemoryFilter.SEASON_FOUR
                    )
                }
            }
            TBDInterfaceType.ISLAND_EXCHANGE -> {
                runBlocking {
                    TBDInterfaces.createExchangeInterface(
                        player,
                        currentListings = null,
                        interfaceType,
                        IslandExchangeMainFilter.RECENTLY_LISTED,
                        IslandExchangeRarityFilter.ALL,
                        IslandExchangeTypeFilter.ALL
                    )
                }
            }
            TBDInterfaceType.ISLAND_EXCHANGE_HISTORY -> {
                runBlocking {
                    TBDInterfaces.createPreviousSalesExchangeInterface(
                        player,
                        emptyList(),
                        interfaceType
                    )
                }
            }
            TBDInterfaceType.ISLAND_COSMETIC_INSPECT -> {
                runBlocking {
                    TBDInterfaces.createInspectCosmeticInterface(
                        player,
                        ItemStack.empty(),
                        interfaceType
                    )
                }
            }
        }
    }
}

object TBDInterfaces {
    suspend fun createExchangeInterface(player: Player, currentListings: List<Listings>?, interfaceType: TBDInterfaceType, mainFilter: IslandExchangeMainFilter, rarityFilter: IslandExchangeRarityFilter, typeFilter: IslandExchangeTypeFilter) = buildChestInterface {
        // Listings are only queried when the interface is initially opened, sorting and filter controls do not refresh this interface
        val baseListings = currentListings ?: IslandAPI.getListings()
        var listings = baseListings
        listings = when(mainFilter) {
            IslandExchangeMainFilter.RECENTLY_LISTED -> listings.sortedByDescending { Instant.parse(it.startTime) }
            IslandExchangeMainFilter.HIGHEST_PRICE -> listings.sortedByDescending { it.cost }
            IslandExchangeMainFilter.LOWEST_PRICE -> listings.sortedBy { it.cost }
        }
        listings = when(rarityFilter) {
            IslandExchangeRarityFilter.ALL -> { listings }
            IslandExchangeRarityFilter.COMMON -> listings.filter { it.rarity == ItemRarity.COMMON }
            IslandExchangeRarityFilter.UNCOMMON -> listings.filter { it.rarity == ItemRarity.UNCOMMON }
            IslandExchangeRarityFilter.RARE -> listings.filter { it.rarity == ItemRarity.RARE }
            IslandExchangeRarityFilter.EPIC -> listings.filter { it.rarity == ItemRarity.EPIC }
            IslandExchangeRarityFilter.LEGENDARY -> listings.filter { it.rarity == ItemRarity.LEGENDARY }
            IslandExchangeRarityFilter.MYTHIC -> listings.filter { it.rarity == ItemRarity.MYTHIC }
        }
        listings = when(typeFilter) {
            IslandExchangeTypeFilter.ALL -> { listings }
            IslandExchangeTypeFilter.COSMETIC_TOKEN -> listings.filter { it.assetType == IslandAssetType.COSMETIC_TOKEN }
            IslandExchangeTypeFilter.MCC_PLUS_TOKEN -> listings.filter { it.assetType == IslandAssetType.MCC_PLUS_TOKEN  }
            IslandExchangeTypeFilter.OTHER -> listings.filter { it.assetType == IslandAssetType.OTHER }
        }

        val items = mutableListOf<ItemStack>()
        for(listing in listings) items.add(listing.item)

        titleSupplier = { Formatting.allTags.deserialize("<!i><b><tbdcolour><shadow:#0:0.75>${interfaceType.interfaceName}") }
        rows = 6

        addTransform(PaginatedIslandExchangeMenu(items))
        /** Add overview item **/
        withTransform { pane, _ ->
            val infoMenuItem = ItemStack(Material.NETHER_STAR)
            val infoMenuItemMeta = infoMenuItem.itemMeta
            infoMenuItemMeta.displayName(Formatting.allTags.deserialize("<!i><tbdcolour>${interfaceType.interfaceName}"))
            infoMenuItemMeta.lore(listOf(
                Formatting.allTags.deserialize("<!i><white>Here you can find listings from the"),
                Formatting.allTags.deserialize("<!i><white>Island Exchange on MCC Island.")
            ))
            infoMenuItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
            infoMenuItem.itemMeta = infoMenuItemMeta
            pane[0,8] = StaticElement(drawable(infoMenuItem))
        }
        /** Add main filter control **/
        withTransform { pane, _ ->
            val filterMenuItem = ItemStack(Material.ACTIVATOR_RAIL)
            val filterMenuItemMeta = filterMenuItem.itemMeta
            filterMenuItemMeta.displayName(Formatting.allTags.deserialize("<!i><tbdcolour>Main Filter"))
            val filterItemLore = mutableListOf(
                Formatting.allTags.deserialize("<!i><gray>Sorts all shown listings by the specific"),
                Formatting.allTags.deserialize("<!i><gray>attribute selected below."),
                Formatting.allTags.deserialize("<!i>"),
                Formatting.allTags.deserialize("<!i><gray>Current Attribute:")
            )
            for(filterType in IslandExchangeMainFilter.entries) {
                filterItemLore.add(Formatting.allTags.deserialize("<!i><dark_gray>• ${if(filterType == mainFilter) "<tbdcolour>> " else "<dark_gray>"}${filterType.filterName}"))
            }
            filterMenuItemMeta.lore(filterItemLore)
            filterMenuItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
            filterMenuItem.itemMeta = filterMenuItemMeta
            pane[0,3] = StaticElement(drawable(filterMenuItem)) {
                player.playSound(Sounds.INTERFACE_INTERACT)
                when(mainFilter) {
                    IslandExchangeMainFilter.RECENTLY_LISTED -> newExchangeInterface(player, baseListings, TBDInterfaceType.ISLAND_EXCHANGE, IslandExchangeMainFilter.HIGHEST_PRICE, rarityFilter, typeFilter)
                    IslandExchangeMainFilter.HIGHEST_PRICE -> newExchangeInterface(player, baseListings, TBDInterfaceType.ISLAND_EXCHANGE, IslandExchangeMainFilter.LOWEST_PRICE, rarityFilter, typeFilter)
                    IslandExchangeMainFilter.LOWEST_PRICE -> newExchangeInterface(player, baseListings, TBDInterfaceType.ISLAND_EXCHANGE, IslandExchangeMainFilter.RECENTLY_LISTED, rarityFilter, typeFilter)
                }
            }
        }
        /** Add rarity filter control **/
        withTransform { pane, _ ->
            val filterMenuItem = ItemStack(Material.DETECTOR_RAIL)
            val filterMenuItemMeta = filterMenuItem.itemMeta
            filterMenuItemMeta.displayName(Formatting.allTags.deserialize("<!i><tbdcolour>Filter By Rarity"))
            val filterItemLore = mutableListOf(
                Formatting.allTags.deserialize("<!i><gray>Only shows listings that correspond in"),
                Formatting.allTags.deserialize("<!i><gray>rarity with the below selection."),
                Formatting.allTags.deserialize("<!i>"),
                Formatting.allTags.deserialize("<!i><gray>Current Rarity:")
            )
            for(filterType in IslandExchangeRarityFilter.entries) {
                filterItemLore.add(Formatting.allTags.deserialize("<!i><dark_gray>• ${if(filterType == rarityFilter) "<tbdcolour>> " else "<dark_gray>"}${filterType.filterName}"))
            }
            filterMenuItemMeta.lore(filterItemLore)
            filterMenuItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
            filterMenuItem.itemMeta = filterMenuItemMeta
            pane[0,4] = StaticElement(drawable(filterMenuItem)) {
                player.playSound(Sounds.INTERFACE_INTERACT)
                when(rarityFilter) {
                    IslandExchangeRarityFilter.ALL -> newExchangeInterface(player, baseListings, TBDInterfaceType.ISLAND_EXCHANGE, mainFilter, IslandExchangeRarityFilter.COMMON, typeFilter)
                    IslandExchangeRarityFilter.COMMON -> newExchangeInterface(player, baseListings, TBDInterfaceType.ISLAND_EXCHANGE, mainFilter, IslandExchangeRarityFilter.UNCOMMON, typeFilter)
                    IslandExchangeRarityFilter.UNCOMMON -> newExchangeInterface(player, baseListings, TBDInterfaceType.ISLAND_EXCHANGE, mainFilter, IslandExchangeRarityFilter.RARE, typeFilter)
                    IslandExchangeRarityFilter.RARE -> newExchangeInterface(player, baseListings, TBDInterfaceType.ISLAND_EXCHANGE, mainFilter, IslandExchangeRarityFilter.EPIC, typeFilter)
                    IslandExchangeRarityFilter.EPIC -> newExchangeInterface(player, baseListings, TBDInterfaceType.ISLAND_EXCHANGE, mainFilter, IslandExchangeRarityFilter.LEGENDARY, typeFilter)
                    IslandExchangeRarityFilter.LEGENDARY -> newExchangeInterface(player, baseListings, TBDInterfaceType.ISLAND_EXCHANGE, mainFilter, IslandExchangeRarityFilter.MYTHIC, typeFilter)
                    IslandExchangeRarityFilter.MYTHIC -> newExchangeInterface(player, baseListings, TBDInterfaceType.ISLAND_EXCHANGE, mainFilter, IslandExchangeRarityFilter.ALL, typeFilter)
                }
            }
        }
        /** Add type filter control **/
        withTransform { pane, _ ->
            val filterMenuItem = ItemStack(Material.POWERED_RAIL)
            val filterMenuItemMeta = filterMenuItem.itemMeta
            filterMenuItemMeta.displayName(Formatting.allTags.deserialize("<!i><tbdcolour>Filter By Type"))
            val filterItemLore = mutableListOf(
                Formatting.allTags.deserialize("<!i><gray>Only shows listings that correspond in"),
                Formatting.allTags.deserialize("<!i><gray>type with the below selection."),
                Formatting.allTags.deserialize("<!i>"),
                Formatting.allTags.deserialize("<!i><gray>Current Type:")
            )
            for(filterType in IslandExchangeTypeFilter.entries) {
                filterItemLore.add(Formatting.allTags.deserialize("<!i><dark_gray>• ${if(filterType == typeFilter) "<tbdcolour>> " else "<dark_gray>"}${filterType.filterName}"))
            }
            filterMenuItemMeta.lore(filterItemLore)
            filterMenuItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
            filterMenuItem.itemMeta = filterMenuItemMeta
            pane[0,5] = StaticElement(drawable(filterMenuItem)) {
                player.playSound(Sounds.INTERFACE_INTERACT)
                when(typeFilter) {
                    IslandExchangeTypeFilter.ALL -> newExchangeInterface(player, baseListings, TBDInterfaceType.ISLAND_EXCHANGE, mainFilter, rarityFilter, IslandExchangeTypeFilter.COSMETIC_TOKEN)
                    IslandExchangeTypeFilter.COSMETIC_TOKEN -> newExchangeInterface(player, baseListings, TBDInterfaceType.ISLAND_EXCHANGE, mainFilter, rarityFilter, IslandExchangeTypeFilter.MCC_PLUS_TOKEN)
                    IslandExchangeTypeFilter.MCC_PLUS_TOKEN -> newExchangeInterface(player, baseListings, TBDInterfaceType.ISLAND_EXCHANGE, mainFilter, rarityFilter, IslandExchangeTypeFilter.OTHER)
                    IslandExchangeTypeFilter.OTHER -> newExchangeInterface(player, baseListings, TBDInterfaceType.ISLAND_EXCHANGE, mainFilter, rarityFilter, IslandExchangeTypeFilter.ALL)
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
            pane[5,4] = StaticElement(drawable(closeMenuItem)) {
                player.playSound(Sounds.INTERFACE_INTERACT)
                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN)
            }
        }
        /** Draw central information item if the interface cannot be populated **/
        if(listings.isEmpty()) {
            withTransform { pane, _ ->
                val noListingsMenuItem = ItemStack(Material.BARRIER)
                val noListingsMenuItemMeta = noListingsMenuItem.itemMeta
                noListingsMenuItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
                noListingsMenuItemMeta.displayName(Formatting.allTags.deserialize("<!i><red>No Island Exchange listings found. <gray>:pensive:"))
                noListingsMenuItemMeta.lore(listOf(
                    Formatting.allTags.deserialize("<i><dark_gray>Capitalism is at an all time low.")
                ))
                noListingsMenuItem.itemMeta = noListingsMenuItemMeta
                pane[2,4] = StaticElement(drawable(noListingsMenuItem))
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
                for(row in 0..5) {
                    if(column in listOf(0, 8) || row in listOf(0, 5)) {
                        if(pane[row, column] == null) {
                            pane[row, column] = borderElement
                        }
                    }
                }
            }
        }
    }.open(player)

    private fun newExchangeInterface(player: Player, currentListings: List<Listings>, interfaceType: TBDInterfaceType, mainFilter: IslandExchangeMainFilter, rarityFilter: IslandExchangeRarityFilter, typeFilter: IslandExchangeTypeFilter) {
        runBlocking {
            createExchangeInterface(player, currentListings, interfaceType, mainFilter, rarityFilter, typeFilter)
        }
    }

    suspend fun createPreviousSalesExchangeInterface(player: Player, listings: List<Listings>, interfaceType: TBDInterfaceType) = buildChestInterface {
        titleSupplier = { Formatting.allTags.deserialize("<!i><b><tbdcolour><shadow:#0:0.75>${interfaceType.interfaceName}") }
        rows = 6
        /** Add overview item **/
        withTransform { pane, _ ->
            val infoMenuItem = ItemStack(Material.NETHER_STAR)
            val infoMenuItemMeta = infoMenuItem.itemMeta
            infoMenuItemMeta.displayName(Formatting.allTags.deserialize("<!i><tbdcolour>${interfaceType.interfaceName}"))
            infoMenuItemMeta.lore(listOf(
                Formatting.allTags.deserialize("<!i><white>Here you can find data, from the previous"),
                Formatting.allTags.deserialize("<!i><white>24 hours of listings, of the specified item"),
                Formatting.allTags.deserialize("<!i><white>from the Island Exchange on MCC Island.")
            ))
            infoMenuItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
            infoMenuItem.itemMeta = infoMenuItemMeta
            pane[0,8] = StaticElement(drawable(infoMenuItem))
        }
        /** Add close menu button **/
        withTransform { pane, _ ->
            val closeMenuItem = ItemStack(Material.BARRIER)
            val closeMenuItemMeta = closeMenuItem.itemMeta
            closeMenuItemMeta.displayName(Formatting.allTags.deserialize("<!i><red>Close Menu"))
            closeMenuItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
            closeMenuItem.itemMeta = closeMenuItemMeta
            pane[5,4] = StaticElement(drawable(closeMenuItem)) {
                player.playSound(Sounds.INTERFACE_INTERACT)
                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN)
            }
        }
        /** Add back button **/
        withTransform { pane, view ->
            val backItem = ItemStack(Material.SPECTRAL_ARROW)
            val backItemMeta = backItem.itemMeta
            backItemMeta.displayName(Formatting.allTags.deserialize("<!i><tbdcolour>Back"))
            backItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
            backItem.itemMeta = backItemMeta
            pane[5,0] = StaticElement(drawable(backItem)) {
                player.playSound(Sounds.INTERFACE_BACK)
                runBlocking {
                    view.parent()?.open()
                }
            }
        }
        /** Draw central information item if the interface cannot be populated **/
        if(listings.isEmpty()) {
            withTransform { pane, _ ->
                val noListingsMenuItem = ItemStack(Material.BARRIER)
                val noListingsMenuItemMeta = noListingsMenuItem.itemMeta
                noListingsMenuItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
                noListingsMenuItemMeta.displayName(Formatting.allTags.deserialize("<!i><red>No Island Exchange listings found. <gray>:pensive:"))
                noListingsMenuItemMeta.lore(listOf(
                    Formatting.allTags.deserialize("<i><dark_gray>Capitalism is at an all time low.")
                ))
                noListingsMenuItem.itemMeta = noListingsMenuItemMeta
                pane[2,4] = StaticElement(drawable(noListingsMenuItem))
            }
        } else {
            var totalCost = 0
            for(soldListing in listings) {
                totalCost += (soldListing.cost / soldListing.item.amount)
            }
            val averageCost = totalCost / listings.size
            val cheapestListing = listings.sortedBy { l -> l.cost / l.item.amount }[0]
            val minCost = cheapestListing.cost / cheapestListing.item.amount
            val mostExpensiveListing = listings.sortedByDescending { l -> l.cost / l.item.amount }[0]
            val maxCost = mostExpensiveListing.cost / mostExpensiveListing.item.amount
            /** Add sales data item **/
            withTransform { pane, _ ->
                val salesDataItem = listings[0].item
                val salesDataItemMeta = salesDataItem.itemMeta
                val existingLore = salesDataItemMeta.lore()
                val salesDataLore = listOf(
                    Formatting.allTags.deserialize("<!i><tbdcolour>Amount of sales: <white>${listings.size}"),
                    Formatting.allTags.deserialize("<!i><tbdcolour>Average cost: <#ffff00>\uD83E\uDE99<white>${NumberFormat.getIntegerInstance().format(averageCost)}"),
                    Formatting.allTags.deserialize("<!i><tbdcolour>Lowest cost: <#ffff00>\uD83E\uDE99<white>${NumberFormat.getIntegerInstance().format(minCost)}"),
                    Formatting.allTags.deserialize("<!i><tbdcolour>Highest cost: <#ffff00>\uD83E\uDE99<white>${NumberFormat.getIntegerInstance().format(maxCost)}"),
                )
                for(component in salesDataLore) existingLore?.add(component)
                salesDataItemMeta.lore(existingLore)
                salesDataItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
                salesDataItem.itemMeta = salesDataItemMeta
                pane[2,4] = StaticElement(drawable(salesDataItem))
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
                for(row in 0..5) {
                    if(column in listOf(0, 8) || row in listOf(0, 5)) {
                        if(pane[row, column] == null) {
                            pane[row, column] = borderElement
                        }
                    }
                }
            }
        }
    }.open(player)

    fun newPreviousSalesExchangeInterface(player: Player, previousItemName: String, interfaceType: TBDInterfaceType) {
        runBlocking {
            val listings = IslandAPI.previousSales(previousItemName)
            createPreviousSalesExchangeInterface(player, listings, interfaceType)
        }
    }

    suspend fun createInspectCosmeticInterface(player: Player, item: ItemStack, interfaceType: TBDInterfaceType) = buildChestInterface {
        titleSupplier = { Formatting.allTags.deserialize("<!i><b><tbdcolour><shadow:#0:0.75>${interfaceType.interfaceName}") }
        rows = 6
        /** Add overview item **/
        withTransform { pane, _ ->
            val infoMenuItem = ItemStack(Material.NETHER_STAR)
            val infoMenuItemMeta = infoMenuItem.itemMeta
            infoMenuItemMeta.displayName(Formatting.allTags.deserialize("<!i><tbdcolour>${interfaceType.interfaceName}"))
            infoMenuItemMeta.lore(listOf(
                Formatting.allTags.deserialize("<!i><white>Here you can find cosmetic data from"),
                Formatting.allTags.deserialize("<!i><white>the previously selected cosmetic"),
                Formatting.allTags.deserialize("<!i><white>from the Island Exchange on MCC Island.")
            ))
            infoMenuItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
            infoMenuItem.itemMeta = infoMenuItemMeta
            pane[0,8] = StaticElement(drawable(infoMenuItem))
        }
        /** Add close menu button **/
        withTransform { pane, _ ->
            val closeMenuItem = ItemStack(Material.BARRIER)
            val closeMenuItemMeta = closeMenuItem.itemMeta
            closeMenuItemMeta.displayName(Formatting.allTags.deserialize("<!i><red>Close Menu"))
            closeMenuItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
            closeMenuItem.itemMeta = closeMenuItemMeta
            pane[5,4] = StaticElement(drawable(closeMenuItem)) {
                player.playSound(Sounds.INTERFACE_INTERACT)
                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN)
            }
        }
        /** Add back button **/
        withTransform { pane, view ->
            val backItem = ItemStack(Material.SPECTRAL_ARROW)
            val backItemMeta = backItem.itemMeta
            backItemMeta.displayName(Formatting.allTags.deserialize("<!i><tbdcolour>Back"))
            backItemMeta.persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
            backItem.itemMeta = backItemMeta
            pane[5,0] = StaticElement(drawable(backItem)) {
                player.playSound(Sounds.INTERFACE_BACK)
                runBlocking {
                    view.parent()?.open()
                }
            }
        }

        /** Add cosmetic inspection item **/
        withTransform { pane, _ ->
            pane[2,4] = StaticElement(drawable(item))
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
                for(row in 0..5) {
                    if(column in listOf(0, 8) || row in listOf(0, 5)) {
                        if(pane[row, column] == null) {
                            pane[row, column] = borderElement
                        }
                    }
                }
            }
        }
    }.open(player)

    fun newInspectCosmeticInterface(player: Player, cosmeticName: String, interfaceType: TBDInterfaceType) {
        runBlocking {
            val cosmetic = IslandAPI.getCosmetic(cosmeticName)
            createInspectCosmeticInterface(player, cosmetic, interfaceType)
        }
    }

    suspend fun createMemoryInterface(player: Player, interfaceType: TBDInterfaceType, interfaceFilter: MemoryFilter) = buildChestInterface {
        val memories = if(interfaceFilter != MemoryFilter.SEASON_THREE) Memory.getMemories(interfaceFilter).sortedBy { it.type.name } else Memory.getMemories(interfaceFilter)
        titleSupplier = { Formatting.allTags.deserialize("<!i><b><tbdcolour><shadow:#0:0.75>${interfaceType.interfaceName}") }
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
            pane[0,4] = StaticElement(drawable(infoMenuItem))
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
            pane[5,3] = StaticElement(drawable(filterMenuItem)) {
                player.playSound(Sounds.INTERFACE_INTERACT)
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
            pane[5,4] = StaticElement(drawable(closeMenuItem)) {
                player.playSound(Sounds.INTERFACE_INTERACT)
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
                pane[2,4] = StaticElement(drawable(noMemoriesMenuItem))
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
                for(row in 0..5) {
                    if(column in listOf(0, 8) || row in listOf(0, 5)) {
                        if(pane[row, column] == null) {
                            pane[row, column] = borderElement
                        }
                    }
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
                for(col in 1..7) {
                    add(GridPoint(row, col))
                }
            }
        }},
    items,
    back = PaginationButton(
        position = GridPoint(5, 2),
        drawable = drawable(ItemStack(Material.ARROW).apply { itemMeta = itemMeta.apply {
            displayName(Formatting.allTags.deserialize("<!i><tbdcolour>Previous Page"))
            persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
        } }),
        increments = mapOf(Pair(ClickType.LEFT, -1)),
        clickHandler = { player -> player.playSound(Sounds.INTERFACE_INTERACT) }
    ),
    forward = PaginationButton(
        position = GridPoint(5, 6),
        drawable = drawable(ItemStack(Material.ARROW).apply { itemMeta = itemMeta.apply {
            displayName(Formatting.allTags.deserialize("<!i><tbdcolour>Next Page"))
            persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
        } }),
        increments = mapOf(Pair(ClickType.LEFT, 1)),
        clickHandler = { player -> player.playSound(Sounds.INTERFACE_INTERACT) }
    )) {
    override suspend fun drawElement(index: Int, element: ItemStack): Element {
        return StaticElement(drawable(if(element.type == Material.AIR)
            ItemStack(Material.BARRIER).apply {
                itemMeta = itemMeta.apply {
                    displayName(Formatting.allTags.deserialize("<!i><red>An error occurred when loading this item."))
                    persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
                }
            } else element.apply { itemMeta = itemMeta.apply { persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true) } })
        )
    }
}

class PaginatedIslandExchangeMenu(items: List<ItemStack>): PaginationTransformation<Pane, ItemStack>(
    positionGenerator = GridPositionGenerator { buildList {
            for(row in 1..4) {
                for(col in 1..7) {
                    add(GridPoint(row, col))
                }
            }
        }},
    items,
    back = PaginationButton(
        position = GridPoint(5, 2),
        drawable = drawable(ItemStack(Material.ARROW).apply { itemMeta = itemMeta.apply {
            displayName(Formatting.allTags.deserialize("<!i><tbdcolour>Previous Page"))
            persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
        } }),
        increments = mapOf(Pair(ClickType.LEFT, -1)),
        clickHandler = { player -> player.playSound(Sounds.INTERFACE_INTERACT) }
    ),
    forward = PaginationButton(
        position = GridPoint(5, 6),
        drawable = drawable(ItemStack(Material.ARROW).apply { itemMeta = itemMeta.apply {
            displayName(Formatting.allTags.deserialize("<!i><tbdcolour>Next Page"))
            persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
        } }),
        increments = mapOf(Pair(ClickType.LEFT, 1)),
        clickHandler = { player -> player.playSound(Sounds.INTERFACE_INTERACT) }
    )
) {
    override suspend fun drawElement(index: Int, element: ItemStack): Element {
        return StaticElement(drawable(if(element.type == Material.AIR)
            ItemStack(Material.BARRIER).apply {
                itemMeta = itemMeta.apply {
                    displayName(Formatting.allTags.deserialize("<!i><red>An error occurred when loading this item."))
                    persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true)
                }
            } else element.apply { itemMeta = itemMeta.apply { persistentDataContainer.set(NOXESIUM_IMMOVABLE, PersistentDataType.BOOLEAN, true) } })
        ) { click ->
            val player = click.player
            when(click.type) {
                ClickType.LEFT -> {
                    if(element.type in listOf(Material.FISHING_ROD, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.LEATHER_HORSE_ARMOR, Material.BOW, Material.CROSSBOW, Material.WOODEN_SWORD, Material.STONE_SWORD)) {
                        player.playSound(Sounds.INTERFACE_ENTER_SUB_MENU)
                        TBDInterfaces.newInspectCosmeticInterface(player, PlainTextComponentSerializer.plainText().serialize(element.effectiveName()).removeSuffix(" Token"), TBDInterfaceType.ISLAND_COSMETIC_INSPECT)
                    } else {
                        player.playSound(Sounds.INTERFACE_ERROR)
                    }
                }
                ClickType.RIGHT -> {
                    player.playSound(Sounds.INTERFACE_ENTER_SUB_MENU)
                    TBDInterfaces.newPreviousSalesExchangeInterface(player, PlainTextComponentSerializer.plainText().serialize(element.effectiveName()).removeSuffix(" Token"), TBDInterfaceType.ISLAND_EXCHANGE_HISTORY)
                }
                ClickType.SHIFT_RIGHT -> {
                    player.playSound(Sounds.INTERFACE_SHARE_ITEM)
                    Flexing.flex(player, element)
                } else -> {
                    player.playSound(Sounds.INTERFACE_ERROR)
                }
            }
        }
    }
}

enum class TBDInterfaceType(val interfaceName: String) {
    MEMORY_ARCHIVE("TBD SMP Memory Archive"),
    ISLAND_EXCHANGE("Island Exchange"),
    ISLAND_EXCHANGE_HISTORY("Island Exchange: Past Sales"),
    ISLAND_COSMETIC_INSPECT("Island: Cosmetic Details")
}

enum class MemoryFilter(val memoryFilterName: String, val memoryFilterConfigSuffix: String) {
    SEASON_ONE("Season 1", ".season_one"),
    SEASON_TWO("Season 2", ".season_two"),
    SEASON_THREE("Season 3", ".season_three"),
    SEASON_FOUR("Season 4", ".season_four")
}

enum class IslandExchangeMainFilter(val filterName: String) {
    RECENTLY_LISTED("Recently Listed"),
    HIGHEST_PRICE("Highest Price"),
    LOWEST_PRICE("Lowest Price")
}

enum class IslandExchangeRarityFilter(val filterName: String) {
    ALL("All"),
    COMMON("Common"),
    UNCOMMON("Uncommon"),
    RARE("Rare"),
    EPIC("Epic"),
    LEGENDARY("Legendary"),
    MYTHIC("Mythic")
}

enum class IslandExchangeTypeFilter(val filterName: String) {
    ALL("All"),
    COSMETIC_TOKEN("Cosmetic Token"),
    MCC_PLUS_TOKEN("MCC+ Token"),
    OTHER("Other")
}