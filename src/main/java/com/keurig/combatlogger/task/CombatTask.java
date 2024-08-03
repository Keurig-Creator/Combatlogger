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

    public CombatTask(CombatPlayer combatPlayer, Player player) {
        this.combatPlayer = combatPlayer;
        this.player = player;
    }

    @Getter
    private int runtime;

    @Override
    public void run() {
        int timeRemaining = combatPlayer.getTimeRemaining(player);

        if (runtime == ConfigValue.COMBAT_TIMER) {
            combatPlayer.removeCombat(player);
            cancel();
        }

        // Update the action bar
        if (ConfigValue.ACTIONBAR_ENABLED) {
            String finalCombatOnActionBar = combatPlayer.getPlugin().replaceMsg(player, ConfigValue.ACTIONBAR_MESSAGE_ON);
            ActionBar.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', finalCombatOnActionBar));
        }

        // Update the chat interval
        if (!ConfigValue.CHAT_INTERVAL.isEmpty()) {
            long timeRemainingMillis = CombatLoggerAPI.timeRemaining(player);

            if (timeRemainingMillis > 0) { // Ensure non-negative time remaining
                if (ConfigValue.CHAT_INTERVAL.contains(timeRemaining)) {
                    Chat.message(player, combatPlayer.getPlugin().replaceMsg(player, ConfigValue.CHAT_INTERVAL_MESSAGE));
                }
            }
        }

        runtime++;
    }

}
