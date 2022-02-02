package com.keurig.combatlogger.api;

import com.keurig.combatlogger.CombatLoggerPlugin;
import com.keurig.combatlogger.permission.Permission;
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
    public Permission getPermission(Player player) {
        return plugin.getPermissionHandler().getPermission(player);
    }

    @Override
    public boolean isTagged(Player player) {
        return plugin.getLoggedPlayers().containsKey(player.getUniqueId());
    }

    @Override
    public boolean isTagged(UUID uuid) {
        return plugin.getLoggedPlayers().containsKey(uuid);
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
