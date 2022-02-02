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

    net.milkbowl.vault.permission.Permission perms;

    public PermissionHandler(CombatLoggerPlugin plugin) {
        this.plugin = plugin;
        this.perms = plugin.getPerms();

        loadAllPermissions();
    }

    private void loadAllPermissions() {
        FileConfiguration config = plugin.getConfig();

        ConfigurationSection permSection = config.getConfigurationSection("Permissions");

        for (String key : permSection.getKeys(false)) {

            String permissionString = "";

            if (!key.equalsIgnoreCase("Default")) {
                if (!plugin.isVaultEnabled()) {
                    continue;
                }
                permissionString = permSection.getString(key + ".Permission");
            }

            Permission permission = new Permission(permSection.getLong(key + ".Time"), permissionString, permSection.getInt(key + ".Weight"));
            permission.setChatMessage(permSection.getString(key + ".Message.MessageOn"), permSection.getString(key + ".Message.MessageOff"));
            permission.setActionMessage(permSection.getString(key + ".Actionbar.MessageOn"), permSection.getString(key + ".Actionbar.MessageOff"));

            permissionMap.put(key, permission);
            Bukkit.getPluginManager().addPermission(new org.bukkit.permissions.Permission(permission.getPermission()));

            System.out.println(key);
        }
    }

    public Permission getPermission(Player player) {

        Permission permission = getDefault();

        if (plugin.isVaultEnabled()) {
            for (Map.Entry<String, Permission> en : permissionMap.entrySet()) {
                String key = en.getKey();
                Permission value = en.getValue();

                if (key.equalsIgnoreCase("Default"))
                    continue;

                if (permission.getWeight() > value.getWeight()) {
                    continue;
                }

                if (perms.playerHas(player, value.getPermission())) {
                    permission = value;
                }
            }
        }

        return permission;
    }

    public Permission getDefault() {
        return permissionMap.get("Default");
    }
}
