package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.api.CombatLoggerAPI;
import com.keurig.combatlogger.utils.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

    private CombatLogger plugin;

    public CommandListener(CombatLogger plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {

        Player player = event.getPlayer();
        String message = event.getMessage();

        if (!CombatLoggerAPI.isTagged(player)) {
            return;
        }

        String str = plugin.replaceMsg(player, plugin.getConfig().getString("blacklisted-command-message"));

        for (String command : plugin.getConfig().getStringList("blacklisted-commands")) {
            if (message.toLowerCase().contains("/" + command.toLowerCase())) {
                Chat.message(player, str);
                event.setCancelled(true);
                return;
            }
        }
    }

}
