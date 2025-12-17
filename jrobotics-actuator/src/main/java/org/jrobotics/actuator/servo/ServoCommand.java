/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.actuator.servo;

/**
 * Command for controlling servos.
 * 
 * @param angle Target angle in degrees
 * @param speed Movement speed (0.0-1.0, where 1.0 is maximum speed)
 * @param hold  If true, hold position after reaching target
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public record ServoCommand(
        double angle,
        double speed,
        boolean hold) {
    /**
     * Creates a servo command for position control.
     * 
     * @param angle the target angle in degrees
     * @return the servo command
     */
    public static ServoCommand position(double angle) {
        return new ServoCommand(angle, 1.0, true);
    }

    /**
     * Creates a servo command with speed control.
     * 
     * @param angle the target angle in degrees
     * @param speed the movement speed (0.0-1.0)
     * @return the servo command
     */
    public static ServoCommand position(double angle, double speed) {
        return new ServoCommand(angle, clamp(speed), true);
    }

    /**
     * Creates a release command (move to angle and release holding torque).
     * 
     * @param angle the target angle in degrees
     * @return the servo command
     */
    public static ServoCommand release(double angle) {
        return new ServoCommand(angle, 1.0, false);
    }

    /**
     * Creates a center position command (90 degrees).
     * 
     * @return the servo command
     */
    public static ServoCommand center() {
        return position(90.0);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
