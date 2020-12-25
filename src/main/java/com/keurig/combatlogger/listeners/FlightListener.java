package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.api.CombatLoggerAPI;
import com.keurig.combatlogger.utils.Chat;
import net.prosavage.factionsx.core.FPlayer;
import net.prosavage.factionsx.event.FPlayerFlyToggleEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FlightListener implements Listener {

	@EventHandler
	public void fPlayerFlightToggle(FPlayerFlyToggleEvent event) {
		final FPlayer fplayer = event.getFplayer();
		Player player = fplayer.getPlayer();

		assert player != null;
		if (CombatLoggerAPI.isTagged(player)) {
			player.sendMessage(Chat.color("&cYou cannot fly during combat!"));

			event.setCancelled(true);
		}
	}

}
