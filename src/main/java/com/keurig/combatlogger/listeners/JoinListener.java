package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener implements Listener {

	private final CombatLogger main;

	public JoinListener(CombatLogger main) {
		this.main = main;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		main.getCombatPlayer().addPlayer(e.getPlayer());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();

		main.getCombatPlayer().removePlayer(e.getPlayer());

//		if (main.getCombatLogged().containsKey(player.getUniqueId()) && main.getCombatLogged().get(player.getUniqueId()) > System.currentTimeMillis()) {
//			main.getCombatLogged().remove(player.getUniqueId());
//			player.setHealth(0);
//		}
	}

}
