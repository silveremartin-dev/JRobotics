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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for A* path planner.
 */
class AStarPathPlannerTest {
    
    private AStarPathPlanner planner;
    
    @BeforeEach
    void setUp() {
        planner = new AStarPathPlanner(20, 20, true);
    }
    
    @Test
    void testSimplePath() {
        List<int[]> path = planner.findPath(0, 0, 5, 5);
        
        assertFalse(path.isEmpty(), "Should find a path");
        assertEquals(0, path.get(0)[0], "Should start at (0,0)");
        assertEquals(0, path.get(0)[1]);
        assertEquals(5, path.get(path.size()-1)[0], "Should end at (5,5)");
        assertEquals(5, path.get(path.size()-1)[1]);
    }
    
    @Test
    void testPathWithObstacles() {
        // Create a wall
        for (int y = 0; y < 15; y++) {
            planner.setObstacle(10, y, true);
        }
        
        List<int[]> path = planner.findPath(5, 5, 15, 5);
        
        assertFalse(path.isEmpty(), "Should find path around obstacle");
        
        // Verify path doesn't go through obstacles
        for (int[] cell : path) {
            if (cell[0] == 10) {
                assertTrue(cell[1] >= 15, "Should not cross the wall");
            }
        }
    }
    
    @Test
    void testNoPath() {
        // Completely surround start with obstacles
        planner.setObstacle(0, 1, true);
        planner.setObstacle(1, 0, true);
        planner.setObstacle(1, 1, true);
        
        List<int[]> path = planner.findPath(0, 0, 10, 10);
        
        assertTrue(path.isEmpty(), "Should return empty when no path exists");
    }
    
    @Test
    void testSameStartAndGoal() {
        List<int[]> path = planner.findPath(5, 5, 5, 5);
        
        assertEquals(1, path.size(), "Path should contain just the start/goal");
    }
    
    @Test
    void testInvalidCoordinates() {
        List<int[]> path = planner.findPath(-1, 0, 25, 25);
        
        assertTrue(path.isEmpty(), "Should return empty for invalid coords");
    }
    
    @Test
    void testDiagonalMovement() {
        AStarPathPlanner diagPlanner = new AStarPathPlanner(10, 10, true);
        List<int[]> diagPath = diagPlanner.findPath(0, 0, 5, 5);
        
        AStarPathPlanner cardPlanner = new AStarPathPlanner(10, 10, false);
        List<int[]> cardPath = cardPlanner.findPath(0, 0, 5, 5);
        
        assertTrue(diagPath.size() <= cardPath.size(), 
            "Diagonal path should be shorter or equal");
    }
}
