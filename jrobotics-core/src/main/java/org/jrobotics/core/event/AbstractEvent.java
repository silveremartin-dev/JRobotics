/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.core.event;

import org.jrobotics.core.Event;

import java.time.Instant;
import java.util.Objects;

/**
 * Base class for all events in the robotics system.
 * 
 * <p>
 * Provides common fields like timestamp and source ID.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public abstract class AbstractEvent implements Event {

    private final Instant timestamp;
    private final String sourceId;

    /**
     * Constructs a new event with the current timestamp.
     * 
     * @param sourceId the source identifier
     */
    protected AbstractEvent(String sourceId) {
        this.timestamp = Instant.now();
        this.sourceId = sourceId;
    }

    /**
     * Constructs a new event with a specific timestamp.
     * 
     * @param timestamp the event timestamp
     * @param sourceId  the source identifier
     */
    protected AbstractEvent(Instant timestamp, String sourceId) {
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.sourceId = sourceId;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String getSourceId() {
        return sourceId;
    }

    @Override
    public String toString() {
        return String.format("%s[timestamp=%s, sourceId=%s]",
                getEventType(), timestamp, sourceId);
    }
}
