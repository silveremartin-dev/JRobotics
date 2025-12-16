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

import org.jrobotics.hal.I2CBus;
import org.jrobotics.hal.I2CDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulated I2C bus for testing.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class SimulatedI2CBus implements I2CBus {
    
    private static final Logger logger = LoggerFactory.getLogger(SimulatedI2CBus.class);
    
    private final int busNumber;
    private final Map<Integer, SimulatedI2CDevice> devices = new ConcurrentHashMap<>();
    private final Set<Integer> availableAddresses = ConcurrentHashMap.newKeySet();
    
    public SimulatedI2CBus(int busNumber) {
        this.busNumber = busNumber;
        // Add some common simulated device addresses
        availableAddresses.addAll(Arrays.asList(0x68, 0x69, 0x1E, 0x53, 0x77));
    }
    
    @Override
    public int getBusNumber() {
        return busNumber;
    }
    
    @Override
    public I2CDevice openDevice(int address) {
        return devices.computeIfAbsent(address, addr -> {
            logger.debug("Opening I2C device at 0x{} on bus {}", Integer.toHexString(addr), busNumber);
            return new SimulatedI2CDevice(addr);
        });
    }
    
    @Override
    public int[] scanDevices() {
        return availableAddresses.stream().mapToInt(Integer::intValue).toArray();
    }
    
    @Override
    public void close() {
        devices.values().forEach(I2CDevice::close);
        devices.clear();
    }
    
    /**
     * Adds a simulated device address.
     */
    public void addDevice(int address) {
        availableAddresses.add(address);
    }
}
