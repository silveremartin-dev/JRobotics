/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.robot.manipulator;

import org.jrobotics.core.math.Vector3;

/**
 * Interface for inverse kinematics solvers.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.4.0
 */
public interface InverseKinematicsSolver {

    /**
     * Solves for joint angles to reach target pose.
     * 
     * @param targetX     target X coordinate
     * @param targetY     target Y coordinate
     * @param targetZ     target Z coordinate
     * @param targetRoll  target roll (X rotation)
     * @param targetPitch target pitch (Y rotation)
     * @param targetYaw   target yaw (Z rotation)
     * @return array of joint angles in radians, or null if unreachable
     */
    double[] solve(double targetX, double targetY, double targetZ,
            double targetRoll, double targetPitch, double targetYaw);

    /**
     * Solves for joint angles to reach target position (orientation ignored).
     * 
     * @param target the target position
     * @return array of joint angles in radians
     */
    default double[] solvePosition(Vector3 target) {
        return solve(target.x(), target.y(), target.z(), 0, 0, 0);
    }
}
