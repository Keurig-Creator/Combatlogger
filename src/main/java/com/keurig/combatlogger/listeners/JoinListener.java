package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLoggerPlugin;
import com.keurig.combatlogger.punishment.PunishmentHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final PunishmentHandler punishmentHandler;

    public JoinListener(PunishmentHandler punishmentHandler) {
        this.punishmentHandler = punishmentHandler;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        punishmentHandler.runPunishments(event.getPlayer());
        System.out.println(CombatLoggerPlugin.getAPI().isTagged(event.getPlayer()));
        System.out.println(CombatLoggerPlugin.getAPI().isTagged(event.getPlayer().getUniqueId()));
    }
}
