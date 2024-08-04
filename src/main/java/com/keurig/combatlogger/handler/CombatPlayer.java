package com.keurig.combatlogger.handler;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.actionbar.ActionBar;
import com.keurig.combatlogger.event.PlayerEnterCombatEvent;
import com.keurig.combatlogger.event.PlayerLeaveCombatEvent;
import com.keurig.combatlogger.task.CombatTask;
import com.keurig.combatlogger.utils.Chat;
import com.keurig.combatlogger.utils.ConfigValue;
import dev.dejvokep.boostedyaml.YamlDocument;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class CombatPlayer {

    private final CombatLogger plugin;

    public final Map<UUID, CombatTask> tag = new HashMap<>();


    private final YamlDocument config;

    public CombatPlayer(CombatLogger plugin) {
        this.plugin = plugin;


        this.config = plugin.config;
    }

    public void addCombat(Player player) {
        if (player.hasPermission("combatlogger.admin") && player.getGameMode() == GameMode.CREATIVE)
            return;

        // Retag the player resets timer and wont display combat message again
        if (isTagged(player)) {
            removePlayer(player);
        } else {
            if (config.getBoolean("chat.enabled")) {
                Chat.message(player, ConfigValue.CHAT_MESSAGE_ON);

                PlayerEnterCombatEvent enterCombatEvent = new PlayerEnterCombatEvent(player);
                Bukkit.getPluginManager().callEvent(enterCombatEvent);
            }
        }

        if (ConfigValue.FORCE_GAMEMODE != null && !player.getGameMode().equals(ConfigValue.FORCE_GAMEMODE)) {
            player.setGameMode(ConfigValue.FORCE_GAMEMODE);
            Chat.message(player, ConfigValue.FORCE_GAMEMODE_MSG);
        }

        if (config.getBoolean("on-combat.disable-flight")) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }

        if (!this.plugin.isFactionsEnabled()) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }

        // Store player task
        CombatTask task = new CombatTask(this, player);
        task.runTaskTimer(plugin, 0, 20L);
        tag.put(player.getUniqueId(), task);
    }

    public void removeCombat(Player player) {
        if (ConfigValue.ACTIONBAR_ENABLED) {
            ActionBar.sendActionBar(player, ConfigValue.ACTIONBAR_MESSAGE_OFF);
        }

        if (ConfigValue.CHAT_ENABLED) {
            Chat.message(player, ConfigValue.CHAT_MESSAGE_OFF);
        }

        // Remove the player from the combat logged list.
        tag.remove(player.getUniqueId());

        // Call a custom event to signify that the player has left combat.
        PlayerLeaveCombatEvent leaveCombatEvent = new PlayerLeaveCombatEvent(player);
        Bukkit.getPluginManager().callEvent(leaveCombatEvent);
    }

    public void removePlayer(Player player) {
        if (tag.containsKey(player.getUniqueId())) {
            tag.get(player.getUniqueId()).cancel();
            tag.remove(player.getUniqueId());
        }
    }

    public boolean isTagged(Player player) {
        return tag.containsKey(player.getUniqueId());
    }

    public int getTimeRemaining(Player player) {
        if (this.tag.containsKey(player.getUniqueId()))
            return ConfigValue.COMBAT_TIMER - this.tag.get(player.getUniqueId()).getRuntime();

        return 0;
    }
}
