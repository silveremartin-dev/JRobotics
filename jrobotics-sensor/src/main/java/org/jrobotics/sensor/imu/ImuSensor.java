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

import org.jrobotics.sensor.AbstractSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inertial Measurement Unit (IMU) sensor implementation.
 * 
 * <p>An IMU combines accelerometer, gyroscope, and optionally magnetometer
 * sensors to provide orientation and motion data.</p>
 * 
 * <p>Reference sensors: MPU6050, MPU9250, BNO055</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class ImuSensor extends AbstractSensor<ImuData> {
    
    private static final Logger logger = LoggerFactory.getLogger(ImuSensor.class);
    
    // Gravity constant for simulation (m/s²)
    private static final double GRAVITY = 9.81;
    
    private final boolean simulationMode;
    private volatile ImuData simulatedData;
    
    // IMU configuration
    private volatile int accelRange = 2;  // ±2g, ±4g, ±8g, ±16g
    private volatile int gyroRange = 250; // ±250, ±500, ±1000, ±2000 deg/s
    
    /**
     * Constructs a new IMU sensor.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     */
    public ImuSensor(String id, String name) {
        this(id, name, true);
    }
    
    /**
     * Constructs a new IMU sensor.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     * @param simulationMode if true, returns simulated data
     */
    public ImuSensor(String id, String name, boolean simulationMode) {
        super(id, name, "m/s², rad/s");
        this.simulationMode = simulationMode;
        this.simulatedData = ImuData.of(0, 0, GRAVITY, 0, 0, 0);
        setSampleRate(100.0); // Typical IMU rate
    }
    
    @Override
    protected ImuData doRead(long timeoutMs) throws Exception {
        if (simulationMode) {
            // Add noise to simulated readings
            double accelNoise = 0.01;
            double gyroNoise = 0.001;
            
            ImuData data = new ImuData(
                    simulatedData.accelX() + (Math.random() - 0.5) * accelNoise,
                    simulatedData.accelY() + (Math.random() - 0.5) * accelNoise,
                    simulatedData.accelZ() + (Math.random() - 0.5) * accelNoise,
                    simulatedData.gyroX() + (Math.random() - 0.5) * gyroNoise,
                    simulatedData.gyroY() + (Math.random() - 0.5) * gyroNoise,
                    simulatedData.gyroZ() + (Math.random() - 0.5) * gyroNoise,
                    simulatedData.magX(),
                    simulatedData.magY(),
                    simulatedData.magZ(),
                    simulatedData.temperature()
            );
            
            logger.trace("[{}] IMU {} simulated read: accel=[{}, {}, {}], gyro=[{}, {}, {}]",
                    System.currentTimeMillis(), getId(),
                    data.accelX(), data.accelY(), data.accelZ(),
                    data.gyroX(), data.gyroY(), data.gyroZ());
            
            return data;
        }
        
        // TODO: Implement actual hardware reading via I2C/SPI
        throw new UnsupportedOperationException("Hardware mode not yet implemented");
    }
    
    /**
     * Sets the simulated IMU data for testing.
     * 
     * @param data the IMU data
     */
    public void setSimulatedData(ImuData data) {
        this.simulatedData = data;
    }
    
    /**
     * Sets the accelerometer range.
     * 
     * @param gRange the range in g (2, 4, 8, or 16)
     */
    public void setAccelRange(int gRange) {
        if (gRange != 2 && gRange != 4 && gRange != 8 && gRange != 16) {
            throw new IllegalArgumentException("Invalid accel range: " + gRange);
        }
        this.accelRange = gRange;
        logger.debug("[{}] Accel range set to ±{} g", System.currentTimeMillis(), gRange);
    }
    
    /**
     * Sets the gyroscope range.
     * 
     * @param dpsRange the range in degrees per second (250, 500, 1000, or 2000)
     */
    public void setGyroRange(int dpsRange) {
        if (dpsRange != 250 && dpsRange != 500 && dpsRange != 1000 && dpsRange != 2000) {
            throw new IllegalArgumentException("Invalid gyro range: " + dpsRange);
        }
        this.gyroRange = dpsRange;
        logger.debug("[{}] Gyro range set to ±{} deg/s", System.currentTimeMillis(), dpsRange);
    }
    
    /**
     * Gets the accelerometer range.
     * 
     * @return the range in g
     */
    public int getAccelRange() {
        return accelRange;
    }
    
    /**
     * Gets the gyroscope range.
     * 
     * @return the range in degrees per second
     */
    public int getGyroRange() {
        return gyroRange;
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
