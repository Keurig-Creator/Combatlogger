package com.keurig.combatlogger.walls;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Configuration for combat region walls.
 * Supports multiple regions with individual settings.
 * Uses BoostedYAML for configuration.
 */
public class CombatWallConfig {

    private final boolean globalEnabled;
    private final Map<String, RegionWallSettings> regionSettings;

    /**
     * Load configuration from BoostedYAML config
     *
     * @param config Your BoostedYAML YamlDocument instance
     * @param plugin Plugin instance for logging
     */
    public CombatWallConfig(YamlDocument config, Plugin plugin) {
        // Load global enable
        this.globalEnabled = config.getBoolean("combat-walls.enabled", true);

        // Load region settings
        this.regionSettings = new HashMap<>();
        Section regionsSection = config.getSection("combat-walls.regions");

        if (regionsSection != null) {
            Set<Object> regionKeys = regionsSection.getKeys();

            for (Object keyObj : regionKeys) {
                String regionId = keyObj.toString();
                Section regionSection = regionsSection.getSection(regionId);

                if (regionSection != null) {
                    RegionWallSettings settings = loadRegionSettings(plugin, regionId, regionSection);
                    if (settings != null) {
                        regionSettings.put(regionId, settings);
                    }
                }
            }
        }
    }

    private RegionWallSettings loadRegionSettings(Plugin plugin, String regionId, Section section) {
        try {
            boolean enabled = section.getBoolean("enabled", true);
            String materialName = section.getString("wall-material", "RED_STAINED_GLASS");
            int wallHeight = section.getInt("wall-height", 10);
            int wallSegmentLength = section.getInt("wall-segment-length", 8);
            int borderProximity = section.getInt("border-proximity", 5);
            long updateThrottleMs = section.getLong("update-throttle-ms", 100L);
            int groundAnchorRange = section.getInt("ground-anchor-range", 5);  // NEW: default 5 blocks

            // Parse material
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                plugin.getLogger().warning("Invalid wall material for region " + regionId + ": " + materialName);
                material = Material.RED_STAINED_GLASS;
            }

            return new RegionWallSettings(
                    regionId,
                    enabled,
                    material,
                    wallHeight,
                    wallSegmentLength,
                    borderProximity,
                    updateThrottleMs,
                    groundAnchorRange
            );
        } catch (Exception e) {
            plugin.getLogger().warning("Error loading settings for region " + regionId + ": " + e.getMessage());
            return null;
        }
    }

    public boolean isGlobalEnabled() {
        return globalEnabled;
    }

    public Set<String> getRegionIds() {
        return regionSettings.keySet();
    }

    public RegionWallSettings getRegionSettings(String regionId) {
        return regionSettings.get(regionId);
    }

    public Map<String, RegionWallSettings> getAllRegionSettings() {
        return new HashMap<>(regionSettings);
    }

    /**
     * Validate all region configurations
     *
     * @return true if all valid, false if any issues
     */
    public boolean validate(Plugin plugin) {
        boolean valid = true;

        if (regionSettings.isEmpty()) {
            return false;
        }

        for (Map.Entry<String, RegionWallSettings> entry : regionSettings.entrySet()) {
            String regionId = entry.getKey();
            RegionWallSettings settings = entry.getValue();

            if (!settings.validate(plugin, regionId)) {
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Settings for a single region's walls
     */
    public static class RegionWallSettings {
        public final String regionId;
        public final boolean enabled;
        public final Material wallMaterial;
        public final int wallHeight;
        public final int wallSegmentLength;
        public final int borderProximity;
        public final long updateThrottleMs;
        public final int groundAnchorRange;  // NEW: How far below player to anchor wall

        public RegionWallSettings(String regionId, boolean enabled, Material wallMaterial,
                                  int wallHeight, int wallSegmentLength,
                                  int borderProximity, long updateThrottleMs, int groundAnchorRange) {
            this.regionId = regionId;
            this.enabled = enabled;
            this.wallMaterial = wallMaterial;
            this.wallHeight = wallHeight;
            this.wallSegmentLength = wallSegmentLength;
            this.borderProximity = borderProximity;
            this.updateThrottleMs = updateThrottleMs;
            this.groundAnchorRange = groundAnchorRange;
        }

        public boolean validate(Plugin plugin, String regionId) {
            boolean valid = true;

            if (wallHeight <= 0 || wallHeight > 256) {
                plugin.getLogger().warning("[" + regionId + "] Invalid wall-height: " + wallHeight + " (must be 1-256)");
                valid = false;
            }

            if (wallSegmentLength <= 0 || wallSegmentLength > 100) {
                plugin.getLogger().warning("[" + regionId + "] Invalid wall-segment-length: " + wallSegmentLength + " (must be 1-100)");
                valid = false;
            }

            if (borderProximity <= 0 || borderProximity > 50) {
                plugin.getLogger().warning("[" + regionId + "] Invalid border-proximity: " + borderProximity + " (must be 1-50)");
                valid = false;
            }

            if (updateThrottleMs < 0 || updateThrottleMs > 5000) {
                plugin.getLogger().warning("[" + regionId + "] Invalid update-throttle-ms: " + updateThrottleMs + " (must be 0-5000)");
                valid = false;
            }

            if (groundAnchorRange < 0 || groundAnchorRange > 20) {
                plugin.getLogger().warning("[" + regionId + "] Invalid ground-anchor-range: " + groundAnchorRange + " (must be 0-20)");
                valid = false;
            }

            if (regionId == null || regionId.trim().isEmpty()) {
                valid = false;
            }

            return valid;
        }
    }
}