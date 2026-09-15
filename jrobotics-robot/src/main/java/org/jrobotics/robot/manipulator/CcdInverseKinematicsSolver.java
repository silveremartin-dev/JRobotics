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

import java.util.ArrayList;
import java.util.List;

/**
 * Inverse Kinematics solver using Cyclic Coordinate Descent (CCD).
 * 
 * <p>
 * CCD is an iterative method that minimizes position error by adjusting one
 * joint at a time.
 * It is robust, easy to implement, and handles joint constraints well.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.5.0
 */
public class CcdInverseKinematicsSolver implements InverseKinematicsSolver {

    // private static final Logger logger =
    // LoggerFactory.getLogger(CcdInverseKinematicsSolver.class);

    private final List<Double> linkLengths;
    private final int maxIterations;
    private final double tolerance;

    /**
     * Creates a CCD IK solver.
     * 
     * @param linkLengths   list of link lengths from base to end-effector
     * @param maxIterations maximum number of iterations
     * @param tolerance     distance tolerance for convergence
     */
    public CcdInverseKinematicsSolver(List<Double> linkLengths, int maxIterations, double tolerance) {
        this.linkLengths = linkLengths != null ? new ArrayList<>(linkLengths) : new ArrayList<>();
        this.maxIterations = maxIterations;
        this.tolerance = tolerance;
    }

    /**
     * Creates a default CCD solver with 100 iterations and 0.01 tolerance.
     */
    public CcdInverseKinematicsSolver(List<Double> linkLengths) {
        this(linkLengths, 100, 0.001);
    }

    @Override
    public double[] solve(double targetX, double targetY, double targetZ,
            double targetRoll, double targetPitch, double targetYaw) {

        int numJoints = linkLengths.size();
        if (numJoints == 0) {
            return new double[0];
        }
        double[] angles = new double[numJoints]; // Initial angles 0

        // Forward Kinematics to find current end-effector position
        Vector3 endEffector = forwardKinematics(angles);
        Vector3 target = new Vector3(targetX, targetY, targetZ);

        for (int iter = 0; iter < maxIterations; iter++) {
            if (endEffector.distanceTo(target) < tolerance) {
                return angles;
            }

            // Iterate through joints from end-effector to base
            for (int i = numJoints - 1; i >= 0; i--) {
                // Find pivot position of current joint
                Vector3 pivot = forwardKinematics(angles, i);
                Vector3 ee = forwardKinematics(angles, numJoints); // Current end tip

                // Vector from pivot to end effector
                Vector3 toEE = ee.subtract(pivot);
                // Vector from pivot to target
                Vector3 toTarget = target.subtract(pivot);

                // Calculate angle to rotate to align toEE with toTarget
                // In 2D (XY plane):
                double angleEE = Math.atan2(toEE.y(), toEE.x());
                double angleTarget = Math.atan2(toTarget.y(), toTarget.x());
                double diff = angleTarget - angleEE;

                // Update joint angle
                angles[i] += diff;

                // Normalize to -PI to PI
                while (angles[i] > Math.PI)
                    angles[i] -= 2 * Math.PI;
                while (angles[i] < -Math.PI)
                    angles[i] += 2 * Math.PI;

                // Re-calculate end effector for next joint
            }
            endEffector = forwardKinematics(angles);
        }

        // Return best effort
        return angles;
    }

    /**
     * Calculates position of joint index (or end effector if index == sizes).
     */
    private Vector3 forwardKinematics(double[] angles, int upToJoint) {
        double x = 0;
        double y = 0;
        double theta = 0; // Absolute angle

        for (int i = 0; i < upToJoint; i++) {
            theta += angles[i];
            x += linkLengths.get(i) * Math.cos(theta);
            y += linkLengths.get(i) * Math.sin(theta);
        }

        return new Vector3(x, y, 0);
    }

    private Vector3 forwardKinematics(double[] angles) {
        return forwardKinematics(angles, angles.length);
    }
}
