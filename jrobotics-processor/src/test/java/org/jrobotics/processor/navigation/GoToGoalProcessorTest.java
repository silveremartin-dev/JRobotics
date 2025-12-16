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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GoToGoalProcessor}.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class GoToGoalProcessorTest {
    
    private GoToGoalProcessor processor;
    
    @BeforeEach
    void setUp() throws Exception {
        processor = new GoToGoalProcessor("nav-test", "Test Navigation");
        processor.initialize();
        processor.start();
    }
    
    @Test
    void testProcessorCreation() {
        assertEquals("nav-test", processor.getId());
        assertEquals("Test Navigation", processor.getName());
    }
    
    @Test
    void testGoForward() {
        // Robot at origin, goal directly ahead
        Pose2D current = Pose2D.origin();
        Pose2D goal = new Pose2D(5, 0, 0);
        NavigationInput input = NavigationInput.of(current, goal);
        
        VelocityCommand cmd = processor.process(input);
        
        // Should have positive linear velocity, near-zero angular
        assertTrue(cmd.linear() > 0, "Should move forward");
        assertTrue(Math.abs(cmd.angular()) < 0.1, "Should have minimal turning");
    }
    
    @Test
    void testTurnLeft() {
        // Robot at origin facing +X, goal to the left (+Y)
        Pose2D current = new Pose2D(0, 0, 0);
        Pose2D goal = new Pose2D(0, 5, 0);
        NavigationInput input = NavigationInput.of(current, goal);
        
        VelocityCommand cmd = processor.process(input);
        
        // Should turn left (positive angular velocity)
        assertTrue(cmd.angular() > 0, "Should turn left");
    }
    
    @Test
    void testTurnRight() {
        // Robot at origin facing +X, goal to the right (-Y)
        Pose2D current = new Pose2D(0, 0, 0);
        Pose2D goal = new Pose2D(0, -5, 0);
        NavigationInput input = NavigationInput.of(current, goal);
        
        VelocityCommand cmd = processor.process(input);
        
        // Should turn right (negative angular velocity)
        assertTrue(cmd.angular() < 0, "Should turn right");
    }
    
    @Test
    void testStopAtGoal() {
        // Already at goal
        Pose2D current = new Pose2D(5, 5, 0);
        Pose2D goal = new Pose2D(5, 5, 0);
        NavigationInput input = NavigationInput.of(current, goal);
        
        VelocityCommand cmd = processor.process(input);
        
        assertTrue(cmd.isStopped(), "Should stop at goal");
    }
    
    @Test
    void testNearGoal() {
        // Very close to goal (within tolerance)
        processor.setConfiguration("goal_tolerance", 0.2);
        
        Pose2D current = new Pose2D(5.05, 5.05, 0);
        Pose2D goal = new Pose2D(5, 5, 0);
        NavigationInput input = NavigationInput.of(current, goal);
        
        VelocityCommand cmd = processor.process(input);
        
        assertTrue(cmd.isStopped(), "Should stop when near goal");
    }
    
    @Test
    void testConfidence() {
        // When facing goal directly, confidence should be high
        Pose2D current = new Pose2D(0, 0, 0);
        Pose2D goal = new Pose2D(5, 0, 0);
        NavigationInput input = NavigationInput.of(current, goal);
        
        processor.process(input);
        
        assertTrue(processor.getConfidence() > 0.9, "Should have high confidence when facing goal");
    }
    
    @Test
    void testAsyncProcessing() throws Exception {
        Pose2D current = Pose2D.origin();
        Pose2D goal = new Pose2D(5, 0, 0);
        NavigationInput input = NavigationInput.of(current, goal);
        
        VelocityCommand cmd = processor.processAsync(input).get();
        
        assertNotNull(cmd);
        assertTrue(cmd.linear() > 0);
    }
    
    @Test
    void testReset() {
        processor.setConfiguration("test_value", 123);
        processor.reset();
        
        assertNull(processor.getConfiguration().get("test_value"));
        assertEquals(1.0, processor.getConfidence());
    }
}
