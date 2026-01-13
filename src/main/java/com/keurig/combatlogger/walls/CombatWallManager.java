package com.keurig.combatlogger.walls;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages combat wall rendering for players in combat across multiple regions.
 * Integrates with combat system to show/hide walls based on combat state.
 */
public class CombatWallManager implements Listener {

    private final Plugin plugin;
    private final Map<String, RegionWallRenderer> wallRenderers;
    private final Map<String, WorldGuardIntegration> worldGuardIntegrations;

    // Track players currently in combat
    private final Set<UUID> playersInCombat;

    // Cache region bounds to avoid repeated lookups
    private final Map<String, Map<String, RegionWallRenderer.RegionBounds>> regionBoundsCache;

    // Task for periodic cleanup
    private BukkitTask cleanupTask;

    /**
     * Create a new CombatWallManager with multiple regions
     *
     * @param plugin The plugin instance
     * @param config Configuration with all region settings
     */
    public CombatWallManager(Plugin plugin, CombatWallConfig config) {
        this.plugin = plugin;
        this.playersInCombat = ConcurrentHashMap.newKeySet();
        this.regionBoundsCache = new ConcurrentHashMap<>();
        this.wallRenderers = new HashMap<>();
        this.worldGuardIntegrations = new HashMap<>();

        // Initialize renderers for each region
        for (Map.Entry<String, CombatWallConfig.RegionWallSettings> entry : config.getAllRegionSettings().entrySet()) {
            String regionId = entry.getKey();
            CombatWallConfig.RegionWallSettings settings = entry.getValue();

            if (!settings.enabled) {
                continue;
            }

            // Create renderer for this region
            RegionWallRenderer renderer = new RegionWallRenderer(
                    settings.wallMaterial,
                    settings.wallHeight,
                    settings.wallSegmentLength,
                    settings.borderProximity,
                    settings.updateThrottleMs,
                    settings.groundAnchorRange  // NEW: pass ground anchor range
            );
            wallRenderers.put(regionId, renderer);

            // Create WorldGuard integration for this region
            WorldGuardIntegration wgIntegration = new WorldGuardIntegration(
                    plugin.getLogger(),
                    regionId
            );
            worldGuardIntegrations.put(regionId, wgIntegration);
        }

        // Register event listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Start cleanup task
        startCleanupTask();
    }

    /**
     * Call this when a player enters combat
     */
    public void onCombatStart(Player player) {
        UUID playerId = player.getUniqueId();
        playersInCombat.add(playerId);

        // Immediately update walls for this player across all regions
        updatePlayerWalls(player);
    }

    /**
     * Call this when a player exits combat
     */
    public void onCombatEnd(Player player) {
        UUID playerId = player.getUniqueId();
        playersInCombat.remove(playerId);

        // Clear walls for this player in all regions
        clearPlayerWalls(player);
    }

    /**
     * Check if a player is in combat (according to this manager)
     */
    public boolean isInCombat(UUID playerId) {
        return playersInCombat.contains(playerId);
    }

    /**
     * Get the number of players currently in combat with walls
     */
    public int getCombatPlayerCount() {
        return playersInCombat.size();
    }

    /**
     * Get the number of active regions
     */
    public int getActiveRegionCount() {
        return wallRenderers.size();
    }

