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

import org.jrobotics.robot.AbstractRobot;
import org.jrobotics.core.math.Vector3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for humanoid robots.
 * 
 * <p>
 * Represents a bipedal robot with arms, legs, and a head.
 * Manages balance and locomotion via a WalkingEngine.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.5.0
 */
public abstract class HumanoidRobot extends AbstractRobot {

    private static final Logger logger = LoggerFactory.getLogger(HumanoidRobot.class);

    protected WalkingEngine walkingEngine;

    // Kinematic Chains (stubs for now, would be Limb objects)
    // protected Limb leftLeg;
    // protected Limb rightLeg;
    // protected Limb leftArm;
    // protected Limb rightArm;

    public HumanoidRobot(String id) {
        super(id, "Humanoid-" + id);
    }

    /**
     * Sets the walking engine.
     * 
     * @param walkingEngine the gait generator / balance controller
     */
    public void setWalkingEngine(WalkingEngine walkingEngine) {
        this.walkingEngine = walkingEngine;
    }

    /**
     * Commands the robot to walk.
     * 
     * @param velocity Desired velocity (x, y, theta)
     */
    public void walk(Vector3 velocity) {
        if (walkingEngine != null) {
            walkingEngine.walk(velocity);
        } else {
            logger.warn("No WalkingEngine assigned to humanoid {}", getId());
        }
    }

    /**
     * Stops the robot.
     */
    public void stopWalking() {
        if (walkingEngine != null) {
            walkingEngine.stop();
        }
    }
}
