package com.keurig.combatlogger.walls;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renders client-side walls around WorldGuard region borders for players in combat.
 * Walls are only visible to the player and do not affect actual blocks.
 */
public class RegionWallRenderer {

    private final Material wallMaterial;
    private final int wallHeight;
    private final int wallSegmentLength;
    private final int borderProximityThreshold;
    private final int groundAnchorRange;

    // Track fake blocks shown to each player
    private final Map<UUID, Set<Location>> playerFakeBlocks;

    // Cache to avoid recalculating frequently
    private final Map<UUID, Long> lastUpdateTime;
    private final long updateThrottleMs;

    /**
     * Create a new RegionWallRenderer
     *
     * @param wallMaterial             Material to use for the wall (e.g., RED_STAINED_GLASS, BARRIER)
     * @param wallHeight               Height of the wall in blocks
     * @param wallSegmentLength        Length of wall segment around player (±N blocks along edge)
     * @param borderProximityThreshold Distance from border to trigger wall display
     * @param updateThrottleMs         Minimum milliseconds between updates per player
     * @param groundAnchorRange        How far below player (in blocks) to extend wall downward
     */
    public RegionWallRenderer(Material wallMaterial, int wallHeight, int wallSegmentLength,
                              int borderProximityThreshold, long updateThrottleMs, int groundAnchorRange) {
        this.wallMaterial = wallMaterial;
        this.wallHeight = wallHeight;
        this.wallSegmentLength = wallSegmentLength;
        this.borderProximityThreshold = borderProximityThreshold;
        this.updateThrottleMs = updateThrottleMs;
        this.groundAnchorRange = groundAnchorRange;
        this.playerFakeBlocks = new ConcurrentHashMap<>();
        this.lastUpdateTime = new ConcurrentHashMap<>();
    }

    /**
     * Update wall visibility for a player at their current location
     *
     * @param player             The player to update
     * @param regionBounds       The bounds of the region (can be cuboid or polygon)
     * @param playerInsideRegion Whether the player is currently inside the region
     */
    public void updateWalls(Player player, RegionBounds regionBounds, boolean playerInsideRegion) {
        UUID playerId = player.getUniqueId();

        // Throttle updates
        Long lastUpdate = lastUpdateTime.get(playerId);
        long currentTime = System.currentTimeMillis();
        if (lastUpdate != null && (currentTime - lastUpdate) < updateThrottleMs) {
            return;
        }
        lastUpdateTime.put(playerId, currentTime);

        Location playerLoc = player.getLocation();

        // Clear old fake blocks
        clearWalls(player);

        Set<Location> newFakeBlocks = new HashSet<>();

        if (regionBounds.isPolygon()) {
            // Handle polygon regions
            renderPolygonWalls(player, playerLoc, regionBounds, newFakeBlocks);
        } else {
            // Handle cuboid regions (original logic)

            EdgeProximity proximity = calculateEdgeProximity(playerLoc, regionBounds);

            if (proximity.isNearAnyEdge()) {

                if (proximity.isNearMinX()) {
                    renderWallSegment(player, playerLoc, regionBounds.minX,
                            Axis.X, Direction.MIN, newFakeBlocks, regionBounds, playerInsideRegion);
                }
                if (proximity.isNearMaxX()) {
                    renderWallSegment(player, playerLoc, regionBounds.maxX,
                            Axis.X, Direction.MAX, newFakeBlocks, regionBounds, playerInsideRegion);
                }
                if (proximity.isNearMinZ()) {
                    renderWallSegment(player, playerLoc, regionBounds.minZ,
                            Axis.Z, Direction.MIN, newFakeBlocks, regionBounds, playerInsideRegion);
                }
                if (proximity.isNearMaxZ()) {
                    renderWallSegment(player, playerLoc, regionBounds.maxZ,
                            Axis.Z, Direction.MAX, newFakeBlocks, regionBounds, playerInsideRegion);
                }
            }
        }

        if (!newFakeBlocks.isEmpty()) {
            playerFakeBlocks.put(playerId, newFakeBlocks);
        }
    }

