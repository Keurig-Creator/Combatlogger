package com.keurig.combatlogger.handler;

import com.keurig.combatlogger.CombatLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class CombatPlayer {

	private final CombatLogger main;

	private Map<UUID, Long> combatLogged;

	private List<Player> players;

	private int task;

	public CombatPlayer(CombatLogger main) {
		this.main = main;

		combatLogged = new HashMap<UUID, Long>();
		players = new ArrayList<Player>();

		addOnlinePlayers();
	}

	public void addCombat(final Player player, final Player target) {

		if (combatLogged.containsKey(player.getUniqueId()) && combatLogged.get(player.getUniqueId()) > System.currentTimeMillis() && combatLogged.containsKey(target.getUniqueId()) && combatLogged.get(target.getUniqueId()) > System.currentTimeMillis()) {
			Bukkit.getScheduler().cancelTask(task);
		} else {
			final String inCombat = main.getConfig().getString("combat-message");

			player.sendMessage(ChatColor.translateAlternateColorCodes('&', inCombat));
			target.sendMessage(ChatColor.translateAlternateColorCodes('&', inCombat));
		}

		final int combatTimer = main.getConfig().getInt("combat-timer");

		combatLogged.put(player.getUniqueId(), System.currentTimeMillis() + (combatTimer * 1000));
		combatLogged.put(target.getUniqueId(), System.currentTimeMillis() + (combatTimer * 1000));

		final String outOfCombat = main.getConfig().getString("combat-off-message");

		task = Bukkit.getScheduler().runTaskLater(main, new Runnable() {
			public void run() {
				if (combatLogged.containsKey(player.getUniqueId())) {
					combatLogged.remove(player.getUniqueId());
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', outOfCombat));
				}

				if (combatLogged.containsKey(target.getUniqueId())) {
					combatLogged.remove(target.getUniqueId());
					target.sendMessage(ChatColor.translateAlternateColorCodes('&', outOfCombat));
				}

			}
		}, 20 * combatTimer).getTaskId();

	}

	public void addOnlinePlayers() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!players.contains(player))
				players.add(player);
		}
	}

	public void addPlayer(Player player) {
		if (!players.contains(player))
			players.add(player);
	}

	public void removePlayer(Player player) {
		if (players.contains(player)) {
			players.remove(player);
		}
	}

	public Player getPlayer(Player player) {
		if (players.contains(player)) {
			return player;
		}

		return null;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public Map<UUID, Long> getCombatLogged() {
		return combatLogged;
	}
}
