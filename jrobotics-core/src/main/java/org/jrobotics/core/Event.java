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

import java.time.Instant;

/**
 * Base interface for all events in the robotics system.
 * 
 * <p>
 * Events are used for asynchronous communication between components
 * through the {@link EventBus}.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface Event {

    /**
     * Gets the timestamp when this event was created.
     * 
     * @return the event timestamp
     */
    Instant getTimestamp();

    /**
     * Gets the source identifier of this event.
     * 
     * @return the source ID, or null if not applicable
     */
    String getSourceId();

    /**
     * Gets the event type name.
     * 
     * @return the event type name
     */
    default String getEventType() {
        return getClass().getSimpleName();
    }
}
