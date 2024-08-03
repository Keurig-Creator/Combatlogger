package com.keurig.combatlogger.utils;

import com.keurig.combatlogger.CombatLogger;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigValue {

    public static int COMBAT_TIMER;
    public static boolean ACTIONBAR_ENABLED;
    public static String ACTIONBAR_MESSAGE_ON;
    public static String ACTIONBAR_MESSAGE_OFF;
    public static boolean CHAT_ENABLED;
    public static String CHAT_INTERVAL_MESSAGE;
    public static String CHAT_MESSAGE_ON;
    public static String CHAT_MESSAGE_OFF;
    public static GameMode FORCE_GAMEMODE;
    public static String FORCE_GAMEMODE_MSG;
    public static Set<EntityType> IGNORED_PROJECTILES;
    public static Set<EntityType> BLOCKED_PROJECTILES;
    public static List<Integer> CHAT_INTERVAL;
    public static boolean DISABLE_ELYTRA;

    static {
        IGNORED_PROJECTILES = new HashSet<>();
        BLOCKED_PROJECTILES = new HashSet<>();
        CHAT_INTERVAL = new ArrayList<>();
        loadValues();
    }

    public static void loadValues() {
        CombatLogger instance = CombatLogger.getInstance();
        FileConfiguration config = instance.getConfig();

        BLOCKED_PROJECTILES.clear();
        IGNORED_PROJECTILES.clear();
        CHAT_INTERVAL.clear();

        COMBAT_TIMER = config.getInt("combat-timer");

        ACTIONBAR_ENABLED = config.getBoolean("actionbar.enabled");
        ACTIONBAR_MESSAGE_ON = config.getString("actionbar.on-message");
        ACTIONBAR_MESSAGE_OFF = config.getString("actionbar.off-message");

        CHAT_ENABLED = config.getBoolean("chat.enabled");
        CHAT_MESSAGE_ON = config.getString("chat.on-message");
        CHAT_MESSAGE_OFF = config.getString("chat.off-message");

        CHAT_INTERVAL = config.getIntegerList("chat.interval.seconds");
        CHAT_INTERVAL_MESSAGE = config.getString("chat.interval.message");

        try {
            FORCE_GAMEMODE = GameMode.valueOf(config.getString("on-combat.force-gamemode.mode").toUpperCase());
        } catch (IllegalArgumentException e) {
            FORCE_GAMEMODE = null;
        }
        FORCE_GAMEMODE_MSG = config.getString("on-combat.force-gamemode.message");


        for (String entity : config.getStringList("ignored-projectiles")) {
            try {
                IGNORED_PROJECTILES.add(EntityType.valueOf(entity.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        for (String entity : config.getStringList("blocked-projectiles")) {
            try {
                BLOCKED_PROJECTILES.add(EntityType.valueOf(entity.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        DISABLE_ELYTRA = config.getBoolean("on-combat.disable-elytra");
    }
}
