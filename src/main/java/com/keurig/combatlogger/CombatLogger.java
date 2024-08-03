package com.keurig.combatlogger;

import com.google.common.base.Charsets;
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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        Bukkit.getPluginManager().registerEvents(new QuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new CommandListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProjectileListener(), this);

        String[] versionComponents = Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.");
        int majorVersion = Integer.parseInt(versionComponents[0]);
        int minorVersion = Integer.parseInt(versionComponents[1]);

        if (majorVersion > 1 || (majorVersion == 1 && minorVersion >= 9)) {
            Bukkit.getPluginManager().registerEvents(new ElytraListener(), this);
        }
        // Check if there are any regions, if not disable section
        List<String> regions = getConfig().getStringList("protected-regions.regions");
        if (!regions.isEmpty()) {
            Bukkit.getPluginManager().registerEvents(new MoveListener(), this);
        }
    }

    public void registerConfig() {
        InputStream inputStream = getResource("config.yml");
        try {
            if (inputStream != null) {
                getConfig().setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream, Charsets.UTF_8)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        deleteOld(getConfig());
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public String replaceMsg(Player player, String message) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }

        return message;
    }

    private void deleteOld(FileConfiguration config) {
        String forceGamemode = config.getString("on-combat.force-gamemode");

        if (config.get("on-combat.force-gamemode") instanceof String) {
            config.set("on-combat.force-gamemode", null);
        }

        if (config.getString("on-combat.force-gamemode-message") != null) {
            config.set("on-combat.force-gamemode-message", null);
        }

        if (config.get("chat.use") != null) {
            config.set("chat.use", null);
        }

        if (config.get("actionbar.use") != null) {
            config.set("actionbar.use", null);
        }
    }
}
