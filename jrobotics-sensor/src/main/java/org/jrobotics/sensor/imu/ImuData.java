/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.sensor.imu;

/**
 * Data class representing Inertial Measurement Unit (IMU) readings.
 * 
 * <p>Contains accelerometer, gyroscope, and optional magnetometer data.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public record ImuData(
    /** Acceleration on X-axis in m/s² */
    double accelX,
    /** Acceleration on Y-axis in m/s² */
    double accelY,
    /** Acceleration on Z-axis in m/s² */
    double accelZ,
    /** Angular velocity around X-axis in rad/s */
    double gyroX,
    /** Angular velocity around Y-axis in rad/s */
    double gyroY,
    /** Angular velocity around Z-axis in rad/s */
    double gyroZ,
    /** Magnetic field on X-axis in µT (optional, may be 0) */
    double magX,
    /** Magnetic field on Y-axis in µT (optional, may be 0) */
    double magY,
    /** Magnetic field on Z-axis in µT (optional, may be 0) */
    double magZ,
    /** Temperature in °C (optional, may be 0) */
    double temperature
) {
    /**
     * Creates an IMU data record with only accelerometer and gyroscope data.
     * 
     * @param accelX acceleration X in m/s²
     * @param accelY acceleration Y in m/s²
     * @param accelZ acceleration Z in m/s²
     * @param gyroX angular velocity X in rad/s
     * @param gyroY angular velocity Y in rad/s
     * @param gyroZ angular velocity Z in rad/s
     * @return the IMU data record
     */
    public static ImuData of(double accelX, double accelY, double accelZ,
                              double gyroX, double gyroY, double gyroZ) {
        return new ImuData(accelX, accelY, accelZ, gyroX, gyroY, gyroZ, 0, 0, 0, 0);
    }
    
    /**
     * Gets the magnitude of the acceleration vector.
     * 
     * @return the acceleration magnitude in m/s²
     */
    public double getAccelMagnitude() {
        return Math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
    }
    
    /**
     * Gets the magnitude of the angular velocity vector.
     * 
     * @return the angular velocity magnitude in rad/s
     */
    public double getGyroMagnitude() {
        return Math.sqrt(gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ);
    }
    
    /**
     * Gets the magnitude of the magnetic field vector.
     * 
     * @return the magnetic field magnitude in µT
     */
    public double getMagMagnitude() {
        return Math.sqrt(magX * magX + magY * magY + magZ * magZ);
    }
    
    /**
     * Calculates the roll angle from accelerometer data.
     * 
     * <p>Reference: Tilt Sensing Using a Three-Axis Accelerometer (Freescale AN3461)</p>
     * 
     * @return the roll angle in radians
     */
    public double getRoll() {
        return Math.atan2(accelY, accelZ);
    }
    
    /**
     * Calculates the pitch angle from accelerometer data.
     * 
     * <p>Reference: Tilt Sensing Using a Three-Axis Accelerometer (Freescale AN3461)</p>
     * 
     * @return the pitch angle in radians
     */
    public double getPitch() {
        return Math.atan2(-accelX, Math.sqrt(accelY * accelY + accelZ * accelZ));
    }
}
