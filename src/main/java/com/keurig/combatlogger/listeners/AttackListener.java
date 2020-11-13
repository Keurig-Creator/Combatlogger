package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AttackListener implements Listener {

	private final CombatLogger main;

	public AttackListener(CombatLogger main) {
		this.main = main;
	}

	private int task;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onHit(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player) {

			final Player player = (Player) e.getDamager();
			final Player target = (Player) e.getEntity();

			if (main.getCombatLogged().containsKey(player) && main.getCombatLogged().get(player) > System.currentTimeMillis()) {
				Bukkit.getScheduler().cancelTask(task);
			} else {
				final String inCombat = main.getConfig().getString("combat-message");

				player.sendMessage(ChatColor.translateAlternateColorCodes('&', inCombat));
				target.sendMessage(ChatColor.translateAlternateColorCodes('&', inCombat));
				
			}

			final int combatTimer = main.getConfig().getInt("combat-timer");

			main.getCombatLogged().put(player, System.currentTimeMillis() + (combatTimer * 1000));
			main.getCombatLogged().put(target, System.currentTimeMillis() + (combatTimer * 1000));

			final String outOfCombat = main.getConfig().getString("combat-off-message");

			task = Bukkit.getScheduler().runTaskLater(main, new Runnable() {
				public void run() {
					if (main.getCombatLogged().containsKey(player)) {
						main.getCombatLogged().remove(player);
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', outOfCombat));
					}
					if (main.getCombatLogged().containsKey(target)) {
						main.getCombatLogged().remove(target);
						target.sendMessage(ChatColor.translateAlternateColorCodes('&', outOfCombat));
					}
				}
			}, 20 * combatTimer).getTaskId();

		}
	}
}
