package com.keurig.combatlogger.utils;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.api.CombatLoggerAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "combatlogger";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Keurig";
    }

    @Override
    public @NotNull String getVersion() {
        return CombatLogger.getInstance().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("time")) {
            return String.valueOf(CombatLoggerAPI.timeRemaining(player));
        }

        if (params.equalsIgnoreCase("timeformatted")) {
            return Chat.timeFormat(CombatLoggerAPI.timeRemaining(player));
        }

        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }
}
