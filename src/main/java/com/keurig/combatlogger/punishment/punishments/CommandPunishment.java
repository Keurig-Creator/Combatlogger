package com.keurig.combatlogger.punishment.punishments;

import com.keurig.combatlogger.punishment.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public class CommandPunishment extends Punishment {

    public CommandPunishment() {
        super("COMMAND", 1);
    }

    @Override
    public void onQuit(String label) {
        final Player player = getPlayer();

        if (player.hasPermission("combatlogger.admin"))
            return;

        Map<String, Object> args = getArgs();
        if (args == null || !args.containsKey("command")) {
            return;
        }

        String message = args.get("command").toString();
        message = message.replace("%player%", player.getName());

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message);
    }
}