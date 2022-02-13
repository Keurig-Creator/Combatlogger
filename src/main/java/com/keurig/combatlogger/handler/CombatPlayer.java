package com.keurig.combatlogger.handler;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.actionbar.ActionBar;
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
        final String combatOnActionBar = plugin.replaceMsg(player, this.plugin.getConfig().getString("actionbar.on-message"));
        final String combatOffActionBar = plugin.replaceMsg(player, this.plugin.getConfig().getString("actionbar.off-message"));

        if (isTagged(player)) {
            stopTasks(player);
        } else {
            if (useChat) {
                assert combatOnChat != null;
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', combatOnChat));
            }
        }

        if (!this.plugin.isFactionsEnabled())
            player.setFlying(false);

        if (useActionBar)
            this.taskActionBar.put(player.getUniqueId(), Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
                assert combatOnActionBar != null;
                ActionBar.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', combatOnActionBar.replace("{timeRemaining}", String.valueOf((combatTimeRemaining(player) / 1000) + 1))));
            }, 0, 0));

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
