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
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CombatLogger extends CombatPlugin {


    public int spigotVersion;
    public YamlDocument config;

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        registerConfig();

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
        spigotVersion = Integer.parseInt(versionComponents[0] + versionComponents[1]);


        if (spigotVersion >= 19) {
            Bukkit.getPluginManager().registerEvents(new ElytraListener(), this);
        }

        // Check if there are any regions, if not disable section
        List<String> regions = config.getStringList("protected-regions.regions");
        if (!regions.isEmpty()) {
            Bukkit.getPluginManager().registerEvents(new MoveListener(), this);
        }
    }

    public void registerConfig() {
        try {
            config = YamlDocument.create(new File(getDataFolder(), "config.yml"), getResource("config.yml"),
                    GeneralSettings.builder().setDefaultString("").setDefaultList(ArrayList::new).setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String replaceMsg(Player player, String message) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }

        return message;
    }

    @Override
    public void saveConfig() {
        try {
            if (config != null)
                config.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reloadConfig() {
        try {
            if (config != null)
                config.reload();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
