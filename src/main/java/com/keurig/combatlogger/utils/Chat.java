package com.keurig.combatlogger.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public class Chat {

    public static TempHashSet<UUID> temp = new TempHashSet<>(1000);

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void message(Player player, String... messages) {
        Arrays.stream(messages).forEach(s -> player.sendMessage(color(s)));
    }

    public static void message(Player player, double seconds, String... messages) {
        if (temp.contains(player.getUniqueId())) {
            return;
        }

        temp.add(player.getUniqueId());
        Arrays.stream(messages).forEach(s -> player.sendMessage(color(s)));
    }

    public static void message(CommandSender sender, String... messages) {
        Arrays.stream(messages).forEach(sender::sendMessage);
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(color(message));
    }

    public static String timeFormat(long milis) {

        long second = (milis / 1000) % 60;
        long minute = (milis / (1000 * 60)) % 60;

        if (minute > 0) {
            return String.format("%dm %ds", minute, second);
        } else if (second > 0) {
            return String.format("%ds", second);
        } else
            return String.format("%dms", milis);
    }
}
