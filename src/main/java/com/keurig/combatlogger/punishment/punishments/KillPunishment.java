package com.keurig.combatlogger.punishment.punishments;

import com.keurig.combatlogger.punishment.Punishment;
import org.bukkit.entity.Player;

public class KillPunishment extends Punishment {

	public KillPunishment() {
		super("KILL");
	}

	@Override
	public void onQuit(String label, String[] args) {
		final Player player = getPlayer();

		if (player.hasPermission("combatlogger.admin"))
			return;
		
		player.setHealth(0);
	}
}
