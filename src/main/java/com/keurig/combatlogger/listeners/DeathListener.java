package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.handler.CombatPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final CombatLogger plugin = CombatLogger.getInstance();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        CombatPlayer combatPlayer = this.plugin.getCombatPlayer();

        // Remove combat tag when player dies (dying is not combat logging, so no punishment)
        if (combatPlayer.isTagged(player)) {
            combatPlayer.removeCombat(player);
        }
    }
}