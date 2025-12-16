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

import org.jrobotics.hal.SerialPort;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Simulated serial port for testing.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class SimulatedSerialPort implements SerialPort {
    
    private final String portName;
    private final BlockingQueue<Byte> rxBuffer = new LinkedBlockingQueue<>();
    private final BlockingQueue<Byte> txBuffer = new LinkedBlockingQueue<>();
    private boolean open = false;
    private BaudRate baudRate = BaudRate.B115200;
    
    public SimulatedSerialPort(String portName) {
        this.portName = portName;
    }
    
    @Override
    public String getPortName() {
        return portName;
    }
    
    @Override
    public void open(BaudRate baudRate) {
        this.baudRate = baudRate;
        this.open = true;
    }
    
    @Override
    public boolean isOpen() {
        return open;
    }
    
    @Override
    public void write(byte[] data) {
        for (byte b : data) {
            txBuffer.offer(b);
        }
    }
    
    @Override
    public void write(String text) {
        write(text.getBytes(StandardCharsets.UTF_8));
    }
    
    @Override
    public byte[] read() {
        byte[] result = new byte[rxBuffer.size()];
        for (int i = 0; i < result.length; i++) {
            Byte b = rxBuffer.poll();
            result[i] = (b != null) ? b : 0;
        }
        return result;
    }
    
    @Override
    public byte[] read(int length, long timeoutMs) {
        byte[] result = new byte[length];
        long deadline = System.currentTimeMillis() + timeoutMs;
        
        for (int i = 0; i < length; i++) {
            try {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) break;
                
                Byte b = rxBuffer.poll(remaining, TimeUnit.MILLISECONDS);
                result[i] = (b != null) ? b : 0;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return result;
    }
    
    @Override
    public int available() {
        return rxBuffer.size();
    }
    
    @Override
    public void flush() {
        txBuffer.clear();
    }
    
    @Override
    public void close() {
        open = false;
        rxBuffer.clear();
        txBuffer.clear();
    }
    
    /**
     * Injects data into the receive buffer for testing.
     */
    public void injectRxData(byte[] data) {
        for (byte b : data) {
            rxBuffer.offer(b);
        }
    }
    
    /**
     * Injects a string into the receive buffer.
     */
    public void injectRxData(String text) {
        injectRxData(text.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Gets data from the transmit buffer.
     */
    public byte[] getTxData() {
        byte[] result = new byte[txBuffer.size()];
        for (int i = 0; i < result.length; i++) {
            Byte b = txBuffer.poll();
            result[i] = (b != null) ? b : 0;
        }
        return result;
    }
    
    public BaudRate getBaudRate() {
        return baudRate;
    }
}