    /**
     * Render walls for polygon regions
     */
    private void renderPolygonWalls(Player player, Location playerLoc, RegionBounds bounds, Set<Location> fakeBlocks) {
        List<PolygonPoint> points = bounds.polygonPoints;
        if (points == null || points.size() < 2) {
            return;
        }

        int playerX = playerLoc.getBlockX();
        int playerZ = playerLoc.getBlockZ();

        // Check each edge of the polygon
        for (int i = 0; i < points.size(); i++) {
            PolygonPoint p1 = points.get(i);
            PolygonPoint p2 = points.get((i + 1) % points.size()); // Wrap around to first point

            // Skip duplicate points
            if (p1.x == p2.x && p1.z == p2.z) {
                continue;
            }

            // Calculate distance from player to this edge
            double distance = distanceToLineSegment(playerX, playerZ, p1.x, p1.z, p2.x, p2.z);

            if (distance <= borderProximityThreshold) {
                renderPolygonEdge(player, playerLoc, p1, p2, fakeBlocks);
            }
        }
    }

    /**
     * Calculate distance from point to line segment
     */
    private double distanceToLineSegment(int px, int pz, int x1, int z1, int x2, int z2) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        double lengthSquared = dx * dx + dz * dz;

        if (lengthSquared == 0) {
            // Point case
            double ddx = px - x1;
            double ddz = pz - z1;
            return Math.sqrt(ddx * ddx + ddz * ddz);
        }

        // Calculate projection of point onto line
        double t = Math.max(0, Math.min(1, ((px - x1) * dx + (pz - z1) * dz) / lengthSquared));

        double projX = x1 + t * dx;
        double projZ = z1 + t * dz;

        double distX = px - projX;
        double distZ = pz - projZ;

