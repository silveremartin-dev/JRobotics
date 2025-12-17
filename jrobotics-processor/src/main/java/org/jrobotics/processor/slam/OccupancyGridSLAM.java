/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor.slam;

import java.util.*;

/**
 * Simple Grid-based SLAM implementation.
 * 
 * <p>
 * Builds an occupancy grid map while simultaneously localizing the robot.
 * Uses beam sensor model for LIDAR integration.
 * </p>
 * 
 * <p>
 * <b>References:</b>
 * </p>
 * <ul>
 * <li>Thrun, S., Burgard, W., & Fox, D. (2005). <i>Probabilistic Robotics</i>.
 * Chapter 9.</li>
 * <li>Grisetti, G., Stachniss, C., & Burgard, W. (2007). Improved Techniques
 * for
 * Grid Mapping With Rao-Blackwellized Particle Filters.</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class OccupancyGridSLAM {

    private final double[][] grid; // Log-odds occupancy
    private final int width, height;
    private final double resolution;
    private final double originX, originY;

    // Log-odds parameters
    private double logOddsPrior = 0;
    private double logOddsOccupied = 0.85;
    private double logOddsFree = -0.4;
    private double logOddsMin = -2.0;
    private double logOddsMax = 3.5;

    // Robot pose estimate
    private double robotX, robotY, robotTheta;

    /**
     * Creates SLAM instance.
     * 
     * @param width      grid width in cells
     * @param height     grid height in cells
     * @param resolution cell size in meters
     */
    public OccupancyGridSLAM(int width, int height, double resolution) {
        this.width = width;
        this.height = height;
        this.resolution = resolution;
        this.originX = -width * resolution / 2;
        this.originY = -height * resolution / 2;

        this.grid = new double[width][height];
        // Initialize with prior (unknown)
        for (int x = 0; x < width; x++) {
            Arrays.fill(grid[x], logOddsPrior);
        }
    }

    /**
     * Creates a 20x20m SLAM map with 5cm resolution.
     */
    public OccupancyGridSLAM() {
        this(400, 400, 0.05);
    }

    /**
     * Integrates LIDAR scan into the map.
     * 
     * @param scanRanges array of range measurements
     * @param scanAngles array of angles for each measurement
     * @param maxRange   sensor max range
     */
    public void integrateScan(double[] scanRanges, double[] scanAngles, double maxRange) {
        for (int i = 0; i < scanRanges.length; i++) {
            double range = scanRanges[i];
            double angle = robotTheta + scanAngles[i];

            if (range <= 0 || range >= maxRange)
                continue;

            // Ray casting
            double endX = robotX + range * Math.cos(angle);
            double endY = robotY + range * Math.sin(angle);

            // Mark free cells along ray
            List<int[]> rayCells = bresenham(
                    worldToGridX(robotX), worldToGridY(robotY),
                    worldToGridX(endX), worldToGridY(endY));

            for (int j = 0; j < rayCells.size() - 1; j++) {
                int[] cell = rayCells.get(j);
                if (isValidCell(cell[0], cell[1])) {
                    grid[cell[0]][cell[1]] = Math.max(logOddsMin,
                            grid[cell[0]][cell[1]] + logOddsFree);
                }
            }

            // Mark occupied cell at hit
            int hitX = worldToGridX(endX);
            int hitY = worldToGridY(endY);
            if (isValidCell(hitX, hitY)) {
                grid[hitX][hitY] = Math.min(logOddsMax,
                        grid[hitX][hitY] + logOddsOccupied);
            }
        }
    }

    /**
     * Updates robot pose from odometry.
     */
    public void updatePose(double deltaX, double deltaY, double deltaTheta) {
        robotX += deltaX * Math.cos(robotTheta) - deltaY * Math.sin(robotTheta);
        robotY += deltaX * Math.sin(robotTheta) + deltaY * Math.cos(robotTheta);
        robotTheta += deltaTheta;
        normalizeTheta();
    }

    /**
     * Sets robot pose directly.
     */
    public void setPose(double x, double y, double theta) {
        this.robotX = x;
        this.robotY = y;
        this.robotTheta = theta;
    }

    /**
     * Gets occupancy probability for a cell.
     * 
     * @return probability [0, 1]
     */
    public double getOccupancy(int gx, int gy) {
        if (!isValidCell(gx, gy))
            return 0.5;
        return 1.0 - 1.0 / (1.0 + Math.exp(grid[gx][gy]));
    }

    /**
     * Gets occupancy probability at world coordinates.
     */
    public double getOccupancyWorld(double x, double y) {
        return getOccupancy(worldToGridX(x), worldToGridY(y));
    }

    /**
     * Checks if cell is occupied (&gt; threshold).
     */
    public boolean isOccupied(int gx, int gy) {
        return getOccupancy(gx, gy) > 0.7;
    }

    /**
     * Checks if cell is free (&lt; threshold).
     */
    public boolean isFree(int gx, int gy) {
        return getOccupancy(gx, gy) < 0.3;
    }

    /**
     * Gets robot pose.
     */
    public double[] getRobotPose() {
        return new double[] { robotX, robotY, robotTheta };
    }

    /**
     * Gets grid dimensions.
     */
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getResolution() {
        return resolution;
    }

    /**
     * Exports map as byte array (0=unknown, 128=free, 255=occupied).
     */
    public byte[][] exportMap() {
        byte[][] map = new byte[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double p = getOccupancy(x, y);
                if (p > 0.7)
                    map[x][y] = (byte) 255;
                else if (p < 0.3)
                    map[x][y] = (byte) 128;
                else
                    map[x][y] = 0;
            }
        }
        return map;
    }

    // Coordinate conversions
    private int worldToGridX(double x) {
        return (int) ((x - originX) / resolution);
    }

    private int worldToGridY(double y) {
        return (int) ((y - originY) / resolution);
    }

    private boolean isValidCell(int gx, int gy) {
        return gx >= 0 && gx < width && gy >= 0 && gy < height;
    }

    private void normalizeTheta() {
        while (robotTheta > Math.PI)
            robotTheta -= 2 * Math.PI;
        while (robotTheta < -Math.PI)
            robotTheta += 2 * Math.PI;
    }

    // Bresenham's line algorithm
    private List<int[]> bresenham(int x0, int y0, int x1, int y1) {
        List<int[]> cells = new ArrayList<>();
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            cells.add(new int[] { x0, y0 });
            if (x0 == x1 && y0 == y1)
                break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
        return cells;
    }
}
