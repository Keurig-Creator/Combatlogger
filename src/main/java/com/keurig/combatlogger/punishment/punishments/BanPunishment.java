package com.keurig.combatlogger.punishment.punishments;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.punishment.Punishment;
import com.keurig.combatlogger.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BanPunishment extends Punishment {

    // Track players being kicked for combat log ban (to hide their quit message)
    private static final Set<UUID> pendingKicks = new HashSet<>();

    public static boolean isPendingKick(UUID uuid) {
        return pendingKicks.remove(uuid);
    }

    public BanPunishment() {
        super("BAN", 2);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        // Admins bypass combat log ban
        if (player.hasPermission("combatlogger.admin")) {
            BanInfo.remove(player.getUniqueId());
            return;
        }

        BanInfo banInfo = BanInfo.get(player.getUniqueId());
        if (banInfo == null) {
            return;
        }

        if (banInfo.isBanned()) {
            // Deny login entirely - prevents PlayerJoinEvent from firing
            String message = banInfo.getMessageForLogin(player);
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Chat.color(message != null ? message : "You are banned for combat logging."));
        } else {
            // Ban expired, remove it
            BanInfo.remove(player.getUniqueId());
        }
    }

    @Override
    public void onQuit(String label) {
        final Player player = getPlayer();

        if (player.hasPermission("combatlogger.admin"))
            return;

        Map<String, Object> args = getArgs();
        if (args == null || !args.containsKey("message") || !args.containsKey("seconds")) {
            CombatLogger.getInstance().getLogger().warning("BanPunishment: Missing 'message' or 'seconds' in config for player " + player.getName());
            return;
        }

        String message = args.get("message").toString();
        String seconds = args.get("seconds").toString();

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
        return getMessageForLogin(player);
    }

    public String getMessageForLogin(Player player) {
        String msg = message;
        msg = msg.replace("%combatlogger_timeformatted%", Chat.timeFormat(time - System.currentTimeMillis(), true));
        msg = msg.replace("{timeRemaining}", Chat.timeFormat(time - System.currentTimeMillis(), true));

        if (player != null) {
            msg = CombatLogger.getInstance().replaceMsg(player, msg);
        }

        return msg;
    }

    public static BanInfo create(UUID uuid, String message, Long time) {
        return new BanInfo(uuid, message, time);
    }

    public static BanInfo get(UUID uuid) {
        return banned.get(uuid);
    }

    public static void remove(UUID uuid) {
        banned.remove(uuid);
    }
}
