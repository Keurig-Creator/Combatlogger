package com.keurig.combatlogger.api;

import com.keurig.combatlogger.CombatLogger;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class API {

	/**
	 * Check if the player is tagged or not.
	 *
	 * @param player get the player tagged check
	 * @return true if user is tagged else false
	 */
	public static boolean isTagged(Player player) {
		final Map<UUID, Long> combatLogged = CombatLogger.getInstance().getCombatPlayer().getCombatLogged();
		return combatLogged.containsKey(player.getUniqueId()) && combatLogged.get(player.getUniqueId()) > System.currentTimeMillis();
	}

	/**
	 * Get players combat time remaining.
	 *
	 * @param player get the player of time left
	 * @return the time left of players combat
	 */
	public static long timeRemaining(Player player) {
		final Map<UUID, Long> combatLogged = CombatLogger.getInstance().getCombatPlayer().getCombatLogged();
		return isTagged(player) ? combatLogged.get(player.getUniqueId()) - System.currentTimeMillis() : 0;
	}

}
