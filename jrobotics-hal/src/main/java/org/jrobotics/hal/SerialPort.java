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
 * Abstraction for UART (Universal Asynchronous Receiver-Transmitter) serial port.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface SerialPort {
    
    /**
     * Common baud rates.
     */
    enum BaudRate {
        B9600(9600),
        B19200(19200),
        B38400(38400),
        B57600(57600),
        B115200(115200),
        B230400(230400),
        B460800(460800),
        B921600(921600);
        
        private final int value;
        
        BaudRate(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    /**
     * Gets the port name.
     * 
     * @return the port name (e.g., "/dev/ttyUSB0" or "COM3")
     */
    String getPortName();
    
    /**
     * Opens the port.
     * 
     * @param baudRate the baud rate
     */
    void open(BaudRate baudRate);
    
    /**
     * Checks if the port is open.
     * 
     * @return true if open
     */
    boolean isOpen();
    
    /**
     * Writes data.
     * 
     * @param data the data to write
     */
    void write(byte[] data);
    
    /**
     * Writes a string.
     * 
     * @param text the text to write
     */
    void write(String text);
    
    /**
     * Reads available data.
     * 
     * @return the received data
     */
    byte[] read();
    
    /**
     * Reads a specific number of bytes.
     * 
     * @param length the number of bytes
     * @param timeoutMs the timeout in milliseconds
     * @return the received data
     */
    byte[] read(int length, long timeoutMs);
    
    /**
     * Gets the number of bytes available to read.
     * 
     * @return the available count
     */
    int available();
    
    /**
     * Flushes the output buffer.
     */
    void flush();
    
    /**
     * Closes the port.
     */
    void close();
}
