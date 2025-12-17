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
 * iRobot Create3 hardware adapter.
 * 
 * <p>
 * Supports the iRobot Create3 educational robot platform.
 * Uses ROS2 interface for communication.
 * </p>
 * 
 * <p>
 * This is a mockup implementation. Replace with real ROS2 calls
 * for actual hardware.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class Create3Adapter implements RobotHardwareAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Create3Adapter.class);

    private final String namespace;
    private boolean connected = false;
    private SimulatedHalProvider halProvider;

    // Simulated state
    private double x = 0, y = 0, theta = 0;
    private double vx = 0, vy = 0, omega = 0;
    private double battery = 100.0;
    private long lastUpdateTime;

    // Create3 specs
    private static final double MAX_LINEAR_VEL = 0.306; // m/s
    private static final double MAX_ANGULAR_VEL = 2.64; // rad/s
    // private static final double WHEEL_BASE = 0.235; // 235mm

    /**
     * Creates a Create3 adapter.
     * 
     * @param namespace the ROS2 namespace (e.g., "/create3")
     */
    public Create3Adapter(String namespace) {
        this.namespace = namespace;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Creates a Create3 adapter with default namespace.
     */
    public Create3Adapter() {
        this("/create3");
    }

    @Override
    public String getName() {
        return "iRobot Create3";
    }

    @Override
    public String getDescription() {
        return "iRobot Create3 educational robot with ROS2 interface";
    }

    @Override
    public boolean initialize() {
        logger.info("[{}] Initializing {} with namespace {}",
                System.currentTimeMillis(), getName(), namespace);

        // In real implementation:
        // - Initialize ROS2 node
        // - Create publishers/subscribers
        // - Wait for Create3 to respond

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

        this.vx = linearX;
        this.vy = 0; // Non-holonomic
        this.omega = angularZ;

        updateOdometry();

        // In real implementation:
        // - Publish to /cmd_vel topic

        logger.debug("[{}] Create3 velocity: linear={:.2f} m/s, angular={:.2f} rad/s",
                System.currentTimeMillis(), linearX, angularZ);
    }

    @Override
    public void stop() {
        setVelocity(0, 0, 0);
        logger.info("[{}] Create3 stopped", System.currentTimeMillis());
    }

    @Override
    public void emergencyStop() {
        vx = vy = omega = 0;
        // In real implementation: call /stop_motors service
        logger.warn("[{}] Create3 EMERGENCY STOP", System.currentTimeMillis());
    }

    @Override
    public double[] getOdometry() {
        updateOdometry();
        return new double[] { x, y, theta, vx, vy, omega };
    }

    @Override
    public double getBatteryLevel() {
        // Simulate slow discharge
        battery = Math.max(0, battery - 0.00005);
        return battery;
    }

    @Override
    public void disconnect() {
        stop();
        connected = false;
        if (halProvider != null) {
            halProvider.shutdown();
        }
        logger.info("[{}] Create3 disconnected", System.currentTimeMillis());
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
     * Gets the ROS2 namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Docks the robot (mockup).
     */
    public void dock() {
        logger.info("[{}] Create3 docking...", System.currentTimeMillis());
        // In real implementation: call /dock action
    }

    /**
     * Undocks the robot (mockup).
     */
    public void undock() {
        logger.info("[{}] Create3 undocking...", System.currentTimeMillis());
        // In real implementation: call /undock action
    }

    /**
     * Plays a sound.
     */
    public void playSound(int soundId) {
        logger.debug("[{}] Create3 playing sound {}", System.currentTimeMillis(), soundId);
        // In real implementation: publish to /audio topic
    }
}
