package com.keurig.combatlogger.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class Chat {

	public static String color(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public static void message(Player player, String... messages) {
		Arrays.stream(messages).forEach(player::sendMessage);
	}

	public static void message(CommandSender sender, String... messages) {
		Arrays.stream(messages).forEach(sender::sendMessage);
	}

	public static void log(String message) {
		Bukkit.getConsoleSender().sendMessage(color(message));
	}
}
