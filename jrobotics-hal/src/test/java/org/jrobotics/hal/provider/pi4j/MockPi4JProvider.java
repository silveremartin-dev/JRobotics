/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.hal.provider.pi4j;

import org.jrobotics.hal.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock implementation of HalProvider for testing purposes.
 * <p>
 * Simulates Pi4J hardware interactions in memory.
 * </p>
 */
public class MockPi4JProvider implements HalProvider {

    private static final Logger logger = LoggerFactory.getLogger(MockPi4JProvider.class);

    private final Map<Integer, MockGpioPin> pins = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "Mock Pi4J Provider";
    }

    @Override
    public Platform getPlatform() {
        return Platform.SIMULATION;
    }

    @Override
    public GpioPin getGpioPin(int pinNumber) {
        return pins.computeIfAbsent(pinNumber, MockGpioPin::new);
    }

    @Override
    public I2CBus getI2CBus(int busNumber) {
        logger.info("Mock I2C bus {} requested", busNumber);
        return null; // TODO: Implement MockI2CBus if needed
    }

    @Override
    public SpiBus getSpiBus(int busNumber) {
        logger.info("Mock SPI bus {} requested", busNumber);
        return null; // TODO: Implement MockSpiBus if needed
    }

    @Override
    public SerialPort getSerialPort(String portName) {
        logger.info("Mock Serial port {} requested", portName);
        return null; // TODO: Implement MockSerialPort if needed
    }

    @Override
    public String[] listSerialPorts() {
        return new String[] { "COM1", "COM2" };
    }

    @Override
    public boolean isRealHardware() {
        return false;
    }

    @Override
    public void shutdown() {
        logger.info("Mock Pi4J Provider shutting down");
        pins.clear();
    }

    /**
     * Mock GPIO Pin implementation.
     */
    public static class MockGpioPin implements GpioPin {
        private final int pinNumber;
        private Mode mode = Mode.INPUT;
        private State state = State.LOW;
        private double analogValue = 0.0;
        private double pwmDuty = 0.0;
        private int pwmFreq = 0;

        public MockGpioPin(int pinNumber) {
            this.pinNumber = pinNumber;
        }

        @Override
        public int getPinNumber() {
            return pinNumber;
        }

        @Override
        public void setMode(Mode mode) {
            this.mode = mode;
        }

        @Override
        public Mode getMode() {
            return mode;
        }

        @Override
        public void write(State state) {
            this.state = state;
            logger.debug("Pin {} written {}", pinNumber, state);
        }

        @Override
        public State read() {
            return state;
        }

        @Override
        public double readAnalog() {
            return analogValue;
        }

        // Helper for tests to set input value
        public void simulateAnalogInput(double value) {
            this.analogValue = value;
        }

        // Helper for tests to set digital input
        public void simulateDigitalInput(State state) {
            this.state = state;
        }

        @Override
        public void writeAnalog(double value) {
            this.analogValue = value;
            logger.debug("Pin {} analog write {}", pinNumber, value);
        }

        @Override
        public void setPwmDutyCycle(double value) {
            this.pwmDuty = value;
            logger.debug("Pin {} PWM duty {}", pinNumber, value);
        }

        @Override
        public void setPwmFrequency(int frequency) {
            this.pwmFreq = frequency;
            logger.debug("Pin {} PWM freq {}", pinNumber, frequency);
        }

        public double getPwmDutyCycle() {
            return pwmDuty;
        }

        public int getPwmFrequency() {
            return pwmFreq;
        }

        @Override
        public boolean isSimulated() {
            return true;
        }
    }
}
