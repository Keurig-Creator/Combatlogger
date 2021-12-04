package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLoggerPlugin;
import com.keurig.combatlogger.permission.Permission;
import com.keurig.combatlogger.runnables.CombatRemoveRunnable;
import com.keurig.combatlogger.utils.Chat;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor()
public class JoinListener implements Listener {

    private CombatLoggerPlugin plugin;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        System.out.println(plugin.getPermissionHandler().getPermission(event.getPlayer()).getPermission());
        Player player = event.getPlayer();

        Permission permission = plugin.getPermissionHandler().getPermission(player);

        Chat.msg(player, permission.getChatMessage().getOn());
        Chat.msg(player, permission.getChatMessage().getOff());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        CombatRemoveRunnable combat = plugin.getLoggedPlayers().get(event.getPlayer().getUniqueId());

        if (combat == null) return;

        combat.cancel();
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
