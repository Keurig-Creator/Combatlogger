package com.keurig.combatlogger.punishment;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.punishment.punishments.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class PunishmentManager {

    private static CombatLogger plugin;

    @Getter
    private static List<Punishment> punishments;

    public PunishmentManager() {
        plugin = CombatLogger.getInstance();
        punishments = new ArrayList<>();

        initializeDefault();
    }

    private void initializeDefault() {
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
        final List<String> punishmentArgs = plugin.getConfig().getStringList("punishment");

        for (String punishmentArg : punishmentArgs) {
            final String[] split = punishmentArg.split(":");
            final String label = split[0];
            String[] args = new String[0];

            if (getPunishmentByName(label) == null) {
                plugin.getLogger().log(Level.WARNING, "Cannot find " + label + " punishment! Did you misspell it or forgot to register it?");
                continue;
            }

            final Punishment punishment = getPunishmentByName(label);
            punishment.setPlayer(player);

            if (punishment.getNumberArgs() >= 1) {
                args = Arrays.copyOfRange(split, 1, split.length);

                if (punishment.getNumberArgs() != args.length) {
                    plugin.getLogger().log(Level.WARNING, punishment.getName() + ", must have " + punishment.getNumberArgs() + " argument" + (punishment.getNumberArgs() == 1 ? "" : "s") + "!");
                    continue;
                }
            } else if (split.length >= 2) {
                plugin.getLogger().log(Level.WARNING, punishment.getName() + " Has no arguments!");
                continue;
            }

            punishment.onQuit(label, args);
        }
    }

    private Punishment getPunishmentByName(String punishmentName) {
        for (Punishment punishment : punishments) {
            if (punishment.getName().equals(punishmentName))
                return punishment;
        }

        return null;
    }
}
