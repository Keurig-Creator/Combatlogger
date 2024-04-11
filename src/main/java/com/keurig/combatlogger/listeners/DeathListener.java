package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.handler.CombatPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

	private final CombatLogger plugin = CombatLogger.getInstance();

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		CombatPlayer combatPlayer = this.plugin.getCombatPlayer();

		if (player.getKiller() == player) {
			return;
		}

		if (player.getKiller() instanceof Player) {
			combatPlayer.removePlayer(player);

			String combatOffChat = this.plugin.getConfig().getString("chat.off-message");

			if (combatOffChat != null) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', combatOffChat));
			}
		} else if (player.getKiller() != null && !player.getKiller().getType().isSpawnable()) {

        	}
	}
}
