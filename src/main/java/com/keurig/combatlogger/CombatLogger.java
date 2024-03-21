package com.keurig.combatlogger;

import com.keurig.combatlogger.command.CombatLoggerCommand;
import com.keurig.combatlogger.handler.CombatPlayer;
import com.keurig.combatlogger.listeners.*;
import com.keurig.combatlogger.punishment.PunishmentConfig;
import com.keurig.combatlogger.punishment.PunishmentManager;
import com.keurig.combatlogger.punishment.punishments.EcoPunishment;
import com.keurig.combatlogger.utils.Chat;
import com.keurig.combatlogger.utils.CombatPlugin;
import com.keurig.combatlogger.utils.PlaceholderAPIHook;
import com.keurig.combatlogger.utils.factions.FactionsManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

public class CombatLogger extends CombatPlugin {

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIHook = new PlaceholderAPIHook();
            placeholderAPIHook.register();
        }

        this.combatPlayer = new CombatPlayer(this);

        // load before punishment manager
        setupPermissions();
        this.punishmentManager = new PunishmentManager();

        this.nsmVersion = Bukkit.getServer().getClass().getPackage().getName();
        this.nsmVersion = this.nsmVersion.substring(this.nsmVersion.lastIndexOf(".") + 1);

        this.factionsManager = new FactionsManager();

        factionsHook = this.factionsManager.getFactionsHook();
        this.factionsEnabled = this.factionsManager.isFactionsEnabled();

        if (setupEconomy()) {
            Chat.log("Ecomony integration has been enabled");
        }

        registerEvents();
        registerConfig();

        getCommand("combatlogger").setExecutor(new CombatLoggerCommand());


        PunishmentConfig punishmentConfig = new PunishmentConfig(this, new File(getDataFolder(), "punishments.yml"));
    }

    @Override
    public void onDisable() {
        this.punishmentManager.unregisterPunishments();
        EcoPunishment.joinMessages.clear();
    }

    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new AttackListener(), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new CommandListener(this), this);

        // Check if there are any regions, if not disable section
        List<String> regions = getConfig().getStringList("protected-regions.regions");
        if (!regions.isEmpty()) {
            Bukkit.getPluginManager().registerEvents(new MoveListener(), this);
        }
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
}
