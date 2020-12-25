package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.api.CombatLoggerAPI;
import com.keurig.combatlogger.utils.Chat;
import com.massivecraft.factions.cmd.FCmdRoot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandFlightListener implements Listener {

	private final CombatLogger plugin = CombatLogger.getInstance();

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		final Player player = event.getPlayer();
		final String label = event.getMessage().substring(1);

		if (Bukkit.getPluginManager().isPluginEnabled("Factions"))
			for (String alias : FCmdRoot.getInstance().aliases) {

				for (String subAlias : FCmdRoot.getInstance().cmdFly.aliases) {

					if (label.equalsIgnoreCase(alias + " " + subAlias)) {

						if (CombatLoggerAPI.isTagged(player)) {
							if (this.plugin.isFactionsEnabled()) {
								CombatLogger.getFactionsHook().disableFlight(player);

								player.sendMessage(Chat.color("&cYou cannot fly during combat!"));

								event.setCancelled(true);
							}

							return;
						}

					}
				}
			}
	}

}
