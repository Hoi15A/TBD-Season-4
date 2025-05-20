package util.pdc

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import util.Keys.PDC_LOCATION_WORLD
import util.Keys.PDC_LOCATION_X
import util.Keys.PDC_LOCATION_Y
import util.Keys.PDC_LOCATION_Z

class LocationDataType : PersistentDataType<PersistentDataContainer, Location> {

    override fun getPrimitiveType(): Class<PersistentDataContainer> {
        return PersistentDataContainer::class.java
    }

    override fun getComplexType(): Class<Location> {
        return Location::class.java
    }

    override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): Location {
        return Location(
            Bukkit.getWorld(primitive.get(PDC_LOCATION_WORLD, PersistentDataType.STRING)!!),
            primitive.get(PDC_LOCATION_X, PersistentDataType.DOUBLE)!!,
            primitive.get(PDC_LOCATION_Y, PersistentDataType.DOUBLE)!!,
            primitive.get(PDC_LOCATION_Z, PersistentDataType.DOUBLE)!!
        )
    }

    override fun toPrimitive(complex: Location, context: PersistentDataAdapterContext): PersistentDataContainer {
        val container = context.newPersistentDataContainer()
        container.set(PDC_LOCATION_WORLD, PersistentDataType.STRING, complex.world.name)
        container.set(PDC_LOCATION_X, PersistentDataType.DOUBLE, complex.x)
        container.set(PDC_LOCATION_Y, PersistentDataType.DOUBLE, complex.y)
        container.set(PDC_LOCATION_Z, PersistentDataType.DOUBLE, complex.z)
        return container
    }
}