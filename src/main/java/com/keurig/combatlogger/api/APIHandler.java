package com.keurig.combatlogger.api;

import org.bukkit.entity.Player;

import java.util.UUID;

public class APIHandler implements CombatLoggerAPI {

    @Override
    public boolean isTagged(Player player) {
        return true;
    }

    @Override
    public boolean isTagged(UUID uuid) {
        return false;
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
