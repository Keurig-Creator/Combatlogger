package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.utils.ConfigValue;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AttackListener implements Listener {

    private final CombatLogger plugin = CombatLogger.getInstance();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {

        if (event.getEntity() instanceof Player) {
            final Player target = (Player) event.getEntity();
            Player player = null;


            if (event.getDamager() instanceof Projectile projectile) {

                if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                    player = (Player) ((Projectile) event.getDamager()).getShooter();
                }

                // Ignore certain projectiles for example snowballs
                if (ConfigValue.IGNORED_PROJECTILES.contains(projectile.getType())) {
                    return;
                }
            } else if (event.getDamager() instanceof Player) {
                player = (Player) event.getDamager();
            }

            if (player == null) {
                return;
            }

            if (player == target) {
                return;
            }

            for (String world : plugin.getConfig().getStringList("blacklisted-worlds")) {
                if (player.getWorld().getName().equals(world)) {
                    return;
                }
            }

            this.plugin.getCombatPlayer().addCombat(player);
            this.plugin.getCombatPlayer().addCombat(target);
        }
    }

}
