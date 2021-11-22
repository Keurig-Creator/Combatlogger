package com.keurig.combatlogger;

import com.keurig.combatlogger.handler.CombatPlayer;
import com.keurig.combatlogger.listeners.AttackListener;
import com.keurig.combatlogger.listeners.CommandListener;
import com.keurig.combatlogger.listeners.DeathListener;
import com.keurig.combatlogger.listeners.JoinListener;
import com.keurig.combatlogger.punishment.PunishmentManager;
import com.keurig.combatlogger.utils.PlaceholderAPIHook;
import com.keurig.combatlogger.utils.factions.FactionsHook;
import com.keurig.combatlogger.utils.factions.FactionsManager;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CombatLogger extends JavaPlugin {

    /**
     * Get the instance of the CombatLogger.
     */
    @Getter
    private static CombatLogger instance;

    @Getter
    private CombatPlayer combatPlayer;

    @Getter
    private PunishmentManager punishmentManager;

    private FactionsManager factionsManager;

    @Getter
    private static FactionsHook factionsHook;

    @Getter
    private String nsmVersion;

    @Getter
    private boolean factionsEnabled;

    @Getter
    private PlaceholderAPIHook placeholderAPIHook;

    @Override
    public void onEnable() {
        instance = this;

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIHook = new PlaceholderAPIHook();
            placeholderAPIHook.register();
        }

        this.combatPlayer = new CombatPlayer(this);
        this.punishmentManager = new PunishmentManager();

        this.nsmVersion = Bukkit.getServer().getClass().getPackage().getName();
        this.nsmVersion = this.nsmVersion.substring(this.nsmVersion.lastIndexOf(".") + 1);

        this.factionsManager = new FactionsManager();

        factionsHook = this.factionsManager.getFactionsHook();
        this.factionsEnabled = this.factionsManager.isFactionsEnabled();

        registerEvents();
        registerConfig();
    }

    @Override
    public void onDisable() {
        this.punishmentManager.unregisterPunishments();
    }

    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new AttackListener(), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new CommandListener(this), this);
    }

    public void registerConfig() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
    }

    public String replaceMsg(Player player, String message) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }

        return message;
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
