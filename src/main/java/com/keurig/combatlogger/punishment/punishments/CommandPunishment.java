package com.keurig.combatlogger.punishment.punishments;

import com.keurig.combatlogger.punishment.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandPunishment extends Punishment {

    public CommandPunishment() {
        super("COMMAND", 1);
    }

    @Override
    public void onQuit(String label) {
        final Player player = getPlayer();

        String message = getArgs().get("command").toString();

        message = message.replace("%player%", player.getName());

        if (player.hasPermission("combatlogger.admin"))
            return;

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message);
    }
}