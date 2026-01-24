package com.keurig.combatlogger.punishment.punishments;

import com.keurig.combatlogger.punishment.Punishment;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KillPunishment extends Punishment {

    public KillPunishment() {
        super("KILL");
    }

    @Override
    public void onQuit(String label) {
        final Player player = getPlayer();

        if (player.hasPermission("combatlogger.admin"))
            return;

        Location loc = player.getLocation();
        World world = loc.getWorld();

        if (world == null)
            return;

        // Drop all inventory items at player's location
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                world.dropItemNaturally(loc, item);
            }
        }

        // Drop armor
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && !item.getType().isAir()) {
                world.dropItemNaturally(loc, item);
            }
        }

        // Drop offhand
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && !offhand.getType().isAir()) {
            world.dropItemNaturally(loc, offhand);
        }

        // Clear their inventory so items don't duplicate
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        // Kill the player (triggers death for stats, etc.)
        player.setHealth(0);
    }
}
