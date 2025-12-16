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
 * Provider interface for hardware abstraction layer.
 * 
 * <p>Implementations provide platform-specific access to hardware.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface HalProvider {
    
    /**
     * Gets the provider name.
     * 
     * @return the name (e.g., "RaspberryPi", "Simulation")
     */
    String getName();
    
    /**
     * Gets the platform type.
     * 
     * @return the platform
     */
    Platform getPlatform();
    
    /**
     * Gets a GPIO pin.
     * 
     * @param pinNumber the pin number
     * @return the GPIO pin
     */
    GpioPin getGpioPin(int pinNumber);
    
    /**
     * Gets an I2C bus.
     * 
     * @param busNumber the bus number
     * @return the I2C bus
     */
    I2CBus getI2CBus(int busNumber);
    
    /**
     * Gets an SPI bus.
     * 
     * @param busNumber the bus number
     * @return the SPI bus
     */
    SpiBus getSpiBus(int busNumber);
    
    /**
     * Gets a serial port.
     * 
     * @param portName the port name
     * @return the serial port
     */
    SerialPort getSerialPort(String portName);
    
    /**
     * Lists available serial ports.
     * 
     * @return array of port names
     */
    String[] listSerialPorts();
    
    /**
     * Checks if running on real hardware.
     * 
     * @return true if real hardware, false if simulation
     */
    boolean isRealHardware();
    
    /**
     * Shuts down and releases resources.
     */
    void shutdown();
    
    /**
     * Supported platforms.
     */
    enum Platform {
        RASPBERRY_PI,
        JETSON,
        ARDUINO,
        ESP32,
        BEAGLEBONE,
        GENERIC_LINUX,
        WINDOWS,
        MACOS,
        SIMULATION
    }
}
