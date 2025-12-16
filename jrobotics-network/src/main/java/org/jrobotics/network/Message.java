/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.network;

import java.util.Map;

/**
 * Base interface for network messages in the robotics system.
 * 
 * <p>Messages are used for communication between robots and control stations.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface Message {
    
    /**
     * Gets the message type.
     * 
     * @return the message type
     */
    MessageType getType();
    
    /**
     * Gets the source identifier (sender).
     * 
     * @return the source ID
     */
    String getSourceId();
    
    /**
     * Gets the target identifier (receiver).
     * 
     * @return the target ID, or null for broadcast
     */
    String getTargetId();
    
    /**
     * Gets the message timestamp.
     * 
     * @return the timestamp in milliseconds since epoch
     */
    long getTimestamp();
    
    /**
     * Gets the message sequence number.
     * 
     * @return the sequence number
     */
    long getSequenceNumber();
    
    /**
     * Gets the message payload data.
     * 
     * @return the payload as a map
     */
    Map<String, Object> getPayload();
    
    /**
     * Checks if this is a broadcast message.
     * 
     * @return true if broadcast
     */
    default boolean isBroadcast() {
        return getTargetId() == null || getTargetId().isEmpty();
    }
}
