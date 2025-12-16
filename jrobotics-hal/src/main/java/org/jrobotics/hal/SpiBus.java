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
 * Abstraction for SPI (Serial Peripheral Interface) bus.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface SpiBus {
    
    /**
     * SPI modes (clock polarity and phase).
     */
    enum Mode {
        MODE_0, // CPOL=0, CPHA=0
        MODE_1, // CPOL=0, CPHA=1
        MODE_2, // CPOL=1, CPHA=0
        MODE_3  // CPOL=1, CPHA=1
    }
    
    /**
     * Gets the bus number.
     * 
     * @return the bus number
     */
    int getBusNumber();
    
    /**
     * Opens a connection to a device.
     * 
     * @param chipSelect the chip select line
     * @return the device handle
     */
    SpiDevice openDevice(int chipSelect);
    
    /**
     * Sets the SPI mode.
     * 
     * @param mode the mode
     */
    void setMode(Mode mode);
    
    /**
     * Sets the clock speed in Hz.
     * 
     * @param speed the clock speed
     */
    void setClockSpeed(int speed);
    
    /**
     * Closes the bus.
     */
    void close();
}
