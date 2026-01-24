package com.keurig.combatlogger.punishment.punishments;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.punishment.Punishment;
import com.keurig.combatlogger.utils.Chat;
import com.keurig.combatlogger.utils.CombatPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
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
    public void onQuit(String label) {
        final Player player = getPlayer();

        if (player.hasPermission("combatlogger.admin"))
            return;

        Economy economy = CombatPlugin.getEconomyAPI();
        if (economy == null) {
            CombatLogger.getInstance().getLogger().warning("EcoPunishment: Economy API is null - is Vault installed with an economy plugin?");
            return;
        }

        Map<String, Object> args = getArgs();
        if (args == null || !args.containsKey("amount")) {
            CombatLogger.getInstance().getLogger().warning("EcoPunishment: Missing 'amount' in config for player " + player.getName());
            return;
        }

        String numberStr = args.get("amount").toString();

        int amount = 0;

        try {
            amount = Integer.parseInt(numberStr);
        } catch (NumberFormatException e) {
            CombatLogger.getInstance().getLogger().warning("EcoPunishment: Invalid amount '" + numberStr + "' for player " + player.getName());
            return;
        }

        // Use OfflinePlayer for better compatibility
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());

        // deposit money into players account
        if (amount > 0) {
            EconomyResponse response = economy.depositPlayer(offlinePlayer, Math.abs(amount));
            if (!response.transactionSuccess()) {
                CombatLogger.getInstance().getLogger().warning("EcoPunishment: Deposit failed for " + player.getName() + ": " + response.errorMessage);
            }
        } else if (amount < 0) { // withdraw money from players account
            EconomyResponse response = economy.withdrawPlayer(offlinePlayer, Math.abs(amount));
            if (!response.transactionSuccess()) {
                CombatLogger.getInstance().getLogger().warning("EcoPunishment: Withdraw failed for " + player.getName() + ": " + response.errorMessage);
            }
        }

        if (args.containsKey("message")) {
            joinMessages.put(player.getUniqueId(), (String) args.get("message"));
        }
    }
}
