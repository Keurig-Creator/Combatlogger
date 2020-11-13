package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

	private final CombatLogger main;

	public QuitListener(CombatLogger main) {
		this.main = main;
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();

		if (main.getCombatLogged().containsKey(player) && main.getCombatLogged().get(player) > System.currentTimeMillis()) {
			main.getCombatLogged().remove(player);
			player.setHealth(0);
		}
	}

}
