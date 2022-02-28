package com.keurig.combatlogger.handler;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.actionbar.ActionBar;
import com.keurig.combatlogger.api.CombatLoggerAPI;
import com.keurig.combatlogger.event.PlayerEnterCombatEvent;
import com.keurig.combatlogger.event.PlayerLeaveCombatEvent;
import com.keurig.combatlogger.utils.Chat;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class CombatPlayer {

    private final CombatLogger plugin;

    private final Map<UUID, Long> combatLogged;
    private final Set<UUID> players;
    private final Map<UUID, Integer> taskActionBar;
    private final Map<UUID, Integer> taskCombat;

    public CombatPlayer(CombatLogger plugin) {
        this.plugin = plugin;

        this.combatLogged = new HashMap<>();

        this.taskActionBar = new HashMap<>();
        this.taskCombat = new HashMap<>();
        this.players = new HashSet<>();

        addOnlinePlayers();
    }

    private void addOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.players.add(player.getUniqueId());
        }
    }

    public void addPlayer(Player player) {
        this.players.add(player.getUniqueId());
    }

    public void removePlayer(Player player) {

        this.players.remove(player.getUniqueId());
        this.combatLogged.remove(player.getUniqueId());

        if (this.taskCombat.get(player.getUniqueId()) != null)
            stopTasks(player);
    }

    public void addCombat(Player player) {
        if (player.hasPermission("combatlogger.admin"))
            return;

        final int combatTimer = this.plugin.getConfig().getInt("combat-timer");

        final boolean useChat = this.plugin.getConfig().getBoolean("chat.use");
        final String combatOnChat = plugin.replaceMsg(player, this.plugin.getConfig().getString("chat.on-message"));
        final String combatOffChat = plugin.replaceMsg(player, this.plugin.getConfig().getString("chat.off-message"));


        final boolean useActionBar = this.plugin.getConfig().getBoolean("actionbar.use");
        String combatOnActionBar = plugin.replaceMsg(player, this.plugin.getConfig().getString("actionbar.on-message"));
        final String combatOffActionBar = plugin.replaceMsg(player, this.plugin.getConfig().getString("actionbar.off-message"));

        final boolean disableFlight = plugin.getConfig().getBoolean("disable-flight-on-combat");

        if (isTagged(player)) {
            stopTasks(player);
        } else {
            if (useChat) {
                assert combatOnChat != null;
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', combatOnChat));

                PlayerEnterCombatEvent enterCombatEvent = new PlayerEnterCombatEvent(player);
                Bukkit.getPluginManager().callEvent(enterCombatEvent);
            }
        }

        if (!this.plugin.isFactionsEnabled()) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }

        if (disableFlight) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }

        if (useActionBar) {
            this.taskActionBar.put(player.getUniqueId(), Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
                String finalCombatOnActionBar = combatOnActionBar;

                finalCombatOnActionBar = finalCombatOnActionBar.replace("{timeRemaining}", Chat.timeFormat(CombatLoggerAPI.timeRemaining(player)));
                finalCombatOnActionBar = finalCombatOnActionBar.replace("%combatlogger_timeformatted%", Chat.timeFormat(CombatLoggerAPI.timeRemaining(player)));
                finalCombatOnActionBar = finalCombatOnActionBar.replace("%combatlogger_time%", String.valueOf(CombatLoggerAPI.timeRemaining(player)));
                ActionBar.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', finalCombatOnActionBar));
            }, 0, 0));
        }

        this.combatLogged.put(player.getUniqueId(), System.currentTimeMillis() + (combatTimer * 1000));
        this.taskCombat.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (taskActionBar.containsKey(player.getUniqueId())) {
                Bukkit.getScheduler().cancelTask(this.taskActionBar.get(player.getUniqueId()));
            }

            if (this.combatLogged.containsKey(player.getUniqueId())) {
                if (useActionBar) {
                    assert combatOffActionBar != null;
                    ActionBar.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', combatOffActionBar));
                }

                if (useChat) {
                    assert combatOffChat != null;
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', combatOffChat));
                }

                PlayerLeaveCombatEvent leaveCombatEvent = new PlayerLeaveCombatEvent(player);
                Bukkit.getPluginManager().callEvent(leaveCombatEvent);

                this.combatLogged.remove(player.getUniqueId());
            }

        }, 20 * combatTimer).getTaskId());

    }

    private void stopTasks(Player player) {
        if (taskActionBar.containsKey(player.getUniqueId()))
            Bukkit.getScheduler().cancelTask(this.taskActionBar.get(player.getUniqueId()));

        if (taskCombat.containsKey(player.getUniqueId()))
            Bukkit.getScheduler().cancelTask(this.taskCombat.get(player.getUniqueId()));
    }

    private boolean isTagged(Player player) {
        return this.combatLogged.containsKey(player.getUniqueId()) && this.combatLogged.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    private long combatTimeRemaining(Player player) {
        if (this.combatLogged.containsKey(player.getUniqueId()))
            return this.combatLogged.get(player.getUniqueId()) - System.currentTimeMillis();

        return 0;
    }
}
