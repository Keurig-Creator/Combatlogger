package com.keurig.combatlogger.punishment;

import com.keurig.combatlogger.CombatLoggerPlugin;
import com.keurig.combatlogger.punishment.data.BanPunishment;
import com.keurig.combatlogger.punishment.data.KillPunishment;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class PunishmentHandler {

    private final HashMap<String, Punishment> punishments = new HashMap<>();

    @Getter
    private final FileConfiguration config;

    @Getter
    private final CombatLoggerPlugin plugin;

    @Getter
    private final String punishmentPath = "Punishment";

    public PunishmentHandler(CombatLoggerPlugin plugin) {
        this.plugin = plugin;

        this.config = plugin.getConfig();

        initDefault();
    }

    private void initDefault() {
        registerPunishment(new KillPunishment(this));
        registerPunishment(new BanPunishment(this), plugin);
    }

    public void runPunishments(Player player) {
        for (String str : config.getConfigurationSection("Punishment").getKeys(false)) {
            Punishment punishment = getByName(str);

            if (punishment == null) {
                Bukkit.getLogger().warning("Punishment " + str + " does not exist.");
                continue;
            }

            punishment.setPlayer(player);

            punishment.runPunishment();
        }
    }

    public void registerPunishment(Punishment punishment) {
        punishments.put(punishment.getLabel(), punishment);
    }

    public void registerPunishment(Punishment punishment, JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(punishment, plugin);
        punishments.put(punishment.getLabel(), punishment);
    }

    public Punishment getByName(String name) {
        return punishments.get(name);
    }

    public void clear() {
        punishments.clear();
    }
}
