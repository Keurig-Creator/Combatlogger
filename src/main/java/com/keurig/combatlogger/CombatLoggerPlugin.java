package com.keurig.combatlogger;

import com.keurig.combatlogger.api.APIHandler;
import com.keurig.combatlogger.api.CombatLoggerAPI;
import com.keurig.combatlogger.listeners.JoinListener;
import com.keurig.combatlogger.punishment.PunishmentHandler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CombatLoggerPlugin extends JavaPlugin {

    /**
     * This class provides you with the needs to [GET|UPDATE] values inside this plugin.
     *
     * @since 2.0.0
     */
    @Getter
    private static final CombatLoggerAPI API = new APIHandler();

    private PunishmentHandler punishmentHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        punishmentHandler = new PunishmentHandler(getConfig());

        Bukkit.getPluginManager().registerEvents(new JoinListener(punishmentHandler), this);

    }

    @Override
    public void onDisable() {
        punishmentHandler.clear();
    }
}
