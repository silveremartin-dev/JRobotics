/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.robot.humanoid;

import org.jrobotics.core.math.Vector3;

/**
 * Interface for humanoid locomotion engines.
 * 
 * <p>
 * Responsible for generating joint trajectories to maintain balance and move.
 * Implementations might use ZMP (Zero Moment Point), CPG (Central Pattern
 * Generators), or RL.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.5.0
 */
public interface WalkingEngine {

    /**
     * Commands to walk with specific velocity.
     * 
     * @param velocity x (forward), y (sideways), z (turn rate)
     */
    void walk(Vector3 velocity);

    /**
     * Stops walking.
     */
    void stop();

    /**
     * Returns true if currently balancing/walking.
     */
    boolean isWalking();
}
