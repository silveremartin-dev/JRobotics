/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.hal;

/**
 * Abstraction for I2C (Inter-Integrated Circuit) bus communication.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface I2CBus {
    
    /**
     * Gets the bus number.
     * 
     * @return the bus number
     */
    int getBusNumber();
    
    /**
     * Opens a connection to a device.
     * 
     * @param address the 7-bit I2C address
     * @return the device handle
     */
    I2CDevice openDevice(int address);
    
    /**
     * Scans for devices on the bus.
     * 
     * @return array of detected addresses
     */
    int[] scanDevices();
    
    /**
     * Closes the bus.
     */
    void close();
}
