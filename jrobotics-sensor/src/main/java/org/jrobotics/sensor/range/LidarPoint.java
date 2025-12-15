/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.sensor.range;

/**
 * Data class representing a single point in LIDAR point cloud data.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public record LidarPoint(
    /** Distance in meters */
    double distance,
    /** Azimuth angle in radians (horizontal plane) */
    double azimuth,
    /** Elevation angle in radians (vertical plane) */
    double elevation,
    /** Intensity/reflectivity value (0-255) */
    int intensity
) {
    /**
     * Creates a 2D LIDAR point (no elevation).
     * 
     * @param distance the distance in meters
     * @param azimuth the azimuth angle in radians
     * @return the LIDAR point
     */
    public static LidarPoint of2D(double distance, double azimuth) {
        return new LidarPoint(distance, azimuth, 0, 0);
    }
    
    /**
     * Creates a 2D LIDAR point with intensity.
     * 
     * @param distance the distance in meters
     * @param azimuth the azimuth angle in radians
     * @param intensity the intensity value
     * @return the LIDAR point
     */
    public static LidarPoint of2D(double distance, double azimuth, int intensity) {
        return new LidarPoint(distance, azimuth, 0, intensity);
    }
    
    /**
     * Converts to Cartesian X coordinate.
     * 
     * @return the X coordinate in meters
     */
    public double toX() {
        return distance * Math.cos(elevation) * Math.cos(azimuth);
    }
    
    /**
     * Converts to Cartesian Y coordinate.
     * 
     * @return the Y coordinate in meters
     */
    public double toY() {
        return distance * Math.cos(elevation) * Math.sin(azimuth);
    }
    
    /**
     * Converts to Cartesian Z coordinate.
     * 
     * @return the Z coordinate in meters
     */
    public double toZ() {
        return distance * Math.sin(elevation);
    }
}
