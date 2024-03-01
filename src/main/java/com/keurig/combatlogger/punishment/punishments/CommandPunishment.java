package com.keurig.combatlogger.punishment.punishments;

import com.keurig.combatlogger.punishment.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandPunishment extends Punishment {

    public CommandPunishment() {
        super("COMMAND", 1);
    }

    @Override
    public void onQuit(String label, String[] args) {
        final Player player = getPlayer();

        String message = args[0];
        message = message.replace("{player}", player.getName());

        if (player.hasPermission("combatlogger.admin"))
            return;


        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message);
    }
}