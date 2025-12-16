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
 * Abstraction for an I2C device.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface I2CDevice {
    
    /**
     * Gets the device address.
     * 
     * @return the 7-bit address
     */
    int getAddress();
    
    /**
     * Reads a byte from a register.
     * 
     * @param register the register address
     * @return the byte value
     */
    byte readByte(int register);
    
    /**
     * Reads multiple bytes from a register.
     * 
     * @param register the register address
     * @param length the number of bytes
     * @return the byte array
     */
    byte[] readBytes(int register, int length);
    
    /**
     * Writes a byte to a register.
     * 
     * @param register the register address
     * @param value the byte value
     */
    void writeByte(int register, byte value);
    
    /**
     * Writes multiple bytes to a register.
     * 
     * @param register the register address
     * @param data the byte array
     */
    void writeBytes(int register, byte[] data);
    
    /**
     * Reads a 16-bit word (big-endian).
     * 
     * @param register the register address
     * @return the word value
     */
    int readWord(int register);
    
    /**
     * Writes a 16-bit word (big-endian).
     * 
     * @param register the register address
     * @param value the word value
     */
    void writeWord(int register, int value);
    
    /**
     * Closes the device.
     */
    void close();
}
