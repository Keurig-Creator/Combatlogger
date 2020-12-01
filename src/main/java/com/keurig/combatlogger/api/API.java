package com.keurig.combatlogger.api;

import com.keurig.combatlogger.CombatLogger;
import org.bukkit.entity.Player;

public class API {

	/**
	 * @param player get the player tagged check
	 * @return true if user is tagged else false
	 */
	public static boolean isTagged(Player player) {
		return CombatLogger.getInstance().getCombatPlayer().getCombatLogged().containsKey(player.getUniqueId());
	}
}
