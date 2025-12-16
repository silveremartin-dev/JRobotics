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

import org.jrobotics.core.Capability;
import org.jrobotics.core.math.DenavitHartenberg;
import org.jrobotics.robot.AbstractRobot;
import org.jrobotics.simulation.Vector3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for robotic arms (manipulators).
 * 
 * <p>
 * Uses Denavit-Hartenberg parameters for kinematics.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.4.0
 */
public abstract class RoboticArm extends AbstractRobot {

    private static final Logger logger = LoggerFactory.getLogger(RoboticArm.class);

    private final List<DenavitHartenberg> links = new ArrayList<>();
    private InverseKinematicsSolver ikSolver;

    private double[] currentJointAngles; // Radians

    /**
     * Creates a new robotic arm.
     * 
     * @param id   the unique identifier
     * @param name the human-readable name
     */
    protected RoboticArm(String id, String name) {
        super(id, name);
        addCapability(Capability.ARM);
    }

    /**
     * Adds a link to the kinematic chain.
     * 
     * @param dh the Denavit-Hartenberg parameters for the link
     */
    protected void addLink(DenavitHartenberg dh) {
        links.add(dh);
        currentJointAngles = new double[links.size()];
    }

    /**
     * Sets the IK solver.
     * 
     * @param solver the solver instance
     */
    public void setIkSolver(InverseKinematicsSolver solver) {
        this.ikSolver = solver;
    }

    /**
     * Moves the end-effector to the target position.
     * 
     * @param target the target position in cartesian space
     * @return true if reachable and solution found
     */
    public boolean moveTo(Vector3 target) {
        if (ikSolver == null) {
            logger.error("No IK solver configured for arm {}", getName());
            return false;
        }

        double[] solution = ikSolver.solvePosition(target);
        if (solution != null && solution.length == links.size()) {
            setJointAngles(solution);
            return true;
        } else {
            logger.warn("Target reachable solution not found for {}", target);
            return false;
        }
    }

    /**
     * Sets the joint angles directly.
     * 
     * @param angles array of angles in radians
     */
    public void setJointAngles(double[] angles) {
        if (angles.length != links.size()) {
            throw new IllegalArgumentException("Angle count mismatch: expected " + links.size());
        }
        System.arraycopy(angles, 0, currentJointAngles, 0, angles.length);
        // In a real implementation, this would command actuators
        logger.debug("Joint angles set: {}", angles);
    }

    /**
     * Gets the kinematic chain.
     * 
     * @return unmodifiable list of links
     */
    public List<DenavitHartenberg> getLinks() {
        return Collections.unmodifiableList(links);
    }

    /**
     * Gets current joint angles.
     * 
     * @return array of angles in radians
     */
    public double[] getJointAngles() {
        return currentJointAngles.clone();
    }
}
