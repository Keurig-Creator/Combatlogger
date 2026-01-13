package com.keurig.combatlogger.task;

import com.keurig.combatlogger.actionbar.ActionBar;
import com.keurig.combatlogger.api.CombatLoggerAPI;
import com.keurig.combatlogger.handler.CombatPlayer;
import com.keurig.combatlogger.utils.Chat;
import com.keurig.combatlogger.utils.ConfigValue;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CombatTask extends BukkitRunnable {

    private final CombatPlayer combatPlayer;
    private final Player player;

    @Getter
    private int runtime;

    public CombatTask(CombatPlayer combatPlayer, Player player) {
        this.combatPlayer = combatPlayer;
        this.player = player;
    }

    @Override
    public void run() {
        // If player went offline, stop cleanly
        if (player == null || !player.isOnline()) {
            cancel();
            return;
        }

        int timeRemaining = combatPlayer.getTimeRemaining(player); // may be -1 by design

        /*
         * End combat BEFORE rendering UI so we never show 0 or -1.
         * runtime starts at 0, so when runtime >= timer, combat is done.
         */
        if (runtime >= ConfigValue.COMBAT_TIMER) {
            // Clear actionbar to avoid lingering UI
            if (ConfigValue.ACTIONBAR_ENABLED) {
                ActionBar.sendActionBar(player, "");
            }

            combatPlayer.removeCombat(player);
            cancel();
            return;
        }

        /*
         * Actionbar rules:
         * - Keep getTimeRemaining() returning -1 for API cleanliness.
         * - Never display -1.
         * - Show "1" once, then disappear before it would display 0.
         */
        if (ConfigValue.ACTIONBAR_ENABLED) {
            if (timeRemaining == -1) {
                // Not in combat (API signal), ensure bar is cleared
                ActionBar.sendActionBar(player, "");
            } else if (timeRemaining > 1) {
                // Normal display: 2..timer
                String msg = combatPlayer.getPlugin().replaceMsg(player, ConfigValue.ACTIONBAR_MESSAGE_ON);
                ActionBar.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', msg));
            } else if (timeRemaining == 1) {
                // Show 1 once (this tick), next tick runtime will hit timer and we clear+end
                String msg = combatPlayer.getPlugin().replaceMsg(player, ConfigValue.ACTIONBAR_MESSAGE_ON);
                ActionBar.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', msg));
            } else {
                // timeRemaining <= 0 should never be shown, clear it
                ActionBar.sendActionBar(player, "");
            }
        }

        // Chat interval messages (unchanged, but don’t rely on negative/invalid time)
        if (!ConfigValue.CHAT_INTERVAL.isEmpty() && timeRemaining > 0) {
            long timeRemainingMillis = CombatLoggerAPI.timeRemaining(player);
            if (timeRemainingMillis > 0 && ConfigValue.CHAT_INTERVAL.contains(timeRemaining)) {
                Chat.message(player, combatPlayer.getPlugin().replaceMsg(player, ConfigValue.CHAT_INTERVAL_MESSAGE));
            }
        }

        runtime++;
    }
}
