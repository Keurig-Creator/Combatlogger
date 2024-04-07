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
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class CombatPlayer {

    private final CombatLogger plugin;

    private final Map<UUID, Long> combatLogged;
    private final Set<UUID> players;
    private final Map<UUID, Integer> taskActionBar;
    private final Map<UUID, Integer> taskChat;
    private final Map<UUID, Integer> taskCombat;

    public CombatPlayer(CombatLogger plugin) {
        this.plugin = plugin;

        this.combatLogged = new HashMap<>();

        this.taskActionBar = new HashMap<>();
        this.taskChat = new HashMap<>();
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

        final boolean useActionBar = this.plugin.getConfig().getBoolean("actionbar.use");
        String combatOnActionBar = plugin.replaceMsg(player, this.plugin.getConfig().getString("actionbar.on-message"));

        String intervalMessage = plugin.getConfig().getString("chat.interval.message");
        List<Integer> intervals = plugin.getConfig().getIntegerList("chat.interval.seconds");


        final boolean disableFlight = plugin.getConfig().getBoolean("on-combat.disable-flight");

        String forceGamemode = plugin.getConfig().getString("on-combat.force-gamemode");

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

        if (forceGamemode != null && !forceGamemode.isBlank()) {

            GameMode gameMode = GameMode.valueOf(forceGamemode.toUpperCase(Locale.ROOT));

            if (!player.getGameMode().equals(gameMode)) {
                player.setGameMode(gameMode);
                Chat.message(player, plugin.getConfig().getString("on-combat.force-gamemode-message"));
            }

        }

        if (disableFlight) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }

        if (!intervals.isEmpty()) {
            this.taskChat.put(player.getUniqueId(), Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {

                String finalIntervalMessage = intervalMessage;

                String time = Chat.timeFormat(CombatLoggerAPI.timeRemaining(player) - 1, true);

                finalIntervalMessage = finalIntervalMessage.replace("{timeRemaining}", time);
                finalIntervalMessage = finalIntervalMessage.replace("%combatlogger_timeformatted%", time);
                finalIntervalMessage = finalIntervalMessage.replace("%combatlogger_time%", time);


                int timeRemaining = (int) (CombatLoggerAPI.getRemainingTime(player));

                Chat.log(String.valueOf(timeRemaining));
                if (intervals.contains(timeRemaining)) {
                    Chat.message(player, finalIntervalMessage);
                }
            }, 0, 20));
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

        // Store the current time in milliseconds when a player enters combat,
        // along with the time when they will leave combat based on the combatTimer.
        this.combatLogged.put(player.getUniqueId(), System.currentTimeMillis() + (combatTimer * 1000));

        // Schedule a task to run after 'combatTimer' seconds to check if the player
        // is still in combat.
        this.taskCombat.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            // Check if the player's ActionBar task exists and cancel it if so.
            if (taskActionBar.containsKey(player.getUniqueId())) {
                Bukkit.getScheduler().cancelTask(this.taskActionBar.get(player.getUniqueId()));
            }

            if (taskChat.containsKey(player.getUniqueId())) {
                Bukkit.getScheduler().cancelTask(this.taskChat.get(player.getUniqueId()));
            }

            // Check if the player is still marked as in combat.
            if (this.combatLogged.containsKey(player.getUniqueId())) {
                removeCombat(player);
            }

        }, 20L * combatTimer).getTaskId());

    }

    public void removeCombat(Player player) {
        final boolean useChat = this.plugin.getConfig().getBoolean("chat.use");
        final String combatOffChat = plugin.replaceMsg(player, this.plugin.getConfig().getString("chat.off-message"));

        final boolean useActionBar = this.plugin.getConfig().getBoolean("actionbar.use");
        final String combatOffActionBar = plugin.replaceMsg(player, this.plugin.getConfig().getString("actionbar.off-message"));


        // If configured to do so, send an ActionBar message to the player.
        if (useActionBar) {
            assert combatOffActionBar != null; // Ensure combatOffActionBar is not null
            ActionBar.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', combatOffActionBar));
        }

        // If configured to do so, send a chat message to the player.
        if (useChat) {
            assert combatOffChat != null; // Ensure combatOffChat is not null
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', combatOffChat));
        }

        // Call a custom event to signify that the player has left combat.
        PlayerLeaveCombatEvent leaveCombatEvent = new PlayerLeaveCombatEvent(player);
        Bukkit.getPluginManager().callEvent(leaveCombatEvent);

        // Remove the player from the combat logged list.
        this.combatLogged.remove(player.getUniqueId());
    }

    private void stopTasks(Player player) {
        if (taskActionBar.containsKey(player.getUniqueId()))
            Bukkit.getScheduler().cancelTask(this.taskActionBar.get(player.getUniqueId()));

        if (taskCombat.containsKey(player.getUniqueId()))
            Bukkit.getScheduler().cancelTask(this.taskCombat.get(player.getUniqueId()));

        if (taskChat.containsKey(player.getUniqueId()))
            Bukkit.getScheduler().cancelTask(this.taskChat.get(player.getUniqueId()));
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
