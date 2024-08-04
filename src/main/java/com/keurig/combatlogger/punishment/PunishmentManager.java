package com.keurig.combatlogger.punishment;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.punishment.punishments.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

@Getter
public class PunishmentManager {

    private static CombatLogger plugin;

    @Getter
    private final PunishmentConfig config;

    @Getter
    private static List<Punishment> punishments;
    public Map<UUID, Punishment> lastPunishment = new HashMap<>();

    public PunishmentManager() {
        plugin = CombatLogger.getInstance();
        punishments = new ArrayList<>();

        config = new PunishmentConfig(plugin, new File(plugin.getDataFolder(), "punishments.yml"));

        initializeDefault();
    }

    public void initializeDefault() {
        registerPunishment(new BanPunishment(), plugin);
        registerPunishment(new KillPunishment());
        registerPunishment(new WhitelistPunishment());
        registerPunishment(new CommandPunishment());
        registerPunishment(new EcoPunishment(), plugin);
    }

    /**
     * Registers the class for the punishment, specify the Plugin if you want your Bukkit Listener to be in the same class.
     *
     * @param punishment The class for your punishment
     */
    public static void registerPunishment(Punishment punishment) {
        punishments.add(punishment);
    }

    /**
     * Registers the class for the punishment, specify the Plugin if you want your Bukkit Listener to be in the same class.
     *
     * @param punishment The class for your punishment
     * @param plugin     The class to be registered for Bukkit Listener
     */
    public static void registerPunishment(Punishment punishment, Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(punishment, plugin);
        punishments.add(punishment);
    }

    /**
     * Unregister all the punishments.
     */
    public void unregisterPunishments() {
        punishments.clear();
    }

    public void onQuit(Player player) {
        for (Punishment punishment : config.getPunishments(player)) {
            punishment.setArgs(config.getPunishmentArgs(player).get(punishment));
            punishment.setPlayer(player);
            punishment.setPunishmentConfig(config);

            try {
                punishment.onQuit(punishment.getName());
            } catch (Exception e) {
            }
        }
//        final List<String> punishmentArgs = plugin.getConfig().getStringList("punishment");
//
//
//        for (String punishmentArg : punishmentArgs) {
//            final String[] tempSplit = punishmentArg.split(":");
//            final String label = tempSplit[0];
//
//            Punishment punishment = getPunishmentByName(label);
//
//            if (punishment == null) {
//                plugin.getLogger().log(Level.WARNING, "Cannot find " + label + " punishment! Did you misspell it or forgot to register it?");
//                continue;
//            }
//
//            punishment.setPlayer(player);
//
//            // Filtered out args basically keeps the other : if you have messages or what not
//            // it would get removed from the split so were just checking the number of args first
//            String[] args = new String[0];
//
//            final String[] split = punishmentArg.split(":", punishment.getNumberArgs() + 1);
//
//            if (punishment.getNumberArgs() >= 1) {
//                // Check if the punishment specifies a minimum number of arguments
//                args = Arrays.copyOfRange(split, 1, split.length);
//            }
//
//            punishment.onQuit(label);
//        }
    }

    public static Punishment getPunishmentByName(String punishmentName) {
        for (Punishment punishment : punishments) {
            if (punishment.getName().equalsIgnoreCase(punishmentName))
                return punishment;
        }

        return null;
    }
}
