package com.keurig.combatlogger.utils;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.handler.CombatPlayer;
import com.keurig.combatlogger.punishment.PunishmentManager;
import com.keurig.combatlogger.utils.factions.FactionsHook;
import com.keurig.combatlogger.utils.factions.FactionsManager;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CombatPlugin extends JavaPlugin {

    /**
     * Get the instance of the CombatLogger main class.
     */
    @Getter
    protected static CombatLogger instance;

    @Getter
    protected CombatPlayer combatPlayer;

    @Getter
    protected PunishmentManager punishmentManager;

    protected FactionsManager factionsManager;

    @Getter
    protected static FactionsHook factionsHook;

    @Getter
    protected String nsmVersion;

    @Getter
    protected boolean factionsEnabled;

    @Getter
    protected PlaceholderAPIHook placeholderAPIHook;

    @Getter
    protected static Economy economyAPI = null;

    @Getter
    protected Permission permissionAPI = null;

    protected boolean setupEconomy() {

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economyAPI = rsp.getProvider();
        return true;
    }

    protected boolean setupPermissions() {

        try {
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            permissionAPI = rsp.getProvider();
        } catch (NoClassDefFoundError e) {
            return false;
        }
        return permissionAPI != null;
    }
}
