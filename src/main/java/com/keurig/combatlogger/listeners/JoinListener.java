package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLoggerPlugin;
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
        Player player = event.getPlayer();

        if (!player.hasPermission("combatlogger.updatenotify"))
            return;

        plugin.getUpdater().getVersion(spigotVersion -> {
            int spigotVersionNumber = Integer.parseInt(spigotVersion.replace(".", ""));
            int pluginVersionNumber = Integer.parseInt(plugin.getDescription().getVersion().replace(".", ""));

            if (spigotVersionNumber > pluginVersionNumber) {
                Chat.msg(player, "&8(&6CombatLogger&8) &6There is a new update available!\n &fNew Version (&a" + spigotVersion + "&f) Your Version (&c" + plugin.getDescription().getVersion() + "&f)");

            } else if (pluginVersionNumber > spigotVersionNumber) {
                Chat.msg(player, "&8(&6CombatLogger&8) &6Houston we have a problem... &7Somehow we have time traveled with our version?\n&fFuture Version (&a" + plugin.getDescription().getVersion() + "&f) &fCurrent Version (&c" + spigotVersion + "&f)");
            }
        });
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
