package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.PunishmentTypes;
import com.keurig.combatlogger.handler.CombatPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JoinListener implements Listener {

	private final CombatLogger main;

	private Map<UUID, Long> banned = new HashMap<UUID, Long>();

	public JoinListener(CombatLogger main) {
		this.main = main;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		main.getCombatPlayer().addPlayer(e.getPlayer());
	}

	@EventHandler
	public void onPreJoin(AsyncPlayerPreLoginEvent e) {
		if (banned.containsKey(e.getUniqueId()) && banned.get(e.getUniqueId()) > System.currentTimeMillis()) {

			final List<String> punishment1 = main.getConfig().getStringList("punishment");

			for (int i = 0; i < punishment1.size(); i++) {

				if (punishment1.get(i).contains(":")) {
					final String[] args = punishment1.get(i).split(":");

					if (PunishmentTypes.valueOf(args[0]) == PunishmentTypes.BAN) {
						final int timeRemaining = (int) ((banned.get(e.getUniqueId()) - System.currentTimeMillis()) / 1000);

						e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&', args[2]).replace("{timeRemaining}",
								String.valueOf(timeRemaining)));
					}
				}
				
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		final CombatPlayer combatPlayer = main.getCombatPlayer();

		Player player = e.getPlayer();

		final List<String> punishment1 = main.getConfig().getStringList("punishment");

		if (e.getPlayer().hasPermission("combatlogger.admin"))
			return;

		if (combatPlayer.getCombatLogged().containsKey(player.getUniqueId()) && combatPlayer.getCombatLogged().get(player.getUniqueId()) > System.currentTimeMillis()) {
			for (int i = 0; i < punishment1.size(); i++) {

				if (punishment1.get(i).contains(":")) {
					final String[] args = punishment1.get(i).split(":");

					if (PunishmentTypes.valueOf(args[0]) == PunishmentTypes.BAN) {
						banned.put(player.getUniqueId(), System.currentTimeMillis() + Long.parseLong(args[1]) * 1000);
					}
				} else {
					if (PunishmentTypes.valueOf(punishment1.get(i)) == PunishmentTypes.KILL) {
						player.setHealth(0);
					}
				}

			}

			combatPlayer.removePlayer(player);
		}
	}
}
