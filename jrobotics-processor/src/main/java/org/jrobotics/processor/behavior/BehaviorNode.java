/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor.behavior;

/**
 * Base interface for behavior tree nodes.
 * 
 * <p>Behavior trees are a powerful decision-making framework used in robotics
 * and game AI. They provide modular, reusable behavior components.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface BehaviorNode {
    
    /**
     * Behavior execution status.
     */
    enum Status {
        /** Behavior succeeded */
        SUCCESS,
        /** Behavior failed */
        FAILURE,
        /** Behavior is still running */
        RUNNING
    }
    
    /**
     * Gets the node name.
     * 
     * @return the name
     */
    String getName();
    
    /**
     * Executes the behavior.
     * 
     * @param context the execution context
     * @return the execution status
     */
    Status execute(BehaviorContext context);
    
    /**
     * Resets the node state.
     */
    void reset();
}
