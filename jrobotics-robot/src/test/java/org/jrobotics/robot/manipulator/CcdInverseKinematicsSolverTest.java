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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CCD Inverse Kinematics.
 */
class CcdInverseKinematicsSolverTest {

    @Test
    void testSolveReachSimple() {
        // 2-link arm with length 1.0 each
        List<Double> links = Arrays.asList(1.0, 1.0);
        CcdInverseKinematicsSolver solver = new CcdInverseKinematicsSolver(links);

        // Target: (2, 0) -> Fully extended
        double[] angles = solver.solve(1.99, 0, 0, 0, 0, 0);

        assertNotNull(angles);
        assertEquals(2, angles.length);

        // Check if it's roughly straight
        // Sum of angles should be near 0
        assertEquals(0.0, angles[0] + angles[1], 0.1, "Arm should be roughly straight");
    }

    @Test
    void testSolveReach90Degrees() {
        // 2-link arm
        List<Double> links = Arrays.asList(1.0, 1.0);
        CcdInverseKinematicsSolver solver = new CcdInverseKinematicsSolver(links);

        // Target: (0, 2) -> Pointing up
        double[] angles = solver.solve(0, 1.99, 0, 0, 0, 0);

        assertNotNull(angles);

        // Should align upwards
        double x = Math.cos(angles[0]) + Math.cos(angles[0] + angles[1]);
        double y = Math.sin(angles[0]) + Math.sin(angles[0] + angles[1]);

        assertEquals(0.0, x, 0.1);
        assertEquals(2.0, y, 0.1);
    }
}
