/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor.navigation;

/**
 * Input data for navigation processors.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public record NavigationInput(
    /** Current robot pose */
    Pose2D currentPose,
    /** Goal pose to navigate to */
    Pose2D goalPose,
    /** Current linear velocity in m/s */
    double currentLinearVelocity,
    /** Current angular velocity in rad/s */
    double currentAngularVelocity
) {
    /**
     * Creates a navigation input with only poses.
     * 
     * @param current the current pose
     * @param goal the goal pose
     * @return the navigation input
     */
    public static NavigationInput of(Pose2D current, Pose2D goal) {
        return new NavigationInput(current, goal, 0, 0);
    }
}
