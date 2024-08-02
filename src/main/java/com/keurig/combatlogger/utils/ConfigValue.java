package com.keurig.combatlogger.utils;

import com.keurig.combatlogger.CombatLogger;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
import java.util.Set;

public class ConfigValue {

    public static GameMode FORCE_GAMEMODE;
    public static String FORCE_GAMEMODE_MSG;
    public static Set<EntityType> IGNORED_PROJECTILES = new HashSet<EntityType>();

    static {
        loadValues();
    }

    public static void loadValues() {
        CombatLogger instance = CombatLogger.getInstance();
        FileConfiguration config = instance.getConfig();
        try {
            FORCE_GAMEMODE = GameMode.valueOf(config.getString("on-combat.force-gamemode.mode", "SURVIVAL").toUpperCase());
        } catch (IllegalArgumentException e) {
            FORCE_GAMEMODE = null;
        }
        FORCE_GAMEMODE_MSG = config.getString("on-combat.force-gamemode.message", "&6Your gamemode has been updated.");

        for (String entity : config.getStringList("ignored-projectiles")) {
            try {
                IGNORED_PROJECTILES.add(EntityType.valueOf(entity.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}
