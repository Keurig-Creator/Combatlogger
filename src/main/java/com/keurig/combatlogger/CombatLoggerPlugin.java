package com.keurig.combatlogger;

import com.keurig.combatlogger.api.APIHandler;
import com.keurig.combatlogger.api.CombatLoggerAPI;
import com.keurig.combatlogger.listeners.JoinListener;
import com.keurig.combatlogger.permission.PermissionHandler;
import com.keurig.combatlogger.punishment.PunishmentHandler;
import com.keurig.combatlogger.runnables.CombatRemoveRunnable;
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
    private static Permission perms = null;


    @Override
    public void onEnable() {
        setupPermissions();

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
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
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
