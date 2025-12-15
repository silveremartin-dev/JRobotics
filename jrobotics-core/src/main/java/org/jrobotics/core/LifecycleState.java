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
 * Enumeration of lifecycle states for components.
 * 
 * <p>
 * State transitions follow this pattern:
 * </p>
 * 
 * <pre>
 * CREATED → INITIALIZING → INITIALIZED → STARTING → RUNNING
 *                                              ↓
 *                                          PAUSING → PAUSED → RESUMING → RUNNING
 *                                              ↓
 *                                          STOPPING → STOPPED → DESTROYING → DESTROYED
 * </pre>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public enum LifecycleState {

    /**
     * Component has been created but not yet initialized.
     */
    CREATED,

    /**
     * Component is currently initializing.
     */
    INITIALIZING,

    /**
     * Component has been initialized and is ready to start.
     */
    INITIALIZED,

    /**
     * Component is currently starting.
     */
    STARTING,

    /**
     * Component is running and fully operational.
     */
    RUNNING,

    /**
     * Component is currently pausing.
     */
    PAUSING,

    /**
     * Component is paused and can be resumed.
     */
    PAUSED,

    /**
     * Component is currently resuming from pause.
     */
    RESUMING,

    /**
     * Component is currently stopping.
     */
    STOPPING,

    /**
     * Component has stopped but resources may still be held.
     */
    STOPPED,

    /**
     * Component is being destroyed.
     */
    DESTROYING,

    /**
     * Component has been destroyed and cannot be reused.
     */
    DESTROYED,

    /**
     * Component encountered an error and is in a failed state.
     */
    ERROR;

    /**
     * Checks if this state represents an active (running or paused) state.
     * 
     * @return true if active
     */
    public boolean isActive() {
        return this == RUNNING || this == PAUSED;
    }

    /**
     * Checks if this state is a transitional state.
     * 
     * @return true if transitioning
     */
    public boolean isTransitioning() {
        return this == INITIALIZING || this == STARTING ||
                this == PAUSING || this == RESUMING ||
                this == STOPPING || this == DESTROYING;
    }

    /**
     * Checks if this state allows starting.
     * 
     * @return true if can start
     */
    public boolean canStart() {
        return this == INITIALIZED || this == STOPPED;
    }
}
