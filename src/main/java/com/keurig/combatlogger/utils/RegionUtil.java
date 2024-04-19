package com.keurig.combatlogger.utils;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.LinkedList;

public class RegionUtil {

    public static void handleClaimGlassBuffer(ProtectedRegion region, Player player, Location location) {
        int x = closestValue(location.getBlockX(), region.getMinimumPoint().getBlockX(), region.getMaximumPoint().getBlockX());
        int z = closestValue(location.getBlockZ(), region.getMinimumPoint().getBlockZ(), region.getMaximumPoint().getBlockZ());

        boolean xClose = (Math.abs(location.getX() - x) < 10);
        boolean zClose = (Math.abs(location.getZ() - z) < 10);

        if (!xClose && !zClose)
            return;

        LinkedList<Location> glassBufferLocations = new LinkedList<>();

        if (xClose) {
            for (int i = -5; i < 5; i++) {
                for (int j = -5; j < 5; j++) {
                    Location loc;
                    if (isCloseToClaimEdge(region.getMaximumPoint().getBlockZ(), region.getMinimumPoint().getBlockZ(), location.getBlockZ() + j) &&
                            !glassBufferLocations.contains(loc = new Location(location.getWorld(), x, (location.getBlockY() + i), (location.getBlockZ() + j))) &&
                            (loc.getBlock().getType() == Material.AIR || !loc.getBlock().getType().isSolid())) {
                        glassBufferLocations.add(loc);
                    }
                }
            }
        }

        if (zClose) {
            for (int i = -5; i < 5; i++) {
                for (int j = -5; j < 5; j++) {
                    Location loc;
                    if (isCloseToClaimEdge(region.getMaximumPoint().getBlockX(), region.getMinimumPoint().getBlockX(), location.getBlockX() + j) &&
                            !glassBufferLocations.contains(loc = new Location(location.getWorld(), (location.getBlockX() + j), (location.getBlockY() + i), z)) &&
                            (loc.getBlock().getType() == Material.AIR || !loc.getBlock().getType().isSolid())) {
                        glassBufferLocations.add(loc);
                    }
                }
            }
        }

        if (!glassBufferLocations.isEmpty()) {
            for (Location glassBufferLocation : glassBufferLocations) {
                player.sendBlockChange(glassBufferLocation, Material.GLASS.createBlockData());
            }
        }
    }

    private static boolean isCloseToClaimEdge(int location, int corner1, int corner2) {
        return (Math.abs(location - corner1) == Math.abs(corner2 - location) + Math.abs(corner2 - corner1));
    }

    private static int closestValue(int location, int... values) {
        int diff = Math.abs(values[0] - location);
        int index = 0;
        for (int i = 1; i < values.length; i++) {
            int d = Math.abs(values[i] - location);
            if (d < diff) {
                index = i;
                diff = d;
            }
        }
        return values[index];
    }

}
