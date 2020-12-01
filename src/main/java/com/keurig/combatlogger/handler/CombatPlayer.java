package com.keurig.combatlogger.handler;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.actionbar.ActionBar;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class CombatPlayer {

	private final CombatLogger plugin;

	private final Map<UUID, Long> combatLogged;
	private final Set<UUID> players;
	private final Map<UUID, Integer> taskActionBar;
	private final Map<UUID, Integer> taskCombat;

	public CombatPlayer(CombatLogger plugin) {
		this.plugin = plugin;

		this.combatLogged = new HashMap<>();
		this.taskActionBar = new HashMap<>();
		this.taskCombat = new HashMap<>();
		this.players = new HashSet<>();

		addOnlinePlayers();
	}

	private void addOnlinePlayers() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			this.players.add(player.getUniqueId());
		}
	}

	public void addPlayer(Player player) {
		this.players.add(player.getUniqueId());
	}

	public void removePlayer(Player player) {

		this.players.remove(player.getUniqueId());
		this.combatLogged.remove(player.getUniqueId());

		if (this.taskCombat.get(player.getUniqueId()) != null)
			stopTasks(player);
	}

	public void addCombat(Player player) {
		final int combatTimer = this.plugin.getConfig().getInt("combat-timer");

		final boolean useChat = this.plugin.getConfig().getBoolean("chat.use");
		final String combatOnChat = this.plugin.getConfig().getString("chat.on-message");
		final String combatOffChat = this.plugin.getConfig().getString("chat.off-message");


		final boolean useActionBar = this.plugin.getConfig().getBoolean("actionbar.use");
		final String combatOnActionBar = this.plugin.getConfig().getString("actionbar.on-message");
		final String combatOffActionBar = this.plugin.getConfig().getString("actionbar.off-message");

		if (isTagged(player)) {
			stopTasks(player);
		} else {
			if (useChat) {
				assert combatOnChat != null;
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', combatOnChat));
			}
		}

		if (useActionBar)
			this.taskActionBar.put(player.getUniqueId(), Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
				assert combatOnActionBar != null;
				ActionBar.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', combatOnActionBar.replace("{timeRemaining}", String.valueOf((combatTimeRemaining(player) / 1000) + 1))));
			}, 0, 0));

		this.combatLogged.put(player.getUniqueId(), System.currentTimeMillis() + (combatTimer * 1000));
		this.taskCombat.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
			Bukkit.getScheduler().cancelTask(this.taskActionBar.get(player.getUniqueId()));
			if (this.combatLogged.containsKey(player.getUniqueId())) {
				if (useActionBar) {
					assert combatOffActionBar != null;
					ActionBar.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', combatOffActionBar));
				}

				if (useChat) {
					assert combatOffChat != null;
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', combatOffChat));
				}
				this.combatLogged.remove(player.getUniqueId());
			}

		}, 20 * combatTimer).getTaskId());

	}

	private void stopTasks(Player player) {
		Bukkit.getScheduler().cancelTask(this.taskActionBar.get(player.getUniqueId()));
		Bukkit.getScheduler().cancelTask(this.taskCombat.get(player.getUniqueId()));
	}

	private boolean isTagged(Player player) {
		return this.combatLogged.containsKey(player.getUniqueId()) && this.combatLogged.get(player.getUniqueId()) > System.currentTimeMillis();
	}

	private long combatTimeRemaining(Player player) {
		return this.combatLogged.get(player.getUniqueId()) - System.currentTimeMillis();
	}

	/**
	 * Add user to combat
	 *
	 * @param player the player that hit the target.
	 * @param target the target the player is in combat with
	 * @deprecated redoing combat system
	 */
	public void addCombat(final Player player, final Player target) {

		// Get combat message
		final String inCombat = this.plugin.getConfig().getString("combat-message");
		assert inCombat != null;

		if (this.combatLogged.containsKey(player.getUniqueId()) && this.combatLogged.get(player.getUniqueId()) > System.currentTimeMillis()) {
			Bukkit.getScheduler().cancelTask(this.taskCombat.get(player.getUniqueId()));
		} else {
			if (!player.hasPermission("combatlogger.admin"))
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', inCombat));
		}

		if (this.combatLogged.containsKey(target.getUniqueId()) && this.combatLogged.get(target.getUniqueId()) > System.currentTimeMillis()) {
			Bukkit.getScheduler().cancelTask(this.taskCombat.get(target.getUniqueId()));
		} else {
			if (!target.hasPermission("combatlogger.admin"))
				target.sendMessage(ChatColor.translateAlternateColorCodes('&', inCombat));
		}

		// Get combat timer
		final int combatTimer = this.plugin.getConfig().getInt("combat-timer");

		// Get combat off message
		final String outOfCombat = this.plugin.getConfig().getString("combat-off-message");
		assert outOfCombat != null;

		// Add attacker and target to combatLogged
		if (!player.hasPermission("combatlogger.admin")) {
			this.combatLogged.put(player.getUniqueId(), System.currentTimeMillis() + (combatTimer * 1000));

			this.taskCombat.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
				if (CombatPlayer.this.combatLogged.containsKey(player.getUniqueId())) {
					CombatPlayer.this.combatLogged.remove(player.getUniqueId());
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', outOfCombat));
				}
			}, 20 * combatTimer).getTaskId());
		}
		if (!target.hasPermission("combatlogger.admin")) {
			this.combatLogged.put(target.getUniqueId(), System.currentTimeMillis() + (combatTimer * 1000));

			this.taskCombat.put(target.getUniqueId(), Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
				if (CombatPlayer.this.combatLogged.containsKey(target.getUniqueId())) {
					CombatPlayer.this.combatLogged.remove(target.getUniqueId());
					target.sendMessage(ChatColor.translateAlternateColorCodes('&', outOfCombat));
				}
			}, 20 * combatTimer).getTaskId());
		}

	}
}
