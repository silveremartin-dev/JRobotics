/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.actuator.motor;

/**
 * Command for controlling motors.
 * 
 * @param speed      Motor speed from -1.0 (full reverse) to 1.0 (full forward)
 * @param durationMs Duration in milliseconds (0 = indefinite)
 * @param brake      If true, brake when stopping; if false, coast
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public record MotorCommand(
        double speed,
        long durationMs,
        boolean brake) {
    /**
     * Creates a motor command for continuous operation.
     * 
     * @param speed the speed from -1.0 to 1.0
     * @return the motor command
     */
    public static MotorCommand speed(double speed) {
        return new MotorCommand(clamp(speed), 0, true);
    }

    /**
     * Creates a motor command for timed operation.
     * 
     * @param speed      the speed from -1.0 to 1.0
     * @param durationMs the duration in milliseconds
     * @return the motor command
     */
    public static MotorCommand timed(double speed, long durationMs) {
        return new MotorCommand(clamp(speed), durationMs, true);
    }

    /**
     * Creates a stop command.
     * 
     * @param brake if true, brake; if false, coast
     * @return the stop command
     */
    public static MotorCommand stop(boolean brake) {
        return new MotorCommand(0, 0, brake);
    }

    /**
     * Creates a stop command with braking.
     * 
     * @return the stop command
     */
    public static MotorCommand stop() {
        return stop(true);
    }

    private static double clamp(double value) {
        return Math.max(-1.0, Math.min(1.0, value));
    }

    /**
     * Checks if this is a stop command.
     * 
     * @return true if speed is effectively zero
     */
    public boolean isStop() {
        return Math.abs(speed) < 0.001;
    }

    /**
     * Gets the direction: 1 for forward, -1 for reverse, 0 for stopped.
     * 
     * @return the direction
     */
    public int getDirection() {
        if (speed > 0.001)
            return 1;
        if (speed < -0.001)
            return -1;
        return 0;
    }
}
