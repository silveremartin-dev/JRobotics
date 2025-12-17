/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.robot.hardware;

import org.jrobotics.hal.HalProvider;
import org.jrobotics.hal.simulation.SimulatedHalProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Raspberry Pi robot hardware adapter.
 * 
 * <p>
 * For robots with Raspberry Pi as the main controller, using HATs like:
 * Motor HAT, PiCAN, Sense HAT, etc.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class RaspberryPiRobotAdapter implements RobotHardwareAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RaspberryPiRobotAdapter.class);

    public enum Hat {
        MOTOR_HAT, ADAFRUIT_DC_STEPPER, WAVESHARE_MOTOR, NONE
    }

    private final Hat hat;
    private boolean connected = false;
    private SimulatedHalProvider halProvider;

    // Simulated state
    private double x = 0, y = 0, theta = 0;
    private double vx = 0, vy = 0, omega = 0;
    private long lastUpdateTime;

    // private double wheelRadius = 0.032;
    // private double wheelBase = 0.14;

    /**
     * Creates a Raspberry Pi robot adapter.
     * 
     * @param hat the motor HAT type
     */
    public RaspberryPiRobotAdapter(Hat hat) {
        this.hat = hat;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Creates adapter with Adafruit Motor HAT.
     */
    public static RaspberryPiRobotAdapter withMotorHat() {
        return new RaspberryPiRobotAdapter(Hat.MOTOR_HAT);
    }

    @Override
    public String getName() {
        return "Raspberry Pi Robot (" + hat + ")";
    }

    @Override
    public String getDescription() {
        return "Raspberry Pi controlled robot with " + hat + " motor driver";
    }

    @Override
    public boolean initialize() {
        logger.info("[{}] Initializing {} with {} HAT", System.currentTimeMillis(), getName(), hat);

        // In real implementation:
        // - Initialize I2C for motor HAT (address 0x60)
        // - Set PWM frequency (typically 1600Hz)
        // - Initialize motor channels

        halProvider = new SimulatedHalProvider();
        connected = true;
        lastUpdateTime = System.currentTimeMillis();

        logger.info("[{}] {} initialized successfully", System.currentTimeMillis(), getName());
        return true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public HalProvider getHalProvider() {
        return halProvider;
    }

    @Override
    public void setVelocity(double linearX, double linearY, double angularZ) {
        this.vx = linearX;
        this.vy = 0;
        this.omega = angularZ;

        updateOdometry();

        // In real implementation:
        // - Calculate wheel speeds
        // - Set motor PWM via I2C

        logger.debug("[{}] RPi robot velocity: linear={:.2f}, angular={:.2f}",
                System.currentTimeMillis(), linearX, angularZ);
    }

    @Override
    public void stop() {
        vx = vy = omega = 0;
        // In real implementation: set all motor channels to 0
        logger.info("[{}] RPi robot stopped", System.currentTimeMillis());
    }

    @Override
    public void emergencyStop() {
        vx = vy = omega = 0;
        logger.warn("[{}] RPi robot EMERGENCY STOP", System.currentTimeMillis());
    }

    @Override
    public double[] getOdometry() {
        updateOdometry();
        return new double[] { x, y, theta, vx, vy, omega };
    }

    @Override
    public double getBatteryLevel() {
        // In real implementation: read from ADC
        return 90.0;
    }

    @Override
    public void disconnect() {
        stop();
        connected = false;
        if (halProvider != null) {
            halProvider.shutdown();
        }
        logger.info("[{}] RPi robot disconnected", System.currentTimeMillis());
    }

    private void updateOdometry() {
        long now = System.currentTimeMillis();
        double dt = (now - lastUpdateTime) / 1000.0;
        lastUpdateTime = now;

        if (dt > 0 && dt < 1.0) {
            double dx = vx * Math.cos(theta) * dt;
            double dy = vx * Math.sin(theta) * dt;
            double dtheta = omega * dt;

            x += dx;
            y += dy;
            theta += dtheta;

            while (theta > Math.PI)
                theta -= 2 * Math.PI;
            while (theta < -Math.PI)
                theta += 2 * Math.PI;
        }
    }

    /**
     * Sets wheel parameters.
     */
    public RaspberryPiRobotAdapter withWheels(double radius, double base) {
        // this.wheelRadius = radius;
        // this.wheelBase = base;
        return this;
    }

    /**
     * Reads a GPIO pin.
     */
    public boolean readGpio(int pin) {
        // In real implementation: use pi4j or similar
        return halProvider.getSimulatedGpioPin(pin).read() == org.jrobotics.hal.GpioPin.State.HIGH;
    }

    /**
     * Writes a GPIO pin.
     */
    public void writeGpio(int pin, boolean high) {
        halProvider.getSimulatedGpioPin(pin).write(
                high ? org.jrobotics.hal.GpioPin.State.HIGH
                        : org.jrobotics.hal.GpioPin.State.LOW);
    }
}
