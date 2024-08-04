package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.api.CombatLoggerAPI;
import com.keurig.combatlogger.utils.ConfigValue;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileListener implements Listener {
    @EventHandler
    public void onInteract(ProjectileLaunchEvent event) {
        if (ConfigValue.DISABLE_ENDER_PEARLS && event.getEntity().getType() == EntityType.ENDER_PEARL) {
            if (event.getEntity().getShooter() instanceof Player player) {
                if (!CombatLoggerAPI.isTagged(player))
                    return;

                if (CombatLogger.getInstance().spigotVersion >= 19) {
                    player.setCooldown(Material.ENDER_PEARL, 0);
                } else {
                    player.getItemInHand().setAmount(player.getItemInHand().getAmount() + 1);
                }

                event.setCancelled(true);
            }
        }
    }
}
