package com.keurig.combatlogger.punishment.punishments;

import com.keurig.combatlogger.punishment.Punishment;
import com.keurig.combatlogger.utils.Chat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPreLoginEvent;

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
    public void onPreJoin(PlayerPreLoginEvent event) {
        if (this.banned.containsKey(event.getUniqueId()) && this.banned.get(event.getUniqueId()) > System.currentTimeMillis())
            event.disallow(PlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&',
                    this.args[1].replace("{timeRemaining}", Chat.timeFormat(this.banned.get(event.getUniqueId()) - System.currentTimeMillis()))));
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
