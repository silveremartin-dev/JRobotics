/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor.slam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OccupancyGridSLAMTest {

    private OccupancyGridSLAM slam;
    private static final double RESOLUTION = 0.1; // 10cm per cell
    private static final int SIZE = 100; // 10x10 meters

    @BeforeEach
    void setUp() {
        slam = new OccupancyGridSLAM(SIZE, SIZE, RESOLUTION);
    }

    @Test
    void testInitialization() {
        assertEquals(SIZE, slam.getWidth());
        assertEquals(SIZE, slam.getHeight());
        assertEquals(RESOLUTION, slam.getResolution());

        // Initial state should be unknown (0.5 probability)
        assertEquals(0.5, slam.getOccupancy(50, 50));
    }

    @Test
    void testPoseUpdate() {
        slam.updatePose(1.0, 0.0, 0.0);
        double[] pose = slam.getRobotPose();
        assertEquals(1.0, pose[0], 0.001);
        assertEquals(0.0, pose[1], 0.001);
        assertEquals(0.0, pose[2], 0.001);

        slam.updatePose(0.0, 1.0, Math.PI / 2);
        pose = slam.getRobotPose();
        assertEquals(1.0, pose[0], 0.001);
        assertEquals(1.0, pose[1], 0.001);
        assertEquals(Math.PI / 2, pose[2], 0.001);
    }

    @Test
    void testIntegrateScan() {
        slam.setPose(0, 0, 0);
        double[] ranges = { 2.0 }; // 2 meters ahead
        double[] angles = { 0.0 }; // straight ahead

        // Integrate multiple times to ensure thresholds are met
        for (int i = 0; i < 5; i++) {
            slam.integrateScan(ranges, angles, 10.0);
        }

        // Check cell at 2.0 meters (occupied)
        assertTrue(slam.getOccupancyWorld(2.0, 0.0) > 0.5, "Target should be occupied");
        // We can't guarantee > 0.7 without calculation, but it should increase.

        // Check cell at 1.0 meters (free)
        assertTrue(slam.getOccupancyWorld(1.0, 0.0) < 0.5, "Path should be free");
    }

    @Test
    void testMapExport() {
        slam.setPose(0, 0, 0);
        // Integrate multiple times to ensure log-odds accumulate beyond threshold
        for (int i = 0; i < 10; i++) {
            slam.integrateScan(new double[] { 1.0 }, new double[] { 0.0 }, 10.0);
        }

        byte[][] map = slam.exportMap();
        assertNotNull(map);
        assertEquals(SIZE, map.length);
        assertEquals(SIZE, map[0].length);

        // Check values
        int center = SIZE / 2;
        int hit = center + 10; // 1m / 0.1 = 10 cells

        // Verify center+5 (0.5m) is Free (128)
        // Verify center+10 (1.0m) is Occupied (255)

        assertEquals((byte) 255, map[hit][center], "Cell at 1.0m should be occupied");
        assertEquals((byte) 128, map[center + 5][center], "Cell at 0.5m should be free");
    }
}
