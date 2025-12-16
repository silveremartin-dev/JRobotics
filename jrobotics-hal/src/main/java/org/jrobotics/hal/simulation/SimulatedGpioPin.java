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

import org.jrobotics.hal.GpioPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulated GPIO pin for testing without hardware.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class SimulatedGpioPin implements GpioPin {
    
    private static final Logger logger = LoggerFactory.getLogger(SimulatedGpioPin.class);
    
    private final int pinNumber;
    private Mode mode = Mode.INPUT;
    private State state = State.LOW;
    private double analogValue = 0.0;
    private double pwmDutyCycle = 0.0;
    private int pwmFrequency = 1000;
    
    public SimulatedGpioPin(int pinNumber) {
        this.pinNumber = pinNumber;
    }
    
    @Override
    public int getPinNumber() {
        return pinNumber;
    }
    
    @Override
    public Mode getMode() {
        return mode;
    }
    
    @Override
    public void setMode(Mode mode) {
        this.mode = mode;
        logger.debug("GPIO {} mode set to {}", pinNumber, mode);
    }
    
    @Override
    public State read() {
        return state;
    }
    
    @Override
    public void write(State state) {
        if (mode != Mode.OUTPUT) {
            throw new IllegalStateException("Pin not in OUTPUT mode");
        }
        this.state = state;
        logger.trace("GPIO {} write: {}", pinNumber, state);
    }
    
    @Override
    public double readAnalog() {
        return analogValue;
    }
    
    @Override
    public void writeAnalog(double value) {
        this.analogValue = Math.max(0, Math.min(1, value));
        logger.trace("GPIO {} analog write: {}", pinNumber, analogValue);
    }
    
    @Override
    public void setPwmDutyCycle(double dutyCycle) {
        this.pwmDutyCycle = Math.max(0, Math.min(1, dutyCycle));
        logger.trace("GPIO {} PWM duty cycle: {}", pinNumber, pwmDutyCycle);
    }
    
    @Override
    public void setPwmFrequency(int frequency) {
        this.pwmFrequency = Math.max(1, frequency);
        logger.trace("GPIO {} PWM frequency: {} Hz", pinNumber, pwmFrequency);
    }
    
    @Override
    public boolean isSimulated() {
        return true;
    }
    
    /**
     * Sets the simulated input state.
     */
    public void setSimulatedState(State state) {
        this.state = state;
    }
    
    /**
     * Sets the simulated analog input value.
     */
    public void setSimulatedAnalogValue(double value) {
        this.analogValue = value;
    }
    
    /**
     * Gets the PWM duty cycle.
     */
    public double getPwmDutyCycle() {
        return pwmDutyCycle;
    }
    
    /**
     * Gets the PWM frequency.
     */
    public int getPwmFrequency() {
        return pwmFrequency;
    }
}
