package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLogger;
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

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onHit(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player) {
			main.getCombatPlayer().addCombat((Player) e.getDamager(), (Player) e.getEntity());
		}
	}
}