        return Math.sqrt(distX * distX + distZ * distZ);
    }

    /**
     * Render a wall along a polygon edge
     */
    private void renderPolygonEdge(Player player, Location playerLoc, PolygonPoint p1, PolygonPoint p2, Set<Location> fakeBlocks) {
        int playerY = playerLoc.getBlockY();

        // Wall follows player vertically
        int startY = playerY - groundAnchorRange;
        int endY = playerY + wallHeight;

        // Clamp to world height
        int minWorldY = playerLoc.getWorld().getMinHeight();
        int maxWorldY = playerLoc.getWorld().getMaxHeight() - 1;
        startY = Math.max(startY, minWorldY);
        endY = Math.min(endY, maxWorldY);

        // Get all blocks along this edge
        List<BlockPos> edgeBlocks = getBlocksAlongLine(p1.x, p1.z, p2.x, p2.z);

        int playerX = playerLoc.getBlockX();
        int playerZ = playerLoc.getBlockZ();

        // Find the closest block on this edge to the player
        int closestIndex = -1;
        double closestDist = Double.MAX_VALUE;

        for (int i = 0; i < edgeBlocks.size(); i++) {
            BlockPos pos = edgeBlocks.get(i);
            int distX = pos.x - playerX;
            int distZ = pos.z - playerZ;
            double dist = Math.sqrt(distX * distX + distZ * distZ);

            if (dist < closestDist) {
                closestDist = dist;
                closestIndex = i;
            }
        }

        if (closestIndex == -1) {
            return;
        }

        // Render a segment of the edge centered on the closest point
        // This ensures continuous walls near the player
        int renderStart = Math.max(0, closestIndex - wallSegmentLength);
        int renderEnd = Math.min(edgeBlocks.size() - 1, closestIndex + wallSegmentLength);

        int blocksRendered = 0;

        for (int i = renderStart; i <= renderEnd; i++) {
            BlockPos pos = edgeBlocks.get(i);

            // Render vertical wall at this position
            for (int y = startY; y <= endY; y++) {
                Location loc = new Location(player.getWorld(), pos.x, y, pos.z);
                Block realBlock = loc.getBlock();

                if (realBlock.getType() == Material.AIR || realBlock.getType().isAir()) {
                    player.sendBlockChange(loc, wallMaterial.createBlockData());
                    fakeBlocks.add(loc);
                    blocksRendered++;
                }
            }
        }

    }

    /**
     * Get all block positions along a line using DDA algorithm (more accurate than Bresenham for walls)
     */
    private List<BlockPos> getBlocksAlongLine(int x1, int z1, int x2, int z2) {
        List<BlockPos> blocks = new ArrayList<>();

        int dx = Math.abs(x2 - x1);
        int dz = Math.abs(z2 - z1);

        // Calculate the number of steps needed
        int steps = Math.max(dx, dz);

        if (steps == 0) {
            blocks.add(new BlockPos(x1, z1));
            return blocks;
        }

        // Calculate increment for each step
        double xIncrement = (x2 - x1) / (double) steps;
        double zIncrement = (z2 - z1) / (double) steps;

        // Track current position
        double x = x1;
        double z = z1;

        // Use a set to avoid duplicates
        Set<String> addedBlocks = new HashSet<>();

        for (int i = 0; i <= steps; i++) {
            int blockX = (int) Math.round(x);
            int blockZ = (int) Math.round(z);
            String key = blockX + "," + blockZ;

            if (!addedBlocks.contains(key)) {
                blocks.add(new BlockPos(blockX, blockZ));
                addedBlocks.add(key);
            }

            x += xIncrement;
            z += zIncrement;
        }

        return blocks;
    }

    /**
     * Clear all fake walls for a player (call when combat ends)
     */
    public void clearWalls(Player player) {
        UUID playerId = player.getUniqueId();
        Set<Location> fakeBlocks = playerFakeBlocks.remove(playerId);

        if (fakeBlocks != null) {
            for (Location loc : fakeBlocks) {
                Block realBlock = loc.getBlock();
                player.sendBlockChange(loc, realBlock.getBlockData());
            }
        }

        lastUpdateTime.remove(playerId);
    }

    /**
     * Clear all fake walls for all tracked players
     */
    public void clearAllWalls() {
        for (UUID playerId : new HashSet<>(playerFakeBlocks.keySet())) {
            Player player = org.bukkit.Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                clearWalls(player);
            } else {
                playerFakeBlocks.remove(playerId);
                lastUpdateTime.remove(playerId);
            }
        }
    }

    private EdgeProximity calculateEdgeProximity(Location loc, RegionBounds bounds) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        boolean nearMinX = false;
        boolean nearMaxX = false;
        boolean nearMinZ = false;
        boolean nearMaxZ = false;

        if (x < bounds.minX) {
            int distMinX = bounds.minX - x;
            if (distMinX <= borderProximityThreshold) {
                nearMinX = true;
            }
        } else if (x > bounds.maxX) {
            int distMaxX = x - bounds.maxX;
            if (distMaxX <= borderProximityThreshold) {
                nearMaxX = true;
            }
        }

        if (z < bounds.minZ) {
            int distMinZ = bounds.minZ - z;
            if (distMinZ <= borderProximityThreshold) {
                nearMinZ = true;
            }
        } else if (z > bounds.maxZ) {
            int distMaxZ = z - bounds.maxZ;
            if (distMaxZ <= borderProximityThreshold) {
                nearMaxZ = true;
            }
        }

        return new EdgeProximity(nearMinX, nearMaxX, nearMinZ, nearMaxZ);
    }

    private void renderWallSegment(Player player, Location playerLoc, int edgeCoord,
                                   Axis axis, Direction direction, Set<Location> fakeBlocks,
                                   RegionBounds bounds, boolean playerInsideRegion) {
        int playerY = playerLoc.getBlockY();

        int startY = playerY - groundAnchorRange;
        int endY = playerY + wallHeight;

        int minWorldY = playerLoc.getWorld().getMinHeight();
        int maxWorldY = playerLoc.getWorld().getMaxHeight() - 1;
        startY = Math.max(startY, minWorldY);
        endY = Math.min(endY, maxWorldY);

        int parallelCoord = axis == Axis.X ? playerLoc.getBlockZ() : playerLoc.getBlockX();
        int desiredStart = parallelCoord - wallSegmentLength;
        int desiredEnd = parallelCoord + wallSegmentLength;

        int regionParallelMin = axis == Axis.X ? bounds.minZ : bounds.minX;
        int regionParallelMax = axis == Axis.X ? bounds.maxZ : bounds.maxX;

        int start = Math.max(desiredStart, regionParallelMin);
        int end = Math.min(desiredEnd, regionParallelMax);

        int blocksRendered = 0;
        for (int i = start; i <= end; i++) {
            for (int y = startY; y <= endY; y++) {
                Location loc;
                if (axis == Axis.X) {
                    loc = new Location(player.getWorld(), edgeCoord, y, i);
                } else {
                    loc = new Location(player.getWorld(), i, y, edgeCoord);
                }

                Block realBlock = loc.getBlock();
                if (realBlock.getType() == Material.AIR || realBlock.getType().isAir()) {
                    player.sendBlockChange(loc, wallMaterial.createBlockData());
                    fakeBlocks.add(loc);
                    blocksRendered++;
                }
            }
        }
    }

    // Helper classes

    private static class BlockPos {
        final int x;
        final int z;

        BlockPos(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

    private static class EdgeProximity {
        final boolean nearMinX, nearMaxX, nearMinZ, nearMaxZ;

        EdgeProximity(boolean nearMinX, boolean nearMaxX, boolean nearMinZ, boolean nearMaxZ) {
            this.nearMinX = nearMinX;
            this.nearMaxX = nearMaxX;
            this.nearMinZ = nearMinZ;
            this.nearMaxZ = nearMaxZ;
        }

        boolean isNearAnyEdge() {
            return nearMinX || nearMaxX || nearMinZ || nearMaxZ;
        }

        boolean isNearMinX() {
            return nearMinX;
        }

        boolean isNearMaxX() {
            return nearMaxX;
        }

        boolean isNearMinZ() {
            return nearMinZ;
        }

        boolean isNearMaxZ() {
            return nearMaxZ;
        }
    }

    private enum Axis {X, Z}

    private enum Direction {MIN, MAX}

    /**
     * Represents a point in a polygon
     */
    public static class PolygonPoint {
        public final int x;
        public final int z;

        public PolygonPoint(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

    /**
     * Represents the bounds of a region (cuboid or polygon)
     */
    public static class RegionBounds {
        public final int minX, maxX, minZ, maxZ;
        public final boolean isInfinite;
        public final List<PolygonPoint> polygonPoints; // null for cuboid regions

        // Cuboid constructor
        public RegionBounds(int minX, int maxX, int minZ, int maxZ) {
            this(minX, maxX, minZ, maxZ, false, null);
        }

        public RegionBounds(int minX, int maxX, int minZ, int maxZ, boolean isInfinite) {
            this(minX, maxX, minZ, maxZ, isInfinite, null);
        }

        // Polygon constructor
        public RegionBounds(List<PolygonPoint> polygonPoints) {
            this.polygonPoints = new ArrayList<>(polygonPoints);
            this.isInfinite = false;

            // Calculate bounding box from polygon points
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxZ = Integer.MIN_VALUE;

            for (PolygonPoint p : polygonPoints) {
                minX = Math.min(minX, p.x);
                maxX = Math.max(maxX, p.x);
                minZ = Math.min(minZ, p.z);
                maxZ = Math.max(maxZ, p.z);
            }

            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }

        // Full constructor
        private RegionBounds(int minX, int maxX, int minZ, int maxZ, boolean isInfinite, List<PolygonPoint> polygonPoints) {
            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;
            this.isInfinite = isInfinite;
            this.polygonPoints = polygonPoints;
        }

        public boolean isPolygon() {
            return polygonPoints != null && !polygonPoints.isEmpty();
        }

        public boolean contains(Location loc) {
            int x = loc.getBlockX();
            int z = loc.getBlockZ();

            if (isInfinite) {
                return true;
            }

            if (isPolygon()) {
                return containsPointInPolygon(x, z);
            }

            return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        }

        private boolean containsPointInPolygon(int x, int z) {
            boolean inside = false;
            int j = polygonPoints.size() - 1;

            for (int i = 0; i < polygonPoints.size(); i++) {
                PolygonPoint pi = polygonPoints.get(i);
                PolygonPoint pj = polygonPoints.get(j);

                if ((pi.z > z) != (pj.z > z) &&
                        x < (pj.x - pi.x) * (z - pi.z) / (double) (pj.z - pi.z) + pi.x) {
                    inside = !inside;
                }
                j = i;
            }

            return inside;
        }

        public boolean isInfinite() {
            return isInfinite;
        }
    }
}