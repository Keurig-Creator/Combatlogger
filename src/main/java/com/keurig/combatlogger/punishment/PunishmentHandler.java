package com.keurig.combatlogger.punishment;

import com.keurig.combatlogger.punishment.data.BanPunishment;
import com.keurig.combatlogger.punishment.data.KillPunishment;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PunishmentHandler {

    private final HashMap<String, Punishment> punishments = new HashMap<>();

    @Getter
    private final FileConfiguration config;

    @Getter
    private final String punishmentPath = "punishment";

    public PunishmentHandler(FileConfiguration config) {
        this.config = config;

        initDefault();
    }

    private void initDefault() {
        registerPunishment(new KillPunishment(this));
        registerPunishment(new BanPunishment(this));
    }

    public void runPunishments(Player player) {
        for (String str : config.getConfigurationSection("punishment").getKeys(false)) {
            Punishment punishment = getByName(str);

            if (punishment == null) {
                Bukkit.getLogger().info("Unknown punishment");
                continue;
            }

            punishment.setPlayer(player);

            punishment.runPunishment();
        }
    }

    public void registerPunishment(Punishment punishment) {
        System.out.println(punishment.getLabel());
        punishments.put(punishment.getLabel(), punishment);
    }

    public Punishment getByName(String name) {
        return punishments.get(name);
    }

    public void clear() {
        punishments.clear();
    }

//    public Punishment getByName(String name) {
//        for (Punishment punishment : punishments) {
//            if (punishment.getLabel().equalsIgnoreCase(name)) {
//                return punishment;
//            }
//        }
//
//        return null;
//    }
}
