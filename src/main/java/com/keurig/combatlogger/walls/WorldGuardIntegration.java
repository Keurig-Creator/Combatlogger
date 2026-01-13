package com.keurig.combatlogger.walls;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * WorldGuard integration for region boundary detection.
 * This class is only loaded if WorldGuard is present on the server.
 */
public class WorldGuardIntegration {

    private final Logger logger;
    private final String regionId;
    private boolean enabled;

    public WorldGuardIntegration(Logger logger, String regionId) {
        this.logger = logger;
        this.regionId = regionId;
        this.enabled = false;

        try {
            // Test if WorldGuard classes are available
            Class.forName("com.sk89q.worldguard.WorldGuard");
            Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion");
            this.enabled = true;
            logger.info("WorldGuard integration enabled for region: " + regionId);
        } catch (ClassNotFoundException e) {
            logger.info("WorldGuard not found - region walls disabled");
        }
    }

    /**
     * Check if WorldGuard integration is available
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the region boundaries for the configured region
     *
     * @param world The world to check
     * @return RegionBounds or null if region not found
     */
    public RegionWallRenderer.RegionBounds getRegionBounds(World world) {
        if (!enabled) {
            return null;
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));

            if (regions == null) {
                return null;
            }

            ProtectedRegion region = regions.getRegion(regionId);
            if (region == null) {
                return null;
            }

            // Check if this is a polygon region
            if (region instanceof ProtectedPolygonalRegion) {
                logger.info("Region '" + regionId + "' is a POLYGON region");
                return getPolygonBounds((ProtectedPolygonalRegion) region);
            }
            // Check if this is a cuboid region
            else if (region instanceof ProtectedCuboidRegion) {
                logger.info("Region '" + regionId + "' is a CUBOID region");
                return getCuboidBounds(region);
            }
            // Handle other region types as cuboid (using bounding box)
            else {
                logger.info("Region '" + regionId + "' is a " + region.getClass().getSimpleName() + " region (using bounding box)");
                return getCuboidBounds(region);
            }

        } catch (Exception e) {
            logger.warning("Error getting WorldGuard region bounds: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get bounds for a polygon region
     */
    private RegionWallRenderer.RegionBounds getPolygonBounds(ProtectedPolygonalRegion polygonRegion) {
        List<BlockVector2> points = polygonRegion.getPoints();

        if (points == null || points.isEmpty()) {
            logger.warning("Polygon region '" + regionId + "' has no points!");
            return null;
        }

        // Convert WorldGuard points to our PolygonPoint format
        List<RegionWallRenderer.PolygonPoint> polygonPoints = new ArrayList<>();

        logger.info("Polygon region '" + regionId + "' has " + points.size() + " points:");

        // Track last point to remove duplicates
        RegionWallRenderer.PolygonPoint lastPoint = null;

        for (BlockVector2 point : points) {
            RegionWallRenderer.PolygonPoint newPoint =
                    new RegionWallRenderer.PolygonPoint(point.getBlockX(), point.getBlockZ());

            // Skip duplicate consecutive points
            if (lastPoint != null && lastPoint.x == newPoint.x && lastPoint.z == newPoint.z) {
                logger.info("  Skipping duplicate point: " + newPoint.x + ", " + newPoint.z);
                continue;
            }

            polygonPoints.add(newPoint);
            logger.info("  Point: " + newPoint.x + ", " + newPoint.z);
            lastPoint = newPoint;
        }

        // Also check if first and last points are duplicates
        if (polygonPoints.size() > 1) {
            RegionWallRenderer.PolygonPoint first = polygonPoints.get(0);
            RegionWallRenderer.PolygonPoint last = polygonPoints.get(polygonPoints.size() - 1);
            if (first.x == last.x && first.z == last.z) {
                logger.info("  Removing duplicate closing point");
                polygonPoints.remove(polygonPoints.size() - 1);
            }
        }

        logger.info("Polygon has " + polygonPoints.size() + " unique points after removing duplicates");

        return new RegionWallRenderer.RegionBounds(polygonPoints);
    }

    /**
     * Get bounds for a cuboid region
     */
    private RegionWallRenderer.RegionBounds getCuboidBounds(ProtectedRegion region) {
        // Get the bounding box
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        // Check if this is an infinite region (has Integer.MIN_VALUE or Integer.MAX_VALUE)
        boolean isInfinite = isInfiniteRegion(min, max);

        if (isInfinite) {
            logger.info("Region '" + regionId + "' is INFINITE (extends to world borders)");
        }

        return new RegionWallRenderer.RegionBounds(
                min.getBlockX(),
                max.getBlockX(),
                min.getBlockZ(),
                max.getBlockZ(),
                isInfinite
        );
    }

    /**
     * Check if a region is infinite (extends to world borders)
     * Infinite regions have coordinates at Integer.MIN_VALUE or Integer.MAX_VALUE
     */
    private boolean isInfiniteRegion(BlockVector3 min, BlockVector3 max) {
        // Check if any coordinate is at the extreme values
        return min.getBlockX() <= Integer.MIN_VALUE + 1000 ||
                max.getBlockX() >= Integer.MAX_VALUE - 1000 ||
                min.getBlockZ() <= Integer.MIN_VALUE + 1000 ||
                max.getBlockZ() >= Integer.MAX_VALUE - 1000;
    }

    /**
     * Check if a location is within the configured region
     *
     * @param location The location to check
     * @return true if within region, false otherwise
     */
    public boolean isInRegion(Location location) {
        if (!enabled) {
            return false;
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));

            if (regions == null) {
                return false;
            }

            ProtectedRegion region = regions.getRegion(regionId);
            if (region == null) {
                return false;
            }

            BlockVector3 position = BlockVector3.at(
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ()
            );

            return region.contains(position);

        } catch (Exception e) {
            logger.warning("Error checking WorldGuard region: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the configured region ID
     */
    public String getRegionId() {
        return regionId;
    }
}