package com.keurig.combatlogger.utils;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.handler.CombatPlayer;
import com.keurig.combatlogger.punishment.PunishmentManager;
import com.keurig.combatlogger.utils.factions.FactionsHook;
import com.keurig.combatlogger.utils.factions.FactionsManager;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
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

    /**
     * @param player get the player tagged check.
     * @return true if user is tagged else false
     * @deprecated moved api to CombatLoggerAPI.isTagged(Player)
     */
    public boolean isTagged(Player player) {
        return getCombatPlayer().getCombatLogged().containsKey(player.getUniqueId());
    }
}
