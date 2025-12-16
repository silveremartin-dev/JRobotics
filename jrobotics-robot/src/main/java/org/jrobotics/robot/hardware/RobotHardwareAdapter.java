/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.robot.hardware;

import org.jrobotics.hal.HalProvider;

/**
 * Adapter interface for real robot hardware.
 * 
 * <p>Implementations connect to specific robot hardware platforms.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface RobotHardwareAdapter {
    
    /**
     * Gets the adapter name.
     * 
     * @return the name (e.g., "TurtleBot3", "Create3")
     */
    String getName();
    
    /**
     * Gets the platform description.
     * 
     * @return the description
     */
    String getDescription();
    
    /**
     * Initializes the hardware connection.
     * 
     * @return true if successful
     */
    boolean initialize();
    
    /**
     * Checks if connected to hardware.
     * 
     * @return true if connected
     */
    boolean isConnected();
    
    /**
     * Gets the HAL provider for this hardware.
     * 
     * @return the HAL provider
     */
    HalProvider getHalProvider();
    
    /**
     * Sends velocity command.
     * 
     * @param linearX linear X velocity in m/s
     * @param linearY linear Y velocity in m/s (for holonomic)
     * @param angularZ angular Z velocity in rad/s
     */
    void setVelocity(double linearX, double linearY, double angularZ);
    
    /**
     * Stops all motion.
     */
    void stop();
    
    /**
     * Emergency stop - immediate halt.
     */
    void emergencyStop();
    
    /**
     * Gets the current odometry.
     * 
     * @return [x, y, theta, vx, vy, omega]
     */
    double[] getOdometry();
    
    /**
     * Gets battery level (0-100%).
     * 
     * @return battery percentage
     */
    double getBatteryLevel();
    
    /**
     * Disconnects from hardware.
     */
    void disconnect();
}
