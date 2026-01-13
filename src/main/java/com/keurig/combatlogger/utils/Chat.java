package com.keurig.combatlogger.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chat {

    public static TempHashSet<UUID> temp = new TempHashSet<>(1000);

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String color(String message) {
        if (message == null) return null;

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1); // e.g. 12d0de
            String replacement = toMinecraftHex(hex);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static String colorize(String input, boolean allowHex) {
        if (input == null) return "";

        String msg = input;

        // Convert hex to §x§R§R§G§G§B§B for 1.16+ only
        if (allowHex) {
            Matcher matcher = HEX_PATTERN.matcher(msg);
            StringBuffer buffer = new StringBuffer();

            while (matcher.find()) {
                String hex = matcher.group(1);
                StringBuilder replacement = new StringBuilder("§x");
                for (char c : hex.toCharArray()) {
                    replacement.append('§').append(c);
                }
                matcher.appendReplacement(buffer, replacement.toString());
            }
            matcher.appendTail(buffer);
            msg = buffer.toString();
        } else {
            // 1.8: hex not supported, remove it so it doesn't show raw
            msg = HEX_PATTERN.matcher(msg).replaceAll("");
        }

        // Convert legacy & codes to §
        msg = ChatColor.translateAlternateColorCodes('&', msg);

        return msg;
    }

    // 1.16+ detection that doesn't rely on NMS strings
    public static boolean supportsHex() {
        try {
            ChatColor.class.getDeclaredMethod("of", String.class);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    private static String toMinecraftHex(String hex) {
        // turns "12d0de" into "§x§1§2§d§0§d§e"
        StringBuilder out = new StringBuilder("§x");
        for (char c : hex.toCharArray()) {
            out.append('§').append(c);
        }
        return out.toString();
    }

    public static String strip(String s) {
        if (s == null) return "";
        return ChatColor.stripColor(color(s));
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
                return String.format("%ds", millis);
            }
        }
    }
}