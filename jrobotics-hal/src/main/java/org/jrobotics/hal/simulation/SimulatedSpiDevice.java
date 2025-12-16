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

import org.jrobotics.hal.SpiDevice;

import java.util.Arrays;

/**
 * Simulated SPI device for testing.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class SimulatedSpiDevice implements SpiDevice {
    
    private final int chipSelect;
    private byte[] simulatedResponse = new byte[0];
    
    public SimulatedSpiDevice(int chipSelect) {
        this.chipSelect = chipSelect;
    }
    
    @Override
    public int getChipSelect() {
        return chipSelect;
    }
    
    @Override
    public byte[] transfer(byte[] txData) {
        byte[] result = new byte[txData.length];
        int copyLen = Math.min(txData.length, simulatedResponse.length);
        System.arraycopy(simulatedResponse, 0, result, 0, copyLen);
        return result;
    }
    
    @Override
    public void write(byte[] data) {
        // Simulated - no action needed
    }
    
    @Override
    public byte[] read(int length) {
        return Arrays.copyOf(simulatedResponse, length);
    }
    
    @Override
    public void close() {
        simulatedResponse = new byte[0];
    }
    
    /**
     * Sets the simulated response data.
     */
    public void setSimulatedResponse(byte[] response) {
        this.simulatedResponse = response.clone();
    }
}
