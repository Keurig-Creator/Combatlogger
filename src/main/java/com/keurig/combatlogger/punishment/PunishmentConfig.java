package com.keurig.combatlogger.punishment;

import com.keurig.combatlogger.utils.CombatPlugin;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class PunishmentConfig {

    private final CombatPlugin plugin;
    public static HashMap<String, Set<Punishment>> punishmentMap = new HashMap<>();
    public static Set<Punishment> defaultPunishments = new HashSet<>();
    private FileConfiguration config;
    private final Permission permissionAPI;
    private final File file;

    public PunishmentConfig(CombatPlugin plugin, File file) {
        this.plugin = plugin;
        this.permissionAPI = plugin.getPermissionAPI();
        this.file = file;

        loadConfig(file);
        loadDefault();
        loadPermission();
    }

    public Set<Punishment> getPunishments(Player player) {
        Set<Punishment> punishments = new HashSet<>();

        boolean useDefault = true;

        for (String punishment : punishmentMap.keySet()) {

            if (permissionAPI != null && permissionAPI.has(player, punishment)) {
                useDefault = false;
                punishments.addAll(punishmentMap.get(punishment));
            }
        }

        if (useDefault) {
            punishments.addAll(defaultPunishments);
        }


        return punishments;
    }

    public Map<String, Object> getDefaultArg(Punishment punishment) {
        Map<String, Object> arguments = new HashMap<>();

        ConfigurationSection defaultSection = config.getConfigurationSection("default");

        if (defaultSection == null)
            return new HashMap<>();

        // Extract arguments from the punishmentConfig
        for (String key : defaultSection.getKeys(false)) {
            String[] split = key.toString().split("\\.");
            String fieldName;


            if (split.length > 1) {
                fieldName = split[1];
            } else {
                // Use the key directly as the fieldName
                fieldName = key;
            }

            Object type = defaultSection.get(key);


//            if (!isDataType(type))
//                continue;
            if (!fieldName.equalsIgnoreCase(punishment.getName()))
                continue;

//            Chat.log(type.toString());
//            Chat.log(key);

            ConfigurationSection punishmentValues = defaultSection.getConfigurationSection(fieldName);


            if (punishmentValues != null) {
                if (!fieldName.isBlank() && !fieldName.isEmpty()) {
                    for (String v : punishmentValues.getKeys(true)) {
                        arguments.put(v, punishmentValues.get(v));
                    }
                }
            }
//            if (!fieldName.isBlank() && !fieldName.isEmpty()) {
//
//                arguments.put(fieldName.toUpperCase(), type);
//            }
        }

//        Chat.log(arguments.toString());
        return arguments.isEmpty() ? Collections.emptyMap() : arguments;
    }

    public Map<Punishment, Map<String, Object>> getPunishmentArgs(Player player) {
        Map<Punishment, Map<String, Object>> punishments = new HashMap<>();


        for (Punishment punishment : getPunishments(player)) {

            Map<String, Object> arguments = new HashMap<>();
            ConfigurationSection permissions = config.getConfigurationSection("permissions");

            Map<String, Object> args = getDefaultArg(punishment);

            if (permissions == null)
                return new HashMap<>();

            List<String> keys = new ArrayList<>(permissions.getKeys(true));
            Collections.reverse(keys);

            for (String types : keys) {
                String permission = permissions.getString(types + ".permission");
                if (permission == null) {
                    continue;
                }

                ConfigurationSection punishmentSection = permissions.getConfigurationSection(types + ".punishments");

                if (punishmentSection == null) {
                    for (String type : permissions.getStringList(types + ".punishments")) {
//
//                        if (types.equalsIgnoreCase(punishment.getName()))
//                            continue;


                        if (!type.toUpperCase().equalsIgnoreCase(punishment.getName())) {
//                                Chat.log(type);
                            continue;
                        }


                        if (isDataType(type) && !args.isEmpty()) {

                            if (!arguments.containsKey(type)) {
                                if (permissionAPI != null && permissionAPI.has(player, permission)) {
//                                    Chat.log(args.toString());
                                    arguments.putAll(args);
                                }
                            }

                        }
                    }

                    continue;
                }

                String label = punishment.toString().toLowerCase();

                if (!label.equalsIgnoreCase(punishment.getName()))
                    continue;


                if (punishmentSection.contains(label)) {
                    ConfigurationSection punishmentConfig = punishmentSection.getConfigurationSection(label);

                    Object type = punishmentSection.get(label);
                    if (type != null && isDataType(type)) {
                        arguments.put(label, type);
                        continue;
                    }

                    // Ensure punishmentConfig is not null
                    if (punishmentConfig == null) {
                        continue;
                    }

                    // Handle other punishments
                    for (String key : punishmentConfig.getKeys(true)) {
                        type = punishmentConfig.get(key);

                        // Use the key directly as the fieldName
                        String fieldName = key;


                        if (!fieldName.isBlank() && !fieldName.isEmpty()) {
                            if (permissionAPI != null && permissionAPI.has(player, permission)) {
                                arguments.put(fieldName, type);
                            } else {
                                arguments.putAll(args);
                            }

                        }
                    }
                }
            }


            // Put the arguments map into the punishments map for the current punishment
            punishments.put(punishment, arguments);
        }


        return punishments;
    }


    public void loadDefault() {
        ConfigurationSection punishmentSection = config.getConfigurationSection("default");

        if (punishmentSection == null)
            return;

        // Save the punishments from a configuration section
        // EXAMPLE
        //    punishments:
        //      eco:
        //        amount: -1500
        //        message: "&fYour balance has been reduced by &6$1500 &7for logging out during combat."
        //      command: "broadcast &6%player% &flogged out during combat :("
        for (String key : punishmentSection.getKeys(false)) {

            Punishment punishment = PunishmentManager.getPunishmentByName(key);

            if (punishment != null) {
//                Chat.log("adding " + key);
                defaultPunishments.add(punishment);
            }
        }


        // Save the punishments from a list in the config
        // EXAMPLE
        //    punishments:
        //      - ban
        //      - eco
        for (String punish : config.getStringList("default")) {
            Punishment punishment = PunishmentManager.getPunishmentByName(punish);

            if (punishment != null) {
//                Chat.log("adding " + punish);
                defaultPunishments.add(punishment);
            }
        }
    }


    public void loadPermission() {
        ConfigurationSection permissions = config.getConfigurationSection("permissions");

        if (permissions == null)
            return;

        for (String types : permissions.getKeys(false)) {
            String permisison = permissions.getString(types + ".permission");


            ConfigurationSection punishmentSection = permissions.getConfigurationSection(types + ".punishments");

            // Use hashset instead to save memory and make sure copies cannot exist
            Set<Punishment> punishments = new HashSet<>();

            // Save the punishments from a configuration section
            // EXAMPLE
            //    punishments:
            //      eco:
            //        amount: -1500
            //        message: "&fYour balance has been reduced by &6$1500 &7for logging out during combat."
            //      command: "broadcast &6%player% &flogged out during combat :("
            if (punishmentSection != null) {
                for (String key : punishmentSection.getKeys(false)) {

                    Punishment punishment = PunishmentManager.getPunishmentByName(key);

                    if (punishment != null) {
                        punishments.add(punishment);
                    }
                }
            }

            // Save the punishments from a list in the config
            // EXAMPLE
            //    punishments:
            //      - ban
            //      - eco
            for (String punish : permissions.getStringList(types + ".punishments")) {
                Punishment punishment = PunishmentManager.getPunishmentByName(punish);

                if (punishment != null) {
                    punishments.add(punishment);
                }
            }


            if (permisison != null && !permisison.isEmpty() && !permisison.isBlank()) {
                punishmentMap.put(permisison, punishments);
            }
        }
    }

    public void loadConfig(File file) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("punishments.yml", false);

        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reloadConfig() {
        punishmentMap.clear();
        defaultPunishments.clear();

        loadConfig(file);
        loadDefault();
        loadPermission();
    }

    public boolean isDataType(Object object) {
        if (object instanceof Integer) {  // compilation error
            return true;
        }

        if (object instanceof String) {
            return true;
        }

        if (object instanceof Number) {  // compilation error
            return true;
        }

        if (object instanceof int[]) {  // compilation error
            return true;
        }

        if (object instanceof Integer[]) {  // compilation error
            return true;
        }

        // compilation error
        return object instanceof String[];
    }
}
