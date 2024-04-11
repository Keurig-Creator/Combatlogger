package com.keurig.combatlogger.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;

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
        Arrays.stream(messages).forEach(s -> sender.sendMessage(color(s)));
    }

    public static void log(String message) {
        CombatPlugin.getInstance().getLogger().log(Level.INFO, color(message));
    }

    public static String timeFormat(long millis) {
        return timeFormat(millis, false);
    }


    public static String timeFormat(long millis, boolean round) {
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;

        if ((millis % 1000) >= 10 && second > 0) {
            second++; // Increment seconds if milliseconds are greater than or equal to 10 and seconds are greater than 0
        }

        if (second >= 0.6 && second < 1) { // Rounding up if second is above 0.6
            second = 1;
        }

        if (minute > 0) {
            return String.format("%dm %ds", minute, second);
        } else if (second > 0) {
            return String.format("%ds", second);
        } else {
            if (round) {
                return "1s"; // Change 0s to 1s if rounding and seconds are 0
            } else {
                return String.format("%dms", millis);
            }
        }
    }
}
