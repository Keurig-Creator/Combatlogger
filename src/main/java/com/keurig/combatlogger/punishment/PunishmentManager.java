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
            final String[] tempSplit = punishmentArg.split(":");
            final String label = tempSplit[0];

            Punishment punishment = getPunishmentByName(label);

            if (punishment == null) {
                plugin.getLogger().log(Level.WARNING, "Cannot find " + label + " punishment! Did you misspell it or forgot to register it?");
                continue;
            }

            punishment.setPlayer(player);

            // Filtered out args basically keeps the other : if you have messages or what not
            // it would get removed from the split so were just checking the number of args first
            String[] args = new String[0];

            final String[] split = punishmentArg.split(":", punishment.getNumberArgs() + 1);

            if (punishment.getNumberArgs() >= 1) {
                // Check if the punishment specifies a minimum number of arguments
                args = Arrays.copyOfRange(split, 1, split.length);
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
