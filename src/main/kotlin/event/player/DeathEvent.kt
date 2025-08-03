package event.player

import chat.Formatting
import logger
import lore.Divinity
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffectType
import util.Keys.DIVINITY_CHAINS
import kotlin.random.Random

class DeathEvent: Listener {
    @EventHandler
    private fun onDeath(e: PlayerDeathEvent) {
        if(e.player.killer != null) {
            val killer = e.player.killer!!
            if(killer.inventory.itemInMainHand.persistentDataContainer.get(DIVINITY_CHAINS, PersistentDataType.BOOLEAN) == true) {
                Divinity.chainPlayer(e.player)
                e.deathMessage(null)
                e.player.health = 20.0
                e.isCancelled = true
                return
            }
        }
        if(Divinity.chainedPlayers.containsKey(e.player)) {
            e.deathMessage(null)
            e.isCancelled = true
            return
        }

        if (e.player.isInvisible || e.player.killer?.isInvisible == true)
            logger.info("Original death message: \"${plainText().serialize(e.deathMessage()!!)}\"")

        val component = e.deathMessage() as? TranslatableComponent ?: run { logger.warning("Another plugin edited this death message."); return; }
        val newArgs = mutableListOf<TranslationArgument>()
        component.arguments().forEach {
            val str = plainText().serialize(it.asComponent())
            if ((str == e.player.name && (e.player.isInvisible || e.player.name == "Byrtrum")) || (str == e.player.killer?.name && (e.player.killer?.isInvisible == true || e.player.killer?.name == "Byrtrum"))) {
                newArgs.add(TranslationArgument.component(
                    Formatting.allTags.deserialize(
                        "<hover:show_text:'Made you look.'><obfuscated>${"*".repeat(Random.nextInt(4, 16))}</obfuscated></hover>"
                    )
                ))
            } else {
                newArgs.add(it)
            }
        }
        if(e.player.killer?.name == "Byrtrum") {
            if(e.player.killer?.hasPotionEffect(PotionEffectType.INVISIBILITY) == true) {
                e.deathMessage(Formatting.allTags.deserialize(Formatting.DIVINATED_DEATH_MESSAGES.random().replace("%s", e.player.name)))
            } else {
                e.deathMessage(Formatting.allTags.deserialize(Formatting.DIVINE_DEATH_MESSAGES.random().replace("%s", e.player.name)))
            }
            return
        }
        val newComponent = component.arguments(newArgs)
        e.deathMessage(newComponent)
    }
}
