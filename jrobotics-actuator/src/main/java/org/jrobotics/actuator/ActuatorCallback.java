/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.actuator;

/**
 * Callback interface for asynchronous actuator command completion.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
@FunctionalInterface
public interface ActuatorCallback {
    
    /**
     * Called when an actuator command completes.
     * 
     * @param actuatorId the ID of the actuator
     * @param success true if command completed successfully
     * @param error the error message if failed, null otherwise
     */
    void onComplete(String actuatorId, boolean success, String error);
}
