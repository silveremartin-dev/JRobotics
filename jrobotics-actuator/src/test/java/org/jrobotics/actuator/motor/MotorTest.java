/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.actuator.motor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Motor}.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class MotorTest {
    
    private Motor motor;
    
    @BeforeEach
    void setUp() throws Exception {
        motor = new Motor("motor-test", "Test Motor");
        motor.initialize();
        motor.start();
    }
    
    @Test
    void testMotorCreation() {
        assertEquals("motor-test", motor.getId());
        assertEquals("Test Motor", motor.getName());
        assertTrue(motor.isSimulationMode());
    }
    
    @Test
    void testLifecycle() {
        assertTrue(motor.isRunning());
        assertFalse(motor.isActive());
    }
    
    @Test
    void testExecuteSpeed() throws Exception {
        motor.execute(MotorCommand.speed(0.5));
        
        assertEquals(0.5, motor.getTargetSpeed(), 0.01);
        assertEquals(0.5, motor.getCurrentValue(), 0.01);
    }
    
    @Test
    void testExecuteStop() throws Exception {
        motor.execute(MotorCommand.speed(0.8));
        motor.execute(MotorCommand.stop());
        
        assertEquals(0, motor.getTargetSpeed(), 0.01);
    }
    
    @Test
    void testEmergencyStop() throws Exception {
        motor.execute(MotorCommand.speed(0.8));
        motor.emergencyStop();
        
        assertTrue(motor.isEmergencyStopped());
        assertEquals(0, motor.getTargetSpeed(), 0.01);
    }
    
    @Test
    void testEmergencyStopPreventsExecution() {
        motor.emergencyStop();
        
        assertThrows(Exception.class, () -> {
            motor.execute(MotorCommand.speed(0.5));
        });
    }
    
    @Test
    void testResetEmergencyStop() throws Exception {
        motor.emergencyStop();
        motor.resetEmergencyStop();
        
        assertFalse(motor.isEmergencyStopped());
        
        // Should be able to execute now
        motor.execute(MotorCommand.speed(0.3));
        assertEquals(0.3, motor.getTargetSpeed(), 0.01);
    }
    
    @Test
    void testEncoderCount() {
        assertEquals(0, motor.getEncoderCount());
        assertEquals(0, motor.getDistanceTraveled(), 0.01);
    }
    
    @Test
    void testResetEncoder() throws Exception {
        motor.execute(MotorCommand.timed(0.5, 1000));
        motor.resetEncoder();
        
        assertEquals(0, motor.getEncoderCount());
    }
    
    @Test
    void testFeedback() throws Exception {
        motor.execute(MotorCommand.speed(0.5));
        
        assertTrue(motor.hasFeedback());
        
        Object feedback = motor.getFeedback();
        assertNotNull(feedback);
        assertTrue(feedback instanceof Motor.MotorFeedback);
    }
    
    @Test
    void testMinMaxValues() {
        assertEquals(-1.0, motor.getMinValue(), 0.01);
        assertEquals(1.0, motor.getMaxValue(), 0.01);
    }
    
    @Test
    void testMotorCommand() {
        MotorCommand forward = MotorCommand.speed(0.7);
        assertEquals(0.7, forward.speed(), 0.01);
        assertFalse(forward.isStop());
        assertEquals(1, forward.getDirection());
        
        MotorCommand reverse = MotorCommand.speed(-0.5);
        assertEquals(-1, reverse.getDirection());
        
        MotorCommand stop = MotorCommand.stop();
        assertTrue(stop.isStop());
        assertEquals(0, stop.getDirection());
    }
}
