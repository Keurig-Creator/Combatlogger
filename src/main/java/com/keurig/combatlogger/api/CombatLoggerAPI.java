package com.keurig.combatlogger.api;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.handler.CombatPlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class CombatLoggerAPI {

    /**
     * Checks if the player is currently tagged in combat.
     *
     * @param player The player to check for combat tagging.
     * @return True if the player is tagged in combat, otherwise false.
     */
    public static boolean isTagged(Player player) {
        final Map<UUID, Long> combatLogged = CombatLogger.getInstance().getCombatPlayer().getCombatLogged();
        return combatLogged.containsKey(player.getUniqueId()) && combatLogged.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    /**
     * Updates the combat tag for a player.
     *
     * @param player The player whose combat tag is to be updated.
     * @param tag    True to tag the player in combat, false to remove the tag.
     */
    public static void setTagged(Player player, boolean tag) {
        CombatPlayer combatPlayer = CombatLogger.getInstance().getCombatPlayer();

        if (tag) {
            combatPlayer.addCombat(player);
        } else {
            combatPlayer.removeCombat(player);
        }
    }

    /**
     * Retrieves the remaining time in seconds for a player's combat tag.
     *
     * @param player The player to query for remaining combat time.
     * @return The remaining time in milliseconds for the player's combat tag,
     * or 0 if the player is not currently tagged in combat.
     */
    public static long timeRemaining(Player player) {
        final Map<UUID, Long> combatLogged = CombatLogger.getInstance().getCombatPlayer().getCombatLogged();
        return isTagged(player) ? combatLogged.get(player.getUniqueId()) - System.currentTimeMillis() : 0;
    }

    public static long getRemainingTime(Player player) {
        return (timeRemaining(player) / 1000);
    }
}