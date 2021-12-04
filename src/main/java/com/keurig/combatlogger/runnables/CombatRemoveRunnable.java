package com.keurig.combatlogger.runnables;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class CombatRemoveRunnable extends BukkitRunnable {

    private final Map<UUID, CombatRemoveRunnable> loggedPlayers;

    private final Player player;

    @Getter
    private final Player target;

    public CombatRemoveRunnable(Map<UUID, CombatRemoveRunnable> loggedPlayers, Player player, Player target) {
        this.loggedPlayers = loggedPlayers;
        this.player = player;
        this.target = target;
    }

    @Override
    public void run() {
        player.sendMessage("CXOMBAT REMOVED HAHAHAHA");
        cancel();
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        loggedPlayers.remove(player.getUniqueId());
        super.cancel();
    }
}
