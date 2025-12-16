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
 * Abstraction for GPIO (General Purpose Input/Output) pins.
 * 
 * <p>Provides a platform-independent interface for digital I/O operations.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface GpioPin {
    
    /**
     * GPIO pin modes.
     */
    enum Mode {
        /** Input mode */
        INPUT,
        /** Output mode */
        OUTPUT,
        /** PWM output mode */
        PWM,
        /** Analog input mode */
        ANALOG_INPUT,
        /** Analog output mode */
        ANALOG_OUTPUT
    }
    
    /**
     * Digital states.
     */
    enum State {
        LOW, HIGH
    }
    
    /**
     * Gets the pin number.
     * 
     * @return the pin number
     */
    int getPinNumber();
    
    /**
     * Gets the current mode.
     * 
     * @return the pin mode
     */
    Mode getMode();
    
    /**
     * Sets the pin mode.
     * 
     * @param mode the mode
     */
    void setMode(Mode mode);
    
    /**
     * Reads the digital state.
     * 
     * @return the state
     */
    State read();
    
    /**
     * Writes a digital state.
     * 
     * @param state the state
     */
    void write(State state);
    
    /**
     * Reads the analog value (0.0-1.0).
     * 
     * @return the analog value
     */
    double readAnalog();
    
    /**
     * Writes an analog value (0.0-1.0).
     * 
     * @param value the value
     */
    void writeAnalog(double value);
    
    /**
     * Sets the PWM duty cycle (0.0-1.0).
     * 
     * @param dutyCycle the duty cycle
     */
    void setPwmDutyCycle(double dutyCycle);
    
    /**
     * Sets the PWM frequency in Hz.
     * 
     * @param frequency the frequency
     */
    void setPwmFrequency(int frequency);
    
    /**
     * Checks if this is a simulated pin.
     * 
     * @return true if simulated
     */
    boolean isSimulated();
}
