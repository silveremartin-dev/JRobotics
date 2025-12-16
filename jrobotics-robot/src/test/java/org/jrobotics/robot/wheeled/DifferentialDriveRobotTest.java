/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.robot.wheeled;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jrobotics.core.Capability;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DifferentialDriveRobot}.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class DifferentialDriveRobotTest {
    
    private DifferentialDriveRobot robot;
    
    @BeforeEach
    void setUp() throws Exception {
        robot = new DifferentialDriveRobot("robot-test", "Test Robot");
        robot.initialize();
        robot.start();
    }
    
    @Test
    void testRobotCreation() {
        assertEquals("robot-test", robot.getId());
        assertEquals("Test Robot", robot.getName());
        assertTrue(robot.hasCapability(Capability.WHEELED_LOCOMOTION));
    }
    
    @Test
    void testInitialPose() {
        assertEquals(0, robot.getX(), 1e-10);
        assertEquals(0, robot.getY(), 1e-10);
        assertEquals(0, robot.getTheta(), 1e-10);
    }
    
    @Test
    void testSetPose() {
        robot.setPose(5, 10, Math.PI / 2);
        
        assertEquals(5, robot.getX(), 1e-10);
        assertEquals(10, robot.getY(), 1e-10);
        assertEquals(Math.PI / 2, robot.getTheta(), 1e-10);
    }
    
    @Test
    void testForwardMotion() {
        robot.setVelocity(1.0, 0); // 1 m/s forward
        robot.updateOdometry(1.0);  // 1 second
        
        assertEquals(1.0, robot.getX(), 0.01);
        assertEquals(0, robot.getY(), 0.01);
    }
    
    @Test
    void testRotation() {
        robot.setVelocity(0, Math.PI / 2); // 90 deg/s rotation
        robot.updateOdometry(1.0);
        
        assertEquals(Math.PI / 2, robot.getTheta(), 0.01);
    }
    
    @Test
    void testCurvedPath() {
        // Move in a circle (constant linear + angular velocity)
        robot.setVelocity(1.0, 1.0);
        
        for (int i = 0; i < 100; i++) {
            robot.updateOdometry(0.01);
        }
        
        // Should have moved in a curved path
        assertTrue(robot.getX() > 0 || robot.getY() > 0);
    }
    
    @Test
    void testWheelSpeedsStraight() {
        robot.setWheelSpeeds(0.5, 0.5, 1.0);
        
        assertEquals(0.5, robot.getLinearVelocity(), 0.01);
        assertEquals(0, robot.getAngularVelocity(), 0.01);
    }
    
    @Test
    void testWheelSpeedsTurn() {
        robot.setWheelSpeeds(0, 0.5, 1.0);
        
        // Left wheel stopped, right wheel moving = turn left
        assertTrue(robot.getAngularVelocity() > 0);
    }
    
    @Test
    void testCalculateWheelSpeeds() {
        double[] speeds = robot.calculateWheelSpeeds(1.0, 0, 10.0);
        
        // Straight forward: both wheels equal
        assertEquals(speeds[0], speeds[1], 0.01);
    }
    
    @Test
    void testResetOdometry() {
        robot.setPose(5, 10, 1);
        robot.resetOdometry();
        
        assertEquals(0, robot.getX(), 1e-10);
        assertEquals(0, robot.getY(), 1e-10);
        assertEquals(0, robot.getTheta(), 1e-10);
    }
    
    @Test
    void testStopMethod() {
        robot.setVelocity(1.0, 0.5);
        robot.stop();
        
        assertEquals(0, robot.getLinearVelocity(), 1e-10);
        assertEquals(0, robot.getAngularVelocity(), 1e-10);
    }
    
    @Test
    void testAngleNormalization() {
        robot.setVelocity(0, 2 * Math.PI); // Full rotation/second
        robot.updateOdometry(1.5); // 1.5 full rotations = 3π → normalized to -π or π
        
        assertTrue(robot.getTheta() >= -Math.PI);
        assertTrue(robot.getTheta() <= Math.PI);
    }
    
    @Test
    void testWheelbaseAndRadius() {
        assertEquals(0.2, robot.getWheelBase(), 0.01);
        assertEquals(0.033, robot.getWheelRadius(), 0.01);
    }
}
