package com.keurig.combatlogger;

import com.keurig.combatlogger.api.APIHandler;
import com.keurig.combatlogger.api.CombatLoggerAPI;
import com.keurig.combatlogger.listeners.JoinListener;
import com.keurig.combatlogger.permission.PermissionHandler;
import com.keurig.combatlogger.punishment.PunishmentHandler;
import com.keurig.combatlogger.runnables.CombatRemoveRunnable;
import com.keurig.combatlogger.utils.Updater;
import lombok.Getter;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CombatLoggerPlugin extends JavaPlugin {

    /**
     * This class provides you with the needs to [GET|UPDATE] values inside this plugin.
     *
     * @since 2.0.0
     */
    @Getter
    private static CombatLoggerAPI API;

    @Getter
    private final Map<UUID, CombatRemoveRunnable> loggedPlayers = new HashMap<>();

    @Getter
    private PunishmentHandler punishmentHandler;

    @Getter
    private PermissionHandler permissionHandler;

    @Getter
    private Permission perms = null;

    @Getter
    private boolean vaultEnabled;

    @Getter
    private Updater updater;

    @Override
    public void onEnable() {

        updater = new Updater(this);
        updater.getVersion(spigotVersion -> {
            int spigotVersionNumber = Integer.parseInt(spigotVersion.replace(".", ""));
            int pluginVersionNumber = Integer.parseInt(getDescription().getVersion().replace(".", ""));

            if (spigotVersionNumber > pluginVersionNumber) {
                getLogger().info("There is a new update available. New Version (" + spigotVersion + ") Your Version (" + getDescription().getVersion() + ")");
            } else if (pluginVersionNumber > spigotVersionNumber) {
                getLogger().info("Houston we have a problem... Some how we have time traveled with versions?? Old Version (" + spigotVersion + ") Your Future Version (" + getDescription().getVersion() + ")");
            } else {
                getLogger().info("All up to date!");
            }
        });

        if (setupPermissions()) {
            vaultEnabled = true;
        }

        API = new APIHandler(this);
        saveDefaultConfig();

        punishmentHandler = new PunishmentHandler(this);
        permissionHandler = new PermissionHandler(this);

        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);

    }

    @Override
    public void onDisable() {
        if (punishmentHandler != null) {
            punishmentHandler.clear();
        }
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = null;

        try {
            rsp = getServer().getServicesManager().getRegistration(Permission.class);
        } catch (NoClassDefFoundError err) {
            return false;
        }
        perms = rsp.getProvider();
        return perms != null;
    }


    public void addLogged(Player player, Player target) {

        if (loggedPlayers.containsKey(player.getUniqueId())) {

        } else {


            player.sendMessage("IN COMBAT");
        }

        removeLogged(player.getUniqueId());

        CombatRemoveRunnable combatRemoveRunnable = new CombatRemoveRunnable(loggedPlayers, player, target);
        loggedPlayers.put(player.getUniqueId(), combatRemoveRunnable);
        combatRemoveRunnable.runTaskLater(this, 20 * 2);
    }

    public void removeLogged(UUID uuid) {
        if (loggedPlayers.containsKey(uuid)) {
            loggedPlayers.get(uuid).cancel();
        }
    }
}
