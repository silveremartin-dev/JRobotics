/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.sensor.fusion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Complementary filter for fusing accelerometer and gyroscope data.
 * 
 * <p>A simple but effective alternative to a full Kalman filter for
 * attitude estimation. Combines high-frequency gyroscope data with
 * low-frequency accelerometer data.</p>
 * 
 * <p>Reference: Colton, "The Balance Filter"</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class ComplementaryFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(ComplementaryFilter.class);
    
    private double alpha;  // Filter coefficient (0-1)
    private double roll;
    private double pitch;
    private double yaw;
    private long lastTimestamp = -1;
    
    /**
     * Constructs a complementary filter.
     * 
     * @param alpha the filter coefficient (0.0 = accelerometer only, 1.0 = gyro only)
     */
    public ComplementaryFilter(double alpha) {
        this.alpha = Math.max(0, Math.min(1, alpha));
    }
    
    /**
     * Creates a filter with typical settings.
     * 
     * @return the filter
     */
    public static ComplementaryFilter standard() {
        return new ComplementaryFilter(0.98);
    }
    
    /**
     * Updates the filter with new IMU data.
     * 
     * @param accelX X acceleration (m/s²)
     * @param accelY Y acceleration (m/s²)
     * @param accelZ Z acceleration (m/s²)
     * @param gyroX X angular velocity (rad/s)
     * @param gyroY Y angular velocity (rad/s)
     * @param gyroZ Z angular velocity (rad/s)
     * @param timestamp the timestamp in milliseconds
     */
    public void update(double accelX, double accelY, double accelZ,
                       double gyroX, double gyroY, double gyroZ,
                       long timestamp) {
        
        // Calculate dt
        double dt = 0.01; // default 10ms
        if (lastTimestamp > 0) {
            dt = (timestamp - lastTimestamp) / 1000.0;
        }
        lastTimestamp = timestamp;
        
        // Calculate roll and pitch from accelerometer
        double accelRoll = Math.atan2(accelY, accelZ);
        double accelPitch = Math.atan2(-accelX, Math.sqrt(accelY * accelY + accelZ * accelZ));
        
        // Integrate gyroscope
        double gyroRoll = roll + gyroX * dt;
        double gyroPitch = pitch + gyroY * dt;
        double gyroYaw = yaw + gyroZ * dt;
        
        // Complementary filter: combine gyro (high freq) with accel (low freq)
        roll = alpha * gyroRoll + (1 - alpha) * accelRoll;
        pitch = alpha * gyroPitch + (1 - alpha) * accelPitch;
        yaw = gyroYaw; // Yaw can only come from gyro (or magnetometer)
        
        logger.trace("Complementary filter: roll={}, pitch={}, yaw={}", 
                Math.toDegrees(roll), Math.toDegrees(pitch), Math.toDegrees(yaw));
    }
    
    /**
     * Gets the filtered roll angle.
     * 
     * @return roll in radians
     */
    public double getRoll() {
        return roll;
    }
    
    /**
     * Gets the filtered pitch angle.
     * 
     * @return pitch in radians
     */
    public double getPitch() {
        return pitch;
    }
    
    /**
     * Gets the filtered yaw angle.
     * 
     * @return yaw in radians
     */
    public double getYaw() {
        return yaw;
    }
    
    /**
     * Gets the orientation as [roll, pitch, yaw] in radians.
     * 
     * @return the orientation array
     */
    public double[] getOrientation() {
        return new double[] { roll, pitch, yaw };
    }
    
    /**
     * Resets the filter.
     */
    public void reset() {
        roll = 0;
        pitch = 0;
        yaw = 0;
        lastTimestamp = -1;
    }
    
    /**
     * Sets the alpha coefficient.
     * 
     * @param alpha the new alpha (0-1)
     */
    public void setAlpha(double alpha) {
        this.alpha = Math.max(0, Math.min(1, alpha));
    }
}
