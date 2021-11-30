package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLoggerPlugin;
import com.keurig.combatlogger.punishment.PunishmentHandler;
import com.keurig.combatlogger.runnables.CombatRemoveRunnable;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

@AllArgsConstructor()
public class JoinListener implements Listener {

    private CombatLoggerPlugin plugin;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getPunishmentHandler().runPunishments(event.getPlayer());
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player target && event.getEntity() instanceof Player attacker) {
            plugin.addLogged(attacker, target);
            plugin.addLogged(target, attacker);
        }
    }
}
