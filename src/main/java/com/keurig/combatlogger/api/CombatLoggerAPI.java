package com.keurig.combatlogger.api;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.handler.CombatPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CombatLoggerAPI {

    /**
     * Checks if the player is currently tagged in combat.
     *
     * @param player The player to check for combat tagging.
     * @return True if the player is tagged in combat, otherwise false.
     */
    public static boolean isTagged(Player player) {
        return CombatLogger.getInstance().getCombatPlayer().isTagged(player);
    }

    /**
     * Retrieves the remaining time in seconds for a player's combat tag.
     *
     * @param player The player to query for remaining combat time.
     * @return The remaining time in seconds for the player's combat tag,
     * or -1 if the player is not currently tagged.
     */
    public static int timeRemaining(Player player) {
        return CombatLogger.getInstance().getCombatPlayer().getTimeRemaining(player);
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

    /* =========================
       WHO TAGGED WHO API
       ========================= */

    /**
     * Returns the UUID of the player who tagged (most recently) this victim.
     * Returns null if victim isn't tagged or if tagger is unknown/non-player.
     */
    public static UUID getTaggerUUID(Player victim) {
        return CombatLogger.getInstance().getCombatPlayer().getTagger(victim);
    }

    /**
     * Returns the tagger as an OfflinePlayer (works even if they're offline).
     * Returns null if victim isn't tagged or tagger is unknown/non-player.
     */
    public static OfflinePlayer getTagger(Player victim) {
        UUID id = getTaggerUUID(victim);
        return id == null ? null : Bukkit.getOfflinePlayer(id);
    }

    /**
     * Returns true if the victim is currently tagged AND was tagged by attacker.
     */
    public static boolean isTaggedBy(Player victim, Player attacker) {
        return CombatLogger.getInstance().getCombatPlayer().isTaggedBy(victim, attacker);
    }

    /**
     * Returns when the victim was tagged (System.currentTimeMillis()).
     * Returns -1 if not tagged / unknown.
     */
    public static long getTaggedAt(Player victim) {
        return CombatLogger.getInstance().getCombatPlayer().getTaggedAt(victim);
    }

    /**
     * Returns a list of victim UUIDs that are currently tagged by attacker.
     */
    public static List<UUID> getVictimsTaggedBy(Player attacker) {
        return CombatLogger.getInstance().getCombatPlayer().getVictimsTaggedBy(attacker);
    }
}
