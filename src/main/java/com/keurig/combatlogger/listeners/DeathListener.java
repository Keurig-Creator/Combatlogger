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
		final Player player = event.getEntity();

		final CombatPlayer combatPlayer = this.plugin.getCombatPlayer();
		combatPlayer.removePlayer(player);
		
		final String combatOffChat = this.plugin.getConfig().getString("chat.off-message");

		assert combatOffChat != null;
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', combatOffChat));
	}

}
