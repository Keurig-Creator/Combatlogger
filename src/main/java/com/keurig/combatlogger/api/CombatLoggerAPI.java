package com.keurig.combatlogger.api;

import com.keurig.combatlogger.permission.Permission;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface CombatLoggerAPI {

    /**
     * Finds the group that the player should be associated with in the config REQUIRES VAULT or returns Default permission
     *
     * @param player the player you want to find the group
     * @return group
     */
    Permission getPermission(Player player);

    /**
     * Finds player and returns if the user is in combat.
     *
     * @param player which you want to check
     * @return true/false
     */
    boolean isTagged(Player player);

    /**
     * Finds player with uuid and returns if the user is in combat.
     *
     * @param uuid of player which you want to check
     * @return true/false
     */
    boolean isTagged(UUID uuid);

    /**
     * Finds the target who is tagged by player
     *
     * @param player player
     * @return player of target
     */
    Player getTarget(Player player);

    /**
     * Find remaining time on players combat
     *
     * @param player which you want to check
     * @return remaining time
     */
    long timeRemaining(Player player);

    /**
     * Find remaining time on players combat
     *
     * @param uuid which you want to check
     * @return remaining time
     */
    long timeRemaining(UUID uuid);

}
