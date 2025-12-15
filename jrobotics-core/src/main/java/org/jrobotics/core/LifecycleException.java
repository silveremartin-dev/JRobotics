/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.core;

/**
 * Exception thrown when a lifecycle operation fails.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class LifecycleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final LifecycleState state;

    /**
     * Constructs a new lifecycle exception with the specified message.
     * 
     * @param message the detail message
     */
    public LifecycleException(String message) {
        super(message);
        this.state = null;
    }

    /**
     * Constructs a new lifecycle exception with the specified message and state.
     * 
     * @param message the detail message
     * @param state   the lifecycle state when the error occurred
     */
    public LifecycleException(String message, LifecycleState state) {
        super(message);
        this.state = state;
    }

    /**
     * Constructs a new lifecycle exception with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause   the cause of this exception
     */
    public LifecycleException(String message, Throwable cause) {
        super(message, cause);
        this.state = null;
    }

    /**
     * Constructs a new lifecycle exception with the specified message, cause, and
     * state.
     * 
     * @param message the detail message
     * @param cause   the cause of this exception
     * @param state   the lifecycle state when the error occurred
     */
    public LifecycleException(String message, Throwable cause, LifecycleState state) {
        super(message, cause);
        this.state = state;
    }

    /**
     * Gets the lifecycle state when the error occurred.
     * 
     * @return the lifecycle state, or null if not available
     */
    public LifecycleState getState() {
        return state;
    }
}
