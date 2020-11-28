package com.keurig.combatlogger.handler;

import com.keurig.combatlogger.CombatLogger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class CombatPlayer {

	private final CombatLogger main;

	private final Map<UUID, Long> combatLogged;
	private final Set<UUID> players;
	private final Map<UUID, Integer> task;

	public CombatPlayer(CombatLogger main) {
		this.main = main;

		this.combatLogged = new HashMap<>();
		this.task = new HashMap<>();
		this.players = new HashSet<>();

		addOnlinePlayers();
	}

	public void addCombat(final Player player, final Player target) {

		// Get combat message
		final String inCombat = this.main.getConfig().getString("combat-message");
		assert inCombat != null;

		if (this.combatLogged.containsKey(player.getUniqueId()) && this.combatLogged.get(player.getUniqueId()) > System.currentTimeMillis()) {
			Bukkit.getScheduler().cancelTask(this.task.get(player.getUniqueId()));
		} else {
			if (!player.hasPermission("combatlogger.admin"))
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', inCombat));
		}

		if (this.combatLogged.containsKey(target.getUniqueId()) && this.combatLogged.get(target.getUniqueId()) > System.currentTimeMillis()) {
			Bukkit.getScheduler().cancelTask(this.task.get(target.getUniqueId()));
		} else {
			if (!target.hasPermission("combatlogger.admin"))
				target.sendMessage(ChatColor.translateAlternateColorCodes('&', inCombat));
		}

		// Get combat timer
		final int combatTimer = this.main.getConfig().getInt("combat-timer");

		// Get combat off message
		final String outOfCombat = this.main.getConfig().getString("combat-off-message");
		assert outOfCombat != null;

		// Add attacker and target to combatLogged
		if (!player.hasPermission("combatlogger.admin")) {
			this.combatLogged.put(player.getUniqueId(), System.currentTimeMillis() + (combatTimer * 1000));

			this.task.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(this.main, () -> {
				if (CombatPlayer.this.combatLogged.containsKey(player.getUniqueId())) {
					CombatPlayer.this.combatLogged.remove(player.getUniqueId());
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', outOfCombat));
				}
			}, 20 * combatTimer).getTaskId());
		}
		if (!target.hasPermission("combatlogger.admin")) {
			this.combatLogged.put(target.getUniqueId(), System.currentTimeMillis() + (combatTimer * 1000));

			this.task.put(target.getUniqueId(), Bukkit.getScheduler().runTaskLater(this.main, () -> {
				if (CombatPlayer.this.combatLogged.containsKey(target.getUniqueId())) {
					CombatPlayer.this.combatLogged.remove(target.getUniqueId());
					target.sendMessage(ChatColor.translateAlternateColorCodes('&', outOfCombat));
				}
			}, 20 * combatTimer).getTaskId());
		}

	}

	public void addOnlinePlayers() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			this.players.add(player.getUniqueId());
		}
	}

	public void addPlayer(Player player) {
		this.players.add(player.getUniqueId());
	}

	public void removePlayer(Player player) {

		Bukkit.getScheduler().cancelTask(this.task.get(player.getUniqueId()));

		this.combatLogged.remove(player.getUniqueId());

		this.players.remove(player.getUniqueId());
	}

	public boolean isTagged(Player player) {
		return this.combatLogged.containsKey(player.getUniqueId());
	}
}
