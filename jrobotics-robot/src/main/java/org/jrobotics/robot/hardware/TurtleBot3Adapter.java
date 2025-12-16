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
 * TurtleBot3 hardware adapter.
 * 
 * <p>Supports TurtleBot3 Burger and Waffle models. Uses serial communication
 * to the OpenCR board and optionally ROS2 for sensor data.</p>
 * 
 * <p>This is a mockup implementation that simulates the hardware interface.
 * Replace with real serial/ROS2 calls for actual hardware.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class TurtleBot3Adapter implements RobotHardwareAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(TurtleBot3Adapter.class);
    
    public enum Model { BURGER, WAFFLE, WAFFLE_PI }
    
    private final Model model;
    private final String serialPort;
    private boolean connected = false;
    private SimulatedHalProvider halProvider;
    
    // Simulated state
    private double x = 0, y = 0, theta = 0;
    private double vx = 0, vy = 0, omega = 0;
    private double battery = 100.0;
    private long lastUpdateTime;
    
    // TurtleBot3 specs
    private static final double WHEEL_RADIUS = 0.033; // 33mm
    private static final double WHEEL_BASE = 0.160;   // 160mm for Burger
    private static final double MAX_LINEAR_VEL = 0.22;  // m/s
    private static final double MAX_ANGULAR_VEL = 2.84; // rad/s
    
    /**
     * Creates a TurtleBot3 adapter.
     * 
     * @param model the TurtleBot3 model
     * @param serialPort the OpenCR serial port (e.g., "/dev/ttyACM0")
     */
    public TurtleBot3Adapter(Model model, String serialPort) {
        this.model = model;
        this.serialPort = serialPort;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * Creates a TurtleBot3 Burger adapter.
     */
    public static TurtleBot3Adapter burger(String serialPort) {
        return new TurtleBot3Adapter(Model.BURGER, serialPort);
    }
    
    /**
     * Creates a TurtleBot3 Waffle adapter.
     */
    public static TurtleBot3Adapter waffle(String serialPort) {
        return new TurtleBot3Adapter(Model.WAFFLE, serialPort);
    }
    
    @Override
    public String getName() {
        return "TurtleBot3 " + model.name();
    }
    
    @Override
    public String getDescription() {
        return "ROBOTIS TurtleBot3 " + model.name() + " differential drive robot";
    }
    
    @Override
    public boolean initialize() {
        logger.info("[{}] Initializing {} on port {}", System.currentTimeMillis(), getName(), serialPort);
        
        // In real implementation:
        // - Open serial port to OpenCR
        // - Send initialization commands
        // - Wait for ready response
        
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
        // Clamp to limits
        linearX = Math.max(-MAX_LINEAR_VEL, Math.min(MAX_LINEAR_VEL, linearX));
        angularZ = Math.max(-MAX_ANGULAR_VEL, Math.min(MAX_ANGULAR_VEL, angularZ));
        
        // TurtleBot3 is non-holonomic, ignore linearY
        this.vx = linearX;
        this.vy = 0;
        this.omega = angularZ;
        
        updateOdometry();
        
        // In real implementation:
        // - Convert to wheel velocities
        // - Send to OpenCR via serial
        
        logger.debug("[{}] TurtleBot3 velocity: linear={:.2f} m/s, angular={:.2f} rad/s", 
                System.currentTimeMillis(), linearX, angularZ);
    }
    
    @Override
    public void stop() {
        setVelocity(0, 0, 0);
        logger.info("[{}] TurtleBot3 stopped", System.currentTimeMillis());
    }
    
    @Override
    public void emergencyStop() {
        vx = vy = omega = 0;
        // In real implementation: send immediate stop command
        logger.warn("[{}] TurtleBot3 EMERGENCY STOP", System.currentTimeMillis());
    }
    
    @Override
    public double[] getOdometry() {
        updateOdometry();
        return new double[] { x, y, theta, vx, vy, omega };
    }
    
    @Override
    public double getBatteryLevel() {
        // Simulate slow discharge
        battery = Math.max(0, battery - 0.0001);
        return battery;
    }
    
    @Override
    public void disconnect() {
        stop();
        connected = false;
        if (halProvider != null) {
            halProvider.shutdown();
        }
        logger.info("[{}] TurtleBot3 disconnected", System.currentTimeMillis());
    }
    
    private void updateOdometry() {
        long now = System.currentTimeMillis();
        double dt = (now - lastUpdateTime) / 1000.0;
        lastUpdateTime = now;
        
        if (dt > 0 && dt < 1.0) {  // Sanity check
            // Simple differential drive kinematics
            double dx = vx * Math.cos(theta) * dt;
            double dy = vx * Math.sin(theta) * dt;
            double dtheta = omega * dt;
            
            x += dx;
            y += dy;
            theta += dtheta;
            
            // Normalize theta to [-pi, pi]
            while (theta > Math.PI) theta -= 2 * Math.PI;
            while (theta < -Math.PI) theta += 2 * Math.PI;
        }
    }
    
    /**
     * Gets the robot model.
     */
    public Model getModel() {
        return model;
    }
    
    /**
     * Gets max linear velocity.
     */
    public double getMaxLinearVelocity() {
        return MAX_LINEAR_VEL;
    }
    
    /**
     * Gets max angular velocity.
     */
    public double getMaxAngularVelocity() {
        return MAX_ANGULAR_VEL;
    }
}
