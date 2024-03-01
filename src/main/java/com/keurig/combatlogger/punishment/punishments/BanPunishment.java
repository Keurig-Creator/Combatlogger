package com.keurig.combatlogger.punishment.punishments;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.punishment.Punishment;
import com.keurig.combatlogger.utils.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BanPunishment extends Punishment {

    private String[] args;

    private final Map<UUID, Long> banned;

    public BanPunishment() {
        super("BAN", 2);

        this.banned = new HashMap<>();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (banned.containsKey(player.getUniqueId()) && banned.get(player.getUniqueId()) > System.currentTimeMillis()) {
            String message = args[1];
            message = message.replace("%combatlogger_timeformatted%", Chat.timeFormat(this.banned.get(player.getUniqueId()) - System.currentTimeMillis()));
            message = message.replace("{timeRemaining}", Chat.timeFormat(this.banned.get(player.getUniqueId()) - System.currentTimeMillis()));
            message = CombatLogger.getInstance().replaceMsg(player, message);
            player.kickPlayer(Chat.color(message));
        }
    }

    @Override
    public void onQuit(String label, String[] args) {
        final Player player = getPlayer();

        if (player.hasPermission("combatlogger.admin"))
            return;

        this.args = args;
        this.banned.put(player.getUniqueId(), System.currentTimeMillis() + (Integer.parseInt(args[0]) * 1000));
    }
}
