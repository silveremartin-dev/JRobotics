/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.hal.simulation;

import org.jrobotics.hal.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulated HAL provider for testing without hardware.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class SimulatedHalProvider implements HalProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(SimulatedHalProvider.class);
    
    private final Map<Integer, SimulatedGpioPin> gpioPins = new ConcurrentHashMap<>();
    private final Map<Integer, SimulatedI2CBus> i2cBuses = new ConcurrentHashMap<>();
    private final Map<Integer, SimulatedSpiBus> spiBuses = new ConcurrentHashMap<>();
    private final Map<String, SimulatedSerialPort> serialPorts = new ConcurrentHashMap<>();
    
    public SimulatedHalProvider() {
        logger.info("SimulatedHalProvider initialized");
    }
    
    @Override
    public String getName() {
        return "Simulation";
    }
    
    @Override
    public Platform getPlatform() {
        return Platform.SIMULATION;
    }
    
    @Override
    public GpioPin getGpioPin(int pinNumber) {
        return gpioPins.computeIfAbsent(pinNumber, SimulatedGpioPin::new);
    }
    
    @Override
    public I2CBus getI2CBus(int busNumber) {
        return i2cBuses.computeIfAbsent(busNumber, SimulatedI2CBus::new);
    }
    
    @Override
    public SpiBus getSpiBus(int busNumber) {
        return spiBuses.computeIfAbsent(busNumber, SimulatedSpiBus::new);
    }
    
    @Override
    public SerialPort getSerialPort(String portName) {
        return serialPorts.computeIfAbsent(portName, SimulatedSerialPort::new);
    }
    
    @Override
    public String[] listSerialPorts() {
        return new String[] { "/dev/ttyUSB0", "/dev/ttyACM0", "COM1", "COM3" };
    }
    
    @Override
    public boolean isRealHardware() {
        return false;
    }
    
    @Override
    public void shutdown() {
        gpioPins.clear();
        i2cBuses.values().forEach(I2CBus::close);
        spiBuses.values().forEach(SpiBus::close);
        serialPorts.values().forEach(SerialPort::close);
        logger.info("SimulatedHalProvider shutdown");
    }
    
    /**
     * Gets a simulated GPIO pin for direct manipulation.
     */
    public SimulatedGpioPin getSimulatedGpioPin(int pinNumber) {
        return (SimulatedGpioPin) getGpioPin(pinNumber);
    }
}
