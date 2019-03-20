package net.perfectdreams.dreamdiscordchestshop

import com.Acrobot.ChestShop.Events.ShopCreatedEvent
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent
import com.Acrobot.ChestShop.Events.TransactionEvent
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.discord.DiscordMessage
import net.perfectdreams.dreamcore.utils.discord.DiscordWebhook
import net.perfectdreams.dreamcore.utils.getLocalizedDisplayName
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.io.File

class DreamDiscordChestShop : KotlinPlugin(), Listener {
	companion object {
		lateinit var LOGGING_WEBHOOK: DiscordWebhook
	}

	override fun softEnable() {
		super.softEnable()

		val configFile = File(this.dataFolder, "config.json")

		this.dataFolder.mkdir()

		if (!configFile.exists()) {
			configFile.createNewFile()
			configFile.writeText(jsonObject("webhook_url" to "Webhook URL").toString())
		}

		val obj = DreamUtils.jsonParser.parse(configFile.readText())

		LOGGING_WEBHOOK = DiscordWebhook(obj["webhook_url"].string)

		registerEvents(this)
	}

	override fun softDisable() {
		super.softDisable()
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onShopCreate(event: ShopCreatedEvent) {
		var special = if (event.player.name != event.signLines[0]) {
			"para **`${event.signLines[0]}`** "
		} else {
			""
		}

		LOGGING_WEBHOOK.send(DiscordMessage(
				content = "<:lori_ok_hand:384300163390373888> **`${event.player.name}`** criou uma loja ${special}de *${event.signLines[1]} ${event.signLines[3]}* por *${event.signLines[2]}* em `${event.sign.world.name}` `${event.sign.location.blockX}`, `${event.sign.location.blockY}`, `${event.sign.location.blockZ}`"
		))
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onShopDestroyed(event: ShopDestroyedEvent) {
		val name = event.destroyer?.name

		var who = "Alguém quebrou uma loja de **`${event.sign.lines[0]}`**"

		if (name == event.sign.lines[0]) {
			who = "**`${event.sign.lines[0]}`** quebrou a própria loja"
		} else if (name != null) {
			who = "**`$name`** quebrou loja de **`${event.sign.lines[0]}`**"
		}

		LOGGING_WEBHOOK.send(DiscordMessage(
				content = "<:lori_triste:390855092976222216> $who que vendia *${event.sign.lines[1]} ${event.sign.lines[3]}* por *${event.sign.lines[2]}* em `${event.sign.world.name}` `${event.sign.location.blockX}`, `${event.sign.location.blockY}`, `${event.sign.location.blockZ}`"
		))
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onTransaction(event: TransactionEvent) {
		val items = event.stock.joinToString(", ", transform = { "*${it.amount} ${it.getLocalizedDisplayName("pt_br")}*" })

		LOGGING_WEBHOOK.send(DiscordMessage(
				content = when (event.transactionType) {
					TransactionEvent.TransactionType.BUY -> "\uD83D\uDCB3 **`${event.client.name}`** comprou $items de **`${event.ownerAccount.name}`** por *${event.price} Sonhos* em `${event.sign.location.world.name}` `${event.sign.location.blockX}`, `${event.sign.location.blockY}`, `${event.sign.location.blockZ}`"
					TransactionEvent.TransactionType.SELL -> "\uD83D\uDCB3 **`${event.client.name}`** vendeu $items para **`${event.ownerAccount.name}`** por *${event.price} Sonhos* em `${event.sign.location.world.name}` `${event.sign.location.blockX}`, `${event.sign.location.blockY}`, `${event.sign.location.blockZ}`"
					else -> "Algo deu errado ao tentar processar logging de $event!"
				}
		))
	}
}