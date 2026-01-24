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

import java.util.*;

@Getter
public class CombatPlayer {

    private final CombatLogger plugin;

    // Victim -> task
    public final Map<UUID, CombatTask> tag = new HashMap<>();

    // Victim -> who tagged them (null = no player / unknown)
    private final Map<UUID, UUID> taggedBy = new HashMap<>();

    // Victim -> when they were tagged (millis)
    private final Map<UUID, Long> taggedAt = new HashMap<>();

    private final YamlDocument config;

    public CombatPlayer(CombatLogger plugin) {
        this.plugin = plugin;
        this.config = plugin.config;
    }

    /**
     * Old behavior: tags with no known attacker.
     */
    public void addCombat(Player player) {
        addCombat(player, (UUID) null);
    }

    /**
     * Tags victim and stores attacker UUID (can be null).
     */
    public void addCombat(Player victim, Player attacker) {
        addCombat(victim, attacker == null ? null : attacker.getUniqueId());
    }

    /**
     * Tags victim and stores attacker UUID (can be null).
     */
    public void addCombat(Player player, UUID attackerUuid) {
        if (player.hasPermission("combatlogger.admin") && player.getGameMode() == GameMode.CREATIVE)
            return;

        // Retag resets timer and wont display combat message again
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

        // Store who tagged who
        taggedBy.put(player.getUniqueId(), attackerUuid);
        taggedAt.put(player.getUniqueId(), System.currentTimeMillis());

        // Add walls - notify wall manager player entered combat
        if (plugin.getCombatWallManager() != null) {
            plugin.getCombatWallManager().onCombatStart(player);
        }
    }

    public void removeCombat(Player player) {
        if (ConfigValue.ACTIONBAR_ENABLED) {
            ActionBar.sendActionBar(player, ConfigValue.ACTIONBAR_MESSAGE_OFF);
        }

        if (ConfigValue.CHAT_ENABLED) {
            Chat.message(player, ConfigValue.CHAT_MESSAGE_OFF);
        }

        // Remove the player from the combat logged list first
        removePlayer(player);

        // Call a custom event to signify that the player has left combat
        PlayerLeaveCombatEvent leaveCombatEvent = new PlayerLeaveCombatEvent(player);
        Bukkit.getPluginManager().callEvent(leaveCombatEvent);

        // Remove walls - notify wall manager player left combat
        if (plugin.getCombatWallManager() != null) {
            plugin.getCombatWallManager().onCombatEnd(player);
        }
    }

    public void removePlayer(Player player) {
        UUID id = player.getUniqueId();

        if (tag.containsKey(id)) {
            CombatTask task = tag.get(id);
            if (task != null) {
                task.cancel();
            }
            tag.remove(id);
        }

        // Clear tag metadata too
        taggedBy.remove(id);
        taggedAt.remove(id);
    }

    public boolean isTagged(Player player) {
        return tag.containsKey(player.getUniqueId());
    }

    public int getTimeRemaining(Player player) {
        if (player == null) return -1;

        CombatTask task = tag.get(player.getUniqueId());
        if (task == null) return -1;

        int remaining = ConfigValue.COMBAT_TIMER - task.getRuntime();
        return Math.max(0, remaining); // in combat: 0..timer
    }

    /* =========================
       API: who tagged who
       ========================= */

    /**
     * Returns the UUID of the player who most recently tagged this victim.
     * Null if unknown / not player-caused / not tagged.
     */
    public UUID getTagger(Player victim) {
        if (victim == null) return null;
        return taggedBy.get(victim.getUniqueId());
    }

    /**
     * Returns true if victim is currently tagged and was tagged by attacker.
     */
    public boolean isTaggedBy(Player victim, Player attacker) {
        if (victim == null || attacker == null) return false;
        UUID tagger = taggedBy.get(victim.getUniqueId());
        return tagger != null && tagger.equals(attacker.getUniqueId());
    }

    /**
     * When the victim was tagged (millis). Returns -1 if not tagged / unknown.
     */
    public long getTaggedAt(Player victim) {
        if (victim == null) return -1L;
        return taggedAt.getOrDefault(victim.getUniqueId(), -1L);
    }

    /**
     * Returns a list of victim UUIDs currently tagged by attacker.
     */
    public List<UUID> getVictimsTaggedBy(Player attacker) {
        if (attacker == null) return Collections.emptyList();
        UUID attackerId = attacker.getUniqueId();

        List<UUID> out = new java.util.ArrayList<>();
        for (Map.Entry<UUID, UUID> entry : taggedBy.entrySet()) {
            if (attackerId.equals(entry.getValue())) {
                out.add(entry.getKey());
            }
        }
        return out;
    }
}
