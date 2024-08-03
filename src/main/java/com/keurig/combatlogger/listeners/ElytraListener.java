package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.api.CombatLoggerAPI;
import com.keurig.combatlogger.utils.ConfigValue;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

public class ElytraListener implements Listener {

    @EventHandler
    public void onElytra(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!CombatLoggerAPI.isTagged(player))
                return;

            if (ConfigValue.DISABLE_ELYTRA) {
                event.setCancelled(true);
            }
        }
    }
}
