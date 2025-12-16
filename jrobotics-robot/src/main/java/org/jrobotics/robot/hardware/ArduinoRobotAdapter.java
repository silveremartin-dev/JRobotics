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
 * Arduino-based robot hardware adapter.
 * 
 * <p>Generic adapter for Arduino-controlled robots using serial protocol.
 * Supports common motor driver configurations (L298N, TB6612, etc.).</p>
 * 
 * <p>Protocol format: "CMD:VAL1:VAL2\n"</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class ArduinoRobotAdapter implements RobotHardwareAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(ArduinoRobotAdapter.class);
    
    public enum MotorDriver { L298N, TB6612, BTS7960, CUSTOM }
    
    private final String serialPort;
    private final int baudRate;
    private final MotorDriver motorDriver;
    private boolean connected = false;
    private SimulatedHalProvider halProvider;
    
    // Simulated state
    private double leftSpeed = 0, rightSpeed = 0;
    private double x = 0, y = 0, theta = 0;
    private long lastUpdateTime;
    
    // Robot specs (configurable)
    private double wheelRadius = 0.034;  // 34mm default
    private double wheelBase = 0.15;     // 150mm default
    private double maxRpm = 200;
    
    /**
     * Creates an Arduino robot adapter.
     * 
     * @param serialPort the serial port (e.g., "COM3" or "/dev/ttyUSB0")
     * @param baudRate the baud rate (typically 115200)
     * @param motorDriver the motor driver type
     */
    public ArduinoRobotAdapter(String serialPort, int baudRate, MotorDriver motorDriver) {
        this.serialPort = serialPort;
        this.baudRate = baudRate;
        this.motorDriver = motorDriver;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * Creates an Arduino robot with L298N driver.
     */
    public static ArduinoRobotAdapter withL298N(String serialPort) {
        return new ArduinoRobotAdapter(serialPort, 115200, MotorDriver.L298N);
    }
    
    @Override
    public String getName() {
        return "Arduino Robot (" + motorDriver + ")";
    }
    
    @Override
    public String getDescription() {
        return "Arduino-based robot with " + motorDriver + " motor driver on " + serialPort;
    }
    
    @Override
    public boolean initialize() {
        logger.info("[{}] Initializing {} on {} at {} baud", 
                System.currentTimeMillis(), getName(), serialPort, baudRate);
        
        // In real implementation:
        // - Open serial port
        // - Send handshake: "INIT\n"
        // - Wait for "READY\n" response
        
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
        // Convert to differential drive wheel speeds
        double vLeft = linearX - angularZ * wheelBase / 2;
        double vRight = linearX + angularZ * wheelBase / 2;
        
        // Convert to motor PWM (-255 to 255)
        double maxWheelSpeed = maxRpm * 2 * Math.PI * wheelRadius / 60;
        int leftPwm = (int) Math.max(-255, Math.min(255, vLeft / maxWheelSpeed * 255));
        int rightPwm = (int) Math.max(-255, Math.min(255, vRight / maxWheelSpeed * 255));
        
        this.leftSpeed = vLeft;
        this.rightSpeed = vRight;
        
        updateOdometry();
        
        // In real implementation:
        // serialPort.write("MOT:" + leftPwm + ":" + rightPwm + "\n");
        
        logger.debug("[{}] Arduino motors: L={}, R={}", System.currentTimeMillis(), leftPwm, rightPwm);
    }
    
    @Override
    public void stop() {
        leftSpeed = rightSpeed = 0;
        // In real implementation: serialPort.write("STOP\n");
        logger.info("[{}] Arduino robot stopped", System.currentTimeMillis());
    }
    
    @Override
    public void emergencyStop() {
        leftSpeed = rightSpeed = 0;
        // In real implementation: serialPort.write("ESTOP\n");
        logger.warn("[{}] Arduino robot EMERGENCY STOP", System.currentTimeMillis());
    }
    
    @Override
    public double[] getOdometry() {
        updateOdometry();
        double vx = (leftSpeed + rightSpeed) / 2;
        double omega = (rightSpeed - leftSpeed) / wheelBase;
        return new double[] { x, y, theta, vx, 0, omega };
    }
    
    @Override
    public double getBatteryLevel() {
        // In real implementation: send "BAT\n" and parse response
        return 85.0;  // Simulated
    }
    
    @Override
    public void disconnect() {
        stop();
        connected = false;
        if (halProvider != null) {
            halProvider.shutdown();
        }
        logger.info("[{}] Arduino robot disconnected", System.currentTimeMillis());
    }
    
    private void updateOdometry() {
        long now = System.currentTimeMillis();
        double dt = (now - lastUpdateTime) / 1000.0;
        lastUpdateTime = now;
        
        if (dt > 0 && dt < 1.0) {
            double v = (leftSpeed + rightSpeed) / 2;
            double omega = (rightSpeed - leftSpeed) / wheelBase;
            
            double dx = v * Math.cos(theta) * dt;
            double dy = v * Math.sin(theta) * dt;
            double dtheta = omega * dt;
            
            x += dx;
            y += dy;
            theta += dtheta;
            
            while (theta > Math.PI) theta -= 2 * Math.PI;
            while (theta < -Math.PI) theta += 2 * Math.PI;
        }
    }
    
    /**
     * Sets wheel parameters.
     */
    public ArduinoRobotAdapter withWheels(double radius, double base) {
        this.wheelRadius = radius;
        this.wheelBase = base;
        return this;
    }
    
    /**
     * Sets max motor RPM.
     */
    public ArduinoRobotAdapter withMaxRpm(double rpm) {
        this.maxRpm = rpm;
        return this;
    }
    
    /**
     * Sends a raw command (for custom firmware).
     */
    public void sendCommand(String command) {
        // In real implementation: serialPort.write(command + "\n");
        logger.debug("[{}] Arduino command: {}", System.currentTimeMillis(), command);
    }
    
    /**
     * Sets a servo angle.
     */
    public void setServo(int servoId, int angle) {
        // In real implementation: serialPort.write("SRV:" + servoId + ":" + angle + "\n");
        logger.debug("[{}] Arduino servo {}: {} degrees", System.currentTimeMillis(), servoId, angle);
    }
}
