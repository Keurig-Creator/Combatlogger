package com.keurig.combatlogger.command;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.utils.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CombatLoggerCommand implements CommandExecutor, TabCompleter {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CombatLogger plugin = CombatLogger.getInstance();

        if (!sender.hasPermission("combatlogger.command") && !sender.isOp()) {
            Chat.message(sender, "&cYou do not have permission to use this command.");
            return false;
        }

        if (args.length > 0) {
            String type = args[0];

            if (type.equalsIgnoreCase("reload") || type.equalsIgnoreCase("rl")) {
                plugin.reloadConfig();
                
                HandlerList.unregisterAll(plugin);
                CombatLogger.getInstance().registerEvents();
                Chat.message(sender, "&eReloaded configuration...");
            } else {
                Chat.message(sender, "&cInvalid arguments. Usage: /combatlogger reload");
            }
        } else {
            Chat.message(sender, "&cInvalid arguments. Usage: /combatlogger reload");
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of("reload");
    }
}
