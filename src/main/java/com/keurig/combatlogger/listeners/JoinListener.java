package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLogger;
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

				if (punishment1.get(i).contains("BAN:")) {

					String[] banArgs = punishment1.get(i).split(":");

					final int timeRemaining = (int) ((banned.get(e.getUniqueId()) - System.currentTimeMillis()) / 1000);

					e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&', banArgs[2]).replace("{timeRemaining}",
							String.valueOf(timeRemaining)));
				}
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();

		final List<String> punishment1 = main.getConfig().getStringList("punishment");


		if (e.getPlayer().hasPermission("combatlogger.admin"))
			return;

		if (main.getCombatPlayer().getCombatLogged().containsKey(player.getUniqueId()) && main.getCombatPlayer().getCombatLogged().get(player.getUniqueId()) > System.currentTimeMillis()) {
			for (int i = 0; i < punishment1.size(); i++) {

				if (punishment1.get(i).contains("KILL")) {
					player.setHealth(0);
				}

				if (punishment1.get(i).contains("BAN:")) {
					String[] banArgs = punishment1.get(i).split(":");

					banned.put(player.getUniqueId(), System.currentTimeMillis() + Long.parseLong(banArgs[1]) * 1000);
				}

			}
		}
	}
}
