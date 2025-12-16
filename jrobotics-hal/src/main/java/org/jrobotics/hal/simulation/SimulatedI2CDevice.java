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

import org.jrobotics.hal.I2CDevice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulated I2C device for testing.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class SimulatedI2CDevice implements I2CDevice {
    
    private final int address;
    private final Map<Integer, Byte> registers = new ConcurrentHashMap<>();
    
    public SimulatedI2CDevice(int address) {
        this.address = address;
    }
    
    @Override
    public int getAddress() {
        return address;
    }
    
    @Override
    public byte readByte(int register) {
        return registers.getOrDefault(register, (byte) 0);
    }
    
    @Override
    public byte[] readBytes(int register, int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = registers.getOrDefault(register + i, (byte) 0);
        }
        return result;
    }
    
    @Override
    public void writeByte(int register, byte value) {
        registers.put(register, value);
    }
    
    @Override
    public void writeBytes(int register, byte[] data) {
        for (int i = 0; i < data.length; i++) {
            registers.put(register + i, data[i]);
        }
    }
    
    @Override
    public int readWord(int register) {
        byte high = registers.getOrDefault(register, (byte) 0);
        byte low = registers.getOrDefault(register + 1, (byte) 0);
        return ((high & 0xFF) << 8) | (low & 0xFF);
    }
    
    @Override
    public void writeWord(int register, int value) {
        registers.put(register, (byte) ((value >> 8) & 0xFF));
        registers.put(register + 1, (byte) (value & 0xFF));
    }
    
    @Override
    public void close() {
        registers.clear();
    }
    
    /**
     * Sets a register value for testing.
     */
    public void setRegister(int register, byte value) {
        registers.put(register, value);
    }
}
