package com.keurig.combatlogger.permission;

import com.keurig.combatlogger.CombatLoggerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PermissionHandler {

    private final CombatLoggerPlugin plugin;

    private final Map<String, Permission> permissionMap = new HashMap<>();

    public PermissionHandler(CombatLoggerPlugin plugin) {
        this.plugin = plugin;

        loadAllPermissions();
    }

    private void loadAllPermissions() {
        FileConfiguration config = plugin.getConfig();

        ConfigurationSection permSection = config.getConfigurationSection("permissions");

        for (String key : permSection.getKeys(false)) {
            ConfigurationSection k = permSection.getConfigurationSection(key);
            String permissionString = "";

            if (!key.equalsIgnoreCase("default")) {
                permissionString = permSection.getString(key + ".permission");
            }

            System.out.println(config.getString("permissions." + key + ".actionbar.on"));
            Bukkit.getLogger().info(String.valueOf(permSection.getLong(key + ".time")));
            Bukkit.getLogger().info(permissionString);
            Permission permission = new Permission(permSection.getLong(key + ".time"), permissionString, permSection.getInt(key + ".weight"));
            permission.setChatMessage(permSection.getString(key + ".message.mon"), permSection.getString(key + ".message.moff"));
            permission.setActionMessage(permSection.getString(key + ".actionbar.mon"), permSection.getString(key + ".actionbar.moff"));
            Bukkit.getLogger().info(permission.getChatMessage().toString());
            Bukkit.getLogger().info(permission.getActionbar().toString());
            permissionMap.put(key, permission);
        }
    }

    public Permission getPermission(Player player) {

        Permission permission = permissionMap.get("default");
        for (Map.Entry<String, Permission> en : permissionMap.entrySet()) {
            String key = en.getKey();
            Permission value = en.getValue();

            if (key.equalsIgnoreCase("default"))
                continue;

            System.out.println(value.toString());
            if (permission.getWeight() > value.getWeight()) {
                continue;
            }
            
            if (CombatLoggerPlugin.getPerms().playerHas(player, value.getPermission())) {
                permission = value;
            }
        }

        return permission;
    }
}
