package com.keurig.combatlogger.api;

import com.keurig.combatlogger.CombatLogger;
import org.bukkit.entity.Player;

public class CombatLoggerAPI {

	public boolean isTagged(Player player) {
		return CombatLogger.getInstance().getCombatPlayer().isTagged(player);
	}
}
