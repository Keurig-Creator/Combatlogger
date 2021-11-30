package com.keurig.combatlogger.api;

import com.keurig.combatlogger.CombatLoggerPlugin;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * #TODO implement api
 */
@AllArgsConstructor
public class APIHandler implements CombatLoggerAPI {

    private CombatLoggerPlugin plugin;

    @Override
    public boolean isTagged(Player player) {
        return true;
    }

    @Override
    public boolean isTagged(UUID uuid) {
        return false;
    }

    @Override
    public Player getTarget(Player player) {
        return plugin.getLoggedPlayers().get(player.getUniqueId()).getTarget();
    }

    @Override
    public long timeRemaining(Player player) {
        return 0;
    }

    @Override
    public long timeRemaining(UUID uuid) {
        return 0;
    }
}
