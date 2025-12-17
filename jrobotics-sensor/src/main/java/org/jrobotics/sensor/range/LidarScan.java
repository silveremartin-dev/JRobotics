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

import java.util.Collections;
import java.util.List;

/**
 * Data class representing a complete LIDAR scan (point cloud).
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public record LidarScan(
        /** Timestamp when scan started in nanoseconds */
        long timestampNanos,
        /** All points in this scan */
        List<LidarPoint> points,
        /** Angular resolution in radians */
        double angularResolution,
        /** Minimum range in meters */
        double minRange,
        /** Maximum range in meters */
        double maxRange) {
    /**
     * Creates an empty LIDAR scan.
     * 
     * @return an empty scan
     */
    public static LidarScan empty() {
        return new LidarScan(System.nanoTime(), Collections.emptyList(), 0, 0, 0);
    }

    /**
     * Gets the number of points in this scan.
     * 
     * @return the point count
     */
    public int getPointCount() {
        return points.size();
    }

    /**
     * Gets the point at the specified index.
     * 
     * @param index the point index
     * @return the LIDAR point
     */
    public LidarPoint getPoint(int index) {
        return points.get(index);
    }

    /**
     * Finds the nearest point in the scan.
     * 
     * @return the nearest point, or null if empty
     */
    public LidarPoint findNearest() {
        return points.stream()
                .min((a, b) -> Double.compare(a.distance(), b.distance()))
                .orElse(null);
    }

    /**
     * Finds the farthest point in the scan.
     * 
     * @return the farthest point, or null if empty
     */
    public LidarPoint findFarthest() {
        return points.stream()
                .max((a, b) -> Double.compare(a.distance(), b.distance()))
                .orElse(null);
    }

    /**
     * Filters points within a given distance.
     * 
     * @param maxDistance the maximum distance in meters
     * @return a new scan with only nearby points
     */
    public LidarScan filterByDistance(double maxDistance) {
        List<LidarPoint> filtered = points.stream()
                .filter(p -> p.distance() <= maxDistance)
                .toList();
        return new LidarScan(timestampNanos, filtered, angularResolution, minRange, maxDistance);
    }

    /**
     * Filters points within an azimuth range.
     * 
     * @param minAzimuth the minimum azimuth in radians
     * @param maxAzimuth the maximum azimuth in radians
     * @return a new scan with only points in the azimuth range
     */
    public LidarScan filterByAzimuth(double minAzimuth, double maxAzimuth) {
        List<LidarPoint> filtered = points.stream()
                .filter(p -> p.azimuth() >= minAzimuth && p.azimuth() <= maxAzimuth)
                .toList();
        return new LidarScan(timestampNanos, filtered, angularResolution, minRange, maxRange);
    }

    /**
     * Gets the list of points.
     * 
     * @return the points
     */
    public List<LidarPoint> getPoints() {
        return points;
    }
}
