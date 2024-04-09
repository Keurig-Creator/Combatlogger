package com.keurig.combatlogger.listeners;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.api.CombatLoggerAPI;
import com.keurig.combatlogger.utils.Chat;
import com.keurig.combatlogger.utils.RegionUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MoveListener implements Listener {

    private final CombatLogger plugin = CombatLogger.getInstance();

    private final HashMap<Player, Block> modifiedBlocks = new HashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();


        if (CombatLoggerAPI.isTagged(player)) {
            Location eventTo = event.getTo();
            Location eventFrom = event.getFrom();

            // Allow player to move cursor
            eventFrom.setYaw(Objects.requireNonNull(eventTo).getYaw());
            eventFrom.setPitch(Objects.requireNonNull(eventTo).getPitch());

            // Allows gravity to happen
            // edits the location the player would be trying to go to i.e y axis only
            // essentially filtering out the keyboard movement
            if (eventTo.getY() < eventFrom.getY()) {
                eventFrom.setY(eventTo.getY());
            }

            List<String> regionList = plugin.getConfig().getStringList("protected-regions.regions");
            for (String regions : regionList) {
                ProtectedRegion region = getRegion(player.getWorld(), regions);
                if (region == null) return;

                if (region.contains(BukkitAdapter.asBlockVector(eventTo)) && !region.contains(BukkitAdapter.asBlockVector(eventFrom))) {
                    event.setTo(eventFrom);
                    Chat.message(player, 5, plugin.getConfig().getString("protected-regions.message"));
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        RegionUtil.handleClaimGlassBuffer(WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld())).getRegion("spawn"), player, player.getLocation());
//        event.setCancelled(true);
    }

    public ProtectedRegion getRegion(World world, String regionName) {
        // Get WorldGuard plugin instance
        com.sk89q.worldguard.WorldGuard worldGuard = WorldGuard.getInstance();

        // Get the WorldGuard API instance
        com.sk89q.worldguard.protection.managers.RegionManager regionManager = worldGuard.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));

        // Check if regionManager is null
        if (regionManager == null) {
            // WorldGuard is not enabled for this world
            return null;
        }

        // Get the region by name
        return regionManager.getRegion(regionName);
    }

}