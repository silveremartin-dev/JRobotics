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

import org.jrobotics.sensor.AbstractSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * LIDAR (Light Detection and Ranging) sensor implementation.
 * 
 * <p>LIDAR sensors measure distances using laser pulses and can produce
 * 2D or 3D point cloud data depending on the sensor type.</p>
 * 
 * <p>Reference sensors: RPLidar, Velodyne, Hokuyo</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class LidarSensor extends AbstractSensor<LidarScan> {
    
    private static final Logger logger = LoggerFactory.getLogger(LidarSensor.class);
    
    private final boolean simulationMode;
    private final int pointsPerScan;
    private final double angularResolution;
    private final double minRange;
    private final double maxRange;
    private volatile double simulatedBaseDistance = 5.0;
    
    /**
     * Constructs a new 2D LIDAR sensor with default settings.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     */
    public LidarSensor(String id, String name) {
        this(id, name, 360, 0.1, 12.0, true);
    }
    
    /**
     * Constructs a new LIDAR sensor with custom settings.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     * @param pointsPerScan the number of points per 360° scan
     * @param minRange the minimum range in meters
     * @param maxRange the maximum range in meters
     * @param simulationMode if true, returns simulated data
     */
    public LidarSensor(String id, String name, int pointsPerScan, 
                       double minRange, double maxRange, boolean simulationMode) {
        super(id, name, "m");
        this.pointsPerScan = pointsPerScan;
        this.angularResolution = (2.0 * Math.PI) / pointsPerScan;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.simulationMode = simulationMode;
        setSampleRate(10.0); // 10 Hz typical for 2D LIDAR
    }
    
    @Override
    protected LidarScan doRead(long timeoutMs) throws Exception {
        if (simulationMode) {
            return generateSimulatedScan();
        }
        
        // Hardware mode: use serial/USB for LIDAR driver if available
        // Currently falls back to simulated scan with warning
        logger.warn("[{}] LIDAR {} hardware mode - driver not configured, using simulated scan",
                System.currentTimeMillis(), getId());
        return generateSimulatedScan();
    }
    
    /**
     * Generates a simulated LIDAR scan for testing.
     * 
     * @return the simulated scan
     */
    private LidarScan generateSimulatedScan() {
        List<LidarPoint> points = new ArrayList<>(pointsPerScan);
        long timestamp = System.nanoTime();
        
        for (int i = 0; i < pointsPerScan; i++) {
            double azimuth = i * angularResolution;
            
            // Create a varied environment with walls and obstacles
            double distance = calculateSimulatedDistance(azimuth);
            
            // Add noise
            distance += (Math.random() - 0.5) * 0.02;
            distance = Math.max(minRange, Math.min(maxRange, distance));
            
            int intensity = (int) (255 * (1.0 - distance / maxRange));
            
            points.add(new LidarPoint(distance, azimuth, 0, intensity));
        }
        
        logger.trace("[{}] LIDAR {} generated {} points", 
                System.currentTimeMillis(), getId(), pointsPerScan);
        
        return new LidarScan(timestamp, points, angularResolution, minRange, maxRange);
    }
    
    /**
     * Calculates simulated distance for a given azimuth angle.
     * Creates a simple room-like environment.
     * 
     * @param azimuth the azimuth angle in radians
     * @return the simulated distance
     */
    private double calculateSimulatedDistance(double azimuth) {
        // Simple rectangular room simulation
        double roomWidth = 8.0;
        double roomLength = 10.0;
        
        double cosA = Math.cos(azimuth);
        double sinA = Math.sin(azimuth);
        
        // Calculate intersection with room walls
        double distX = (cosA > 0) ? roomWidth / 2 / cosA : -roomWidth / 2 / cosA;
        double distY = (sinA > 0) ? roomLength / 2 / sinA : -roomLength / 2 / sinA;
        
        return Math.min(Math.abs(distX), Math.abs(distY));
    }
    
    /**
     * Sets the base distance for simulation.
     * 
     * @param distance the base distance in meters
     */
    public void setSimulatedBaseDistance(double distance) {
        this.simulatedBaseDistance = Math.max(minRange, Math.min(maxRange, distance));
    }
    
    /**
     * Gets the angular resolution in radians.
     * 
     * @return the angular resolution
     */
    public double getAngularResolution() {
        return angularResolution;
    }
    
    /**
     * Gets the number of points per scan.
     * 
     * @return the points per scan
     */
    public int getPointsPerScan() {
        return pointsPerScan;
    }
    
    /**
     * Checks if running in simulation mode.
     * 
     * @return true if simulating
     */
    public boolean isSimulationMode() {
        return simulationMode;
    }
}
