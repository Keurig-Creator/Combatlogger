package com.keurig.combatlogger.handler;

import com.keurig.combatlogger.CombatLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class CombatPlayer {

	private final CombatLogger main;

	private final Map<UUID, Long> combatLogged;

	private final Set<UUID> players;

	private final Map<UUID, Integer> task;

	public CombatPlayer(CombatLogger main) {
		this.main = main;

		combatLogged = new HashMap<UUID, Long>();
		task = new HashMap<UUID, Integer>();

		players = new HashSet<UUID>();

		addOnlinePlayers();
	}

	public void addCombat(final Player player, final Player target) {

		// Get combat message
		final String inCombat = main.getConfig().getString("combat-message");
		assert inCombat != null;

		if (combatLogged.containsKey(player.getUniqueId()) && combatLogged.get(player.getUniqueId()) > System.currentTimeMillis()) {
			Bukkit.getScheduler().cancelTask(task.get(player.getUniqueId()));
		} else {
			if (!player.hasPermission("combatlogger.admin"))
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', inCombat));
		}

		if (combatLogged.containsKey(target.getUniqueId()) && combatLogged.get(target.getUniqueId()) > System.currentTimeMillis()) {
			Bukkit.getScheduler().cancelTask(task.get(target.getUniqueId()));
		} else {
			if (!target.hasPermission("combatlogger.admin"))
				target.sendMessage(ChatColor.translateAlternateColorCodes('&', inCombat));
		}

		// Get combat timer
		final int combatTimer = main.getConfig().getInt("combat-timer");

		// Get combat off message
		final String outOfCombat = main.getConfig().getString("combat-off-message");
		assert outOfCombat != null;


		// Add attacker and target to combatLogged
		if (!player.hasPermission("combatlogger.admin")) {
			combatLogged.put(player.getUniqueId(), System.currentTimeMillis() + (combatTimer * 1000));

			task.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(main, new Runnable() {
				public void run() {
					if (combatLogged.containsKey(player.getUniqueId())) {
						combatLogged.remove(player.getUniqueId());
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', outOfCombat));
					}
				}
			}, 20 * combatTimer).getTaskId());
		}
		if (!target.hasPermission("combatlogger.admin")) {
			combatLogged.put(target.getUniqueId(), System.currentTimeMillis() + (combatTimer * 1000));

			task.put(target.getUniqueId(), Bukkit.getScheduler().runTaskLater(main, new Runnable() {
				public void run() {
					if (combatLogged.containsKey(target.getUniqueId())) {
						combatLogged.remove(target.getUniqueId());
						target.sendMessage(ChatColor.translateAlternateColorCodes('&', outOfCombat));
					}
				}
			}, 20 * combatTimer).getTaskId());
		}

	}

	public void addOnlinePlayers() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!players.contains(player.getUniqueId()))
				players.add(player.getUniqueId());
		}
	}

	public void addPlayer(Player player) {
		if (!players.contains(player.getUniqueId()))
			players.add(player.getUniqueId());
	}

	public void removePlayer(Player player) {

		Bukkit.getScheduler().cancelTask(task.get(player.getUniqueId()));

		if (combatLogged.containsKey(player.getUniqueId())) {
			combatLogged.remove(player.getUniqueId());
		}

		if (players.contains(player.getUniqueId())) {
			players.remove(player.getUniqueId());
		}

	}

	public Player getPlayer(Player player) {
		if (players.contains(player.getUniqueId())) {
			return player;
		}
		return null;
	}

	public Set<UUID> getPlayers() {
		return players;
	}

	public Map<UUID, Long> getCombatLogged() {
		return combatLogged;
	}
}
