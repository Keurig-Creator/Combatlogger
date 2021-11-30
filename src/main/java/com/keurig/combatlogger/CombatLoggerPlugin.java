package com.keurig.combatlogger;

import com.keurig.combatlogger.api.APIHandler;
import com.keurig.combatlogger.api.CombatLoggerAPI;
import com.keurig.combatlogger.listeners.JoinListener;
import com.keurig.combatlogger.punishment.PunishmentHandler;
import com.keurig.combatlogger.runnables.CombatRemoveRunnable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

    @Override
    public void onEnable() {
        API = new APIHandler(this);
        saveDefaultConfig();

        punishmentHandler = new PunishmentHandler(getConfig());

        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);

    }

    @Override
    public void onDisable() {
        if (punishmentHandler != null) {
            punishmentHandler.clear();
        }
    }

    public void addLogged(Player player, Player target) {
        removeLogged(player.getUniqueId());

        CombatRemoveRunnable combatRemoveRunnable = new CombatRemoveRunnable(player, target);
        loggedPlayers.put(player.getUniqueId(), combatRemoveRunnable);
        combatRemoveRunnable.runTaskLater(this, 20 * 2);

    }

    public void removeLogged(UUID uuid) {
        if (loggedPlayers.containsKey(uuid)) {
            loggedPlayers.get(uuid).cancel();
            loggedPlayers.remove(uuid);
        }
    }
}
