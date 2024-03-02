package com.keurig.combatlogger.punishment.punishments;

import com.keurig.combatlogger.punishment.Punishment;
import com.keurig.combatlogger.utils.Chat;
import com.keurig.combatlogger.utils.CombatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class EcoPunishment extends Punishment {

    public static HashMap<UUID, String> joinMessages = new HashMap<>();

    public EcoPunishment() {
        super("ECO", 2);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Delay message
        Bukkit.getScheduler().runTaskLater(CombatPlugin.getInstance(), () -> {
            if (joinMessages.containsKey(player.getUniqueId())) {
                Chat.message(player, joinMessages.get(player.getUniqueId()));
                joinMessages.remove(player.getUniqueId());
            }
        }, 5);
    }

    @Override
    public void onQuit(String label, String[] args) {

        if (!CombatPlugin.getEconomyAPI().isEnabled()) {
            return;
        }

        String numberStr = args[0];
        int amount = 0;

        try {
            amount = Integer.parseInt(numberStr);

        } catch (NumberFormatException ignored) {
            Chat.log("Illegal string in eco punishment");
        }

        // deposit money into players account
        if (amount > 0) {
            CombatPlugin.getEconomyAPI().depositPlayer(getPlayer(), Math.abs(amount));
        } else if (amount < 0) { // withdraw money from players account
            CombatPlugin.getEconomyAPI().withdrawPlayer(getPlayer(), Math.abs(amount));
        }

        if (args.length > 1) {
            joinMessages.put(getPlayer().getUniqueId(), args[1]);
        }
    }
}
