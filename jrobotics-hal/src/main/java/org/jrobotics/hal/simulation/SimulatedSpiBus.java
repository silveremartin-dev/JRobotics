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

import org.jrobotics.hal.SpiBus;
import org.jrobotics.hal.SpiDevice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulated SPI bus for testing.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class SimulatedSpiBus implements SpiBus {
    
    private final int busNumber;
    private final Map<Integer, SimulatedSpiDevice> devices = new ConcurrentHashMap<>();
    private Mode mode = Mode.MODE_0;
    private int clockSpeed = 1000000;
    
    public SimulatedSpiBus(int busNumber) {
        this.busNumber = busNumber;
    }
    
    @Override
    public int getBusNumber() {
        return busNumber;
    }
    
    @Override
    public SpiDevice openDevice(int chipSelect) {
        return devices.computeIfAbsent(chipSelect, cs -> new SimulatedSpiDevice(cs));
    }
    
    @Override
    public void setMode(Mode mode) {
        this.mode = mode;
    }
    
    @Override
    public void setClockSpeed(int speed) {
        this.clockSpeed = speed;
    }
    
    @Override
    public void close() {
        devices.values().forEach(SpiDevice::close);
        devices.clear();
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public int getClockSpeed() {
        return clockSpeed;
    }
}
