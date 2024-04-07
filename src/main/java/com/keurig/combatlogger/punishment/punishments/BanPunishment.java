package com.keurig.combatlogger.punishment.punishments;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.punishment.Punishment;
import com.keurig.combatlogger.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BanPunishment extends Punishment {


    public BanPunishment() {
        super("BAN", 2);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Chat.log("PLayer tried joining");

        BanInfo banInfo = BanInfo.get(player.getUniqueId());
        if (banInfo == null) {
            return;
        }

        if (banInfo.isBanned()) {
            event.setJoinMessage(Chat.color(banInfo.getMessage()));
            player.kickPlayer(Chat.color(banInfo.getMessage()));
        }

//        if (banned.containsKey(player.getUniqueId()) && banned.get(player.getUniqueId()) > System.currentTimeMillis()) {
//            String message = args[1];
//            message = message.replace("%combatlogger_timeformatted%", Chat.timeFormat(this.banned.get(player.getUniqueId()) - System.currentTimeMillis(), true));
//            message = message.replace("{timeRemaining}", Chat.timeFormat(this.banned.get(player.getUniqueId()) - System.currentTimeMillis(), true));
//            message = CombatLogger.getInstance().replaceMsg(player, message);
//            player.kickPlayer(Chat.color(message));
//        }
    }

    @Override
    public void onQuit(String label) {
        final Player player = getPlayer();

        if (player.hasPermission("combatlogger.admin"))
            return;

        String message = getArgs().get("message").toString();
        String seconds = getArgs().get("seconds").toString();

        BanInfo.create(player.getUniqueId(), message, System.currentTimeMillis() + (Integer.parseInt(seconds) * 1000));

    }
}


class BanInfo {
    private final UUID uuid;
    private final String message;
    private final Long time;

    private static final Map<UUID, BanInfo> banned = new HashMap<>();

    private BanInfo(UUID uuid, String message, Long time) {
        this.uuid = uuid;
        this.message = message;
        this.time = time;

        banned.put(uuid, this);
    }

    public boolean isBanned() {
        return time > System.currentTimeMillis();
    }

    public String getMessage() {
        Player player = Bukkit.getPlayer(uuid);

        if (player != null) {
            String msg = message;
            msg = msg.replace("%combatlogger_timeformatted%", Chat.timeFormat(time - System.currentTimeMillis(), true));
            msg = msg.replace("{timeRemaining}", Chat.timeFormat(time - System.currentTimeMillis(), true));
            msg = CombatLogger.getInstance().replaceMsg(player, msg);
            return msg;
        }

        return null;
    }

    public static BanInfo create(UUID uuid, String message, Long time) {
        return new BanInfo(uuid, message, time);
    }

    public static BanInfo get(UUID uuid) {
        return banned.get(uuid);
    }
}
