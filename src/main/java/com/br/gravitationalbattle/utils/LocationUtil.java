package com.br.gravitationalbattle.utils;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtil {

    /**
     * Converts a location to a string format
     * Format: worldUUID:x:y:z:yaw:pitch
     *
     * @param location The location to convert
     * @return String representation of the location
     */
    public static String locationToString(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        return location.getWorld().getUID() + ":" +
                location.getX() + ":" +
                location.getY() + ":" +
                location.getZ() + ":" +
                location.getYaw() + ":" +
                location.getPitch();
    }

    /**
     * Converts a string to a location
     * Format: worldUUID:x:y:z:yaw:pitch
     *
     * @param locationStr The string to convert
     * @return Location object or null if invalid
     */
    public static Location stringToLocation(String locationStr) {
        if (locationStr == null || locationStr.isEmpty()) {
            return null;
        }

        String[] parts = locationStr.split(":");

        if (parts.length < 4) {
            return null;
        }

        try {
            UUID worldUUID = UUID.fromString(parts[0]);
            World world = Bukkit.getWorld(worldUUID);

            if (world == null) {
                return null;
            }

            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);

            float yaw = 0;
            float pitch = 0;

            if (parts.length >= 5) {
                yaw = Float.parseFloat(parts[4]);
            }

            if (parts.length >= 6) {
                pitch = Float.parseFloat(parts[5]);
            }

            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if two locations are in the same block
     *
     * @param loc1 First location
     * @param loc2 Second location
     * @return True if in the same block
     */
    public static boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        if (!loc1.getWorld().equals(loc2.getWorld())) return false;

        return loc1.getBlockX() == loc2.getBlockX() &&
                loc1.getBlockY() == loc2.getBlockY() &&
                loc1.getBlockZ() == loc2.getBlockZ();
    }

    /**
     * Gets the distance between two locations (ignoring Y)
     *
     * @param loc1 First location
     * @param loc2 Second location
     * @return Distance in blocks (ignoring Y)
     */
    public static double distanceSquaredXZ(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return Double.MAX_VALUE;
        if (!loc1.getWorld().equals(loc2.getWorld())) return Double.MAX_VALUE;

        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();

        return dx * dx + dz * dz;
    }
}