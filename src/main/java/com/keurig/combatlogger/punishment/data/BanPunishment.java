package com.keurig.combatlogger.punishment.data;

import com.keurig.combatlogger.punishment.Punishment;
import com.keurig.combatlogger.punishment.PunishmentHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BanPunishment extends Punishment {

    private final Map<UUID, Long> bannedList = new HashMap<>();

    public BanPunishment(PunishmentHandler handler) {
        super(handler, "ban");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (bannedList.containsKey(player.getUniqueId()) && bannedList.get(player.getUniqueId()) >= System.currentTimeMillis()) {
            player.kickPlayer(getString("message"));
            event.setJoinMessage(null);
        }
    }

    @Override
    public void runPunishment() {

        System.out.println("SENDING");
        System.out.println(getString("message"));
        System.out.println(getInt("time"));
        getPlayer().sendMessage("Sheesh" + getString("message"));
    }
}
