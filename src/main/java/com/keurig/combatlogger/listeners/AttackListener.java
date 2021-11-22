package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AttackListener implements Listener {

    private final CombatLogger plugin = CombatLogger.getInstance();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            final Player player = (Player) event.getDamager();
            final Player targer = (Player) event.getEntity();

            for (String world : plugin.getConfig().getStringList("blacklisted-worlds")) {
                if (player.getWorld().getName().equals(world)) {
                    return;
                }
            }

            this.plugin.getCombatPlayer().addCombat(player);
            this.plugin.getCombatPlayer().addCombat(targer);
        }
    }
}
