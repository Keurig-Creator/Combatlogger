package com.keurig.combatlogger.runnables;

import com.keurig.combatlogger.CombatLoggerPlugin;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CombatRemoveRunnable extends BukkitRunnable {

    private final Player player;

    @Getter
    private final Player target;

    public CombatRemoveRunnable(Player player, Player target) {
        this.player = player;
        this.target = target;
    }

    @Override
    public void run() {
        player.sendMessage("CXOMBAT REMOVED HAHAHAHA");
    }
}