    /**
     * Update walls for a player across all applicable regions
     */
    public void updatePlayerWalls(Player player) {
        if (!playersInCombat.contains(player.getUniqueId())) {
            return;
        }

        Location loc = player.getLocation();
        String worldName = loc.getWorld().getName();

        // Check each region to see if player is OUTSIDE and near it
        for (Map.Entry<String, WorldGuardIntegration> entry : worldGuardIntegrations.entrySet()) {
            String regionId = entry.getKey();
            WorldGuardIntegration wgIntegration = entry.getValue();

            if (!wgIntegration.isEnabled()) {
                continue;
            }

            // Get or cache region bounds
            Map<String, RegionWallRenderer.RegionBounds> worldCache =
                    regionBoundsCache.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>());

            RegionWallRenderer.RegionBounds bounds = worldCache.get(regionId);
            if (bounds == null) {
                bounds = wgIntegration.getRegionBounds(loc.getWorld());
                if (bounds != null) {
                    worldCache.put(regionId, bounds);

                    plugin.getLogger().info("[DEBUG] Region '" + regionId + "' bounds: " +
                            "minX=" + bounds.minX + ", maxX=" + bounds.maxX +
                            ", minZ=" + bounds.minZ + ", maxZ=" + bounds.maxZ +
                            ", infinite=" + bounds.isInfinite());

                    // Log if this is an infinite region
                    if (bounds.isInfinite()) {
                        plugin.getLogger().info("Region '" + regionId +
                                "' is infinite - walls will only show at defined boundaries");
                    }
                } else {
                    plugin.getLogger().warning("[DEBUG] Could not get bounds for region: " + regionId);
                }
            }

            RegionWallRenderer renderer = wallRenderers.get(regionId);
            if (bounds != null && renderer != null) {
                // Check if player is OUTSIDE this region
                boolean inRegion = wgIntegration.isInRegion(loc);

                if (!inRegion) {
                    // Player is OUTSIDE region - show walls to block entry
                    renderer.updateWalls(player, bounds, false);  // false = player outside
                } else {
                    // Player is inside region, no walls needed
                    renderer.clearWalls(player);
                }
            } else {
                if (bounds == null) plugin.getLogger().warning("[DEBUG] Bounds is null for " + regionId);
                if (renderer == null) plugin.getLogger().warning("[DEBUG] Renderer is null for " + regionId);
            }
        }
    }

    /**
     * Clear walls for a player in all regions
     */
    private void clearPlayerWalls(Player player) {
        for (RegionWallRenderer renderer : wallRenderers.values()) {
            renderer.clearWalls(player);
        }
    }

    /**
     * Reload configuration and reinitialize all regions
     * Call this when config changes
     *
     * @param config Your BoostedYAML YamlDocument
     */
    public void reloadConfig(YamlDocument config) {
        try {
            // Reload the config file
            config.reload();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reload config file: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Clear all existing walls first
        for (UUID playerId : new HashSet<>(playersInCombat)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                clearPlayerWalls(player);
            }
        }

        // Clear old renderers and integrations
        wallRenderers.clear();
        worldGuardIntegrations.clear();
        regionBoundsCache.clear();

        // Load new config
        CombatWallConfig newConfig = new CombatWallConfig(config, plugin);

        if (!newConfig.validate(plugin)) {
            plugin.getLogger().warning("Config validation failed! Keeping old configuration.");
            return;
        }

        // Reinitialize with new config
        for (Map.Entry<String, CombatWallConfig.RegionWallSettings> entry : newConfig.getAllRegionSettings().entrySet()) {
            String regionId = entry.getKey();
            CombatWallConfig.RegionWallSettings settings = entry.getValue();

            if (!settings.enabled) {
                continue;
            }

            // Create renderer for this region
            RegionWallRenderer renderer = new RegionWallRenderer(
                    settings.wallMaterial,
                    settings.wallHeight,
                    settings.wallSegmentLength,
                    settings.borderProximity,
                    settings.updateThrottleMs,
                    settings.groundAnchorRange
            );
            wallRenderers.put(regionId, renderer);

            // Create WorldGuard integration for this region
            WorldGuardIntegration wgIntegration = new WorldGuardIntegration(
                    plugin.getLogger(),
                    regionId
            );
            worldGuardIntegrations.put(regionId, wgIntegration);
        }

        // Reapply walls for players currently in combat
        for (UUID playerId : playersInCombat) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                updatePlayerWalls(player);
            }
        }
    }

    /**
     * Shutdown the manager and clear all walls
     */
    public void shutdown() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }

        // Clear all walls
        for (UUID playerId : new HashSet<>(playersInCombat)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                clearPlayerWalls(player);
            }
        }

        playersInCombat.clear();
        regionBoundsCache.clear();
    }

    /**
     * Clear region bounds cache (call if regions are modified)
     */
    public void clearRegionCache() {
        regionBoundsCache.clear();
    }

    /**
     * Clear cache for a specific region
     */
    public void clearRegionCache(String regionId) {
        for (Map<String, RegionWallRenderer.RegionBounds> worldCache : regionBoundsCache.values()) {
            worldCache.remove(regionId);
        }
    }

    /**
     * Check if any WorldGuard integration is available
     */
    public boolean isWorldGuardEnabled() {
        for (WorldGuardIntegration wg : worldGuardIntegrations.values()) {
            if (wg.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get list of active region IDs
     */
    public Set<String> getActiveRegions() {
        return new HashSet<>(wallRenderers.keySet());
    }

    // Event Handlers

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Only process if player is in combat
        if (!playersInCombat.contains(player.getUniqueId())) {
            return;
        }

        // Only update if player actually moved to a different block
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) {
            return;
        }

        if (from.getBlockX() == to.getBlockX() &&
                from.getBlockY() == to.getBlockY() &&
                from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        // Update walls for this player across all regions
        updatePlayerWalls(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Clean up data for this player
        playersInCombat.remove(playerId);
        clearPlayerWalls(player);
    }

    // Periodic cleanup task

    private void startCleanupTask() {
        // Run every 30 seconds to clean up stale data
        cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Set<UUID> toRemove = new HashSet<>();

            for (UUID playerId : playersInCombat) {
                Player player = Bukkit.getPlayer(playerId);
                if (player == null || !player.isOnline()) {
                    toRemove.add(playerId);
                }
            }

            for (UUID playerId : toRemove) {
                playersInCombat.remove(playerId);
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    clearPlayerWalls(player);
                }
            }

        }, 600L, 600L); // 30 seconds
    }
}