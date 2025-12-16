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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of the {@link Message} interface.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class NetworkMessage implements Message {
    
    private static final AtomicLong sequenceCounter = new AtomicLong(0);
    
    private final MessageType type;
    private final String sourceId;
    private final String targetId;
    private final long timestamp;
    private final long sequenceNumber;
    private final Map<String, Object> payload;
    
    /**
     * Constructs a new network message.
     * 
     * @param type the message type
     * @param sourceId the source ID
     * @param targetId the target ID (null for broadcast)
     * @param payload the message payload
     */
    public NetworkMessage(MessageType type, String sourceId, String targetId, Map<String, Object> payload) {
        this.type = type;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.timestamp = System.currentTimeMillis();
        this.sequenceNumber = sequenceCounter.incrementAndGet();
        this.payload = payload != null ? new ConcurrentHashMap<>(payload) : new ConcurrentHashMap<>();
    }
    
    /**
     * Creates a broadcast message.
     * 
     * @param type the message type
     * @param sourceId the source ID
     * @return the message
     */
    public static NetworkMessage broadcast(MessageType type, String sourceId) {
        return new NetworkMessage(type, sourceId, null, null);
    }
    
    /**
     * Creates a targeted message.
     * 
     * @param type the message type
     * @param sourceId the source ID
     * @param targetId the target ID
     * @return the message
     */
    public static NetworkMessage targeted(MessageType type, String sourceId, String targetId) {
        return new NetworkMessage(type, sourceId, targetId, null);
    }
    
    /**
     * Creates a message with payload.
     * 
     * @param type the message type
     * @param sourceId the source ID
     * @param targetId the target ID
     * @param payload the payload
     * @return the message
     */
    public static NetworkMessage create(MessageType type, String sourceId, String targetId, Map<String, Object> payload) {
        return new NetworkMessage(type, sourceId, targetId, payload);
    }
    
    /**
     * Creates a message without payload.
     * 
     * @param type the message type
     * @param sourceId the source ID
     * @param targetId the target ID
     * @return the message
     */
    public static NetworkMessage create(MessageType type, String sourceId, String targetId) {
        return new NetworkMessage(type, sourceId, targetId, null);
    }
    
    /**
     * Creates a broadcast message with payload.
     * 
     * @param type the message type
     * @param sourceId the source ID
     * @param payload the payload
     * @return the message
     */
    public static NetworkMessage broadcast(MessageType type, String sourceId, Map<String, Object> payload) {
        return new NetworkMessage(type, sourceId, null, payload);
    }
    
    /**
     * Creates a command message.
     * 
     * @param sourceId the source ID
     * @param targetId the target robot ID
     * @param command the command name
     * @param params the command parameters
     * @return the command message
     */
    public static NetworkMessage command(String sourceId, String targetId, String command, Map<String, Object> params) {
        Map<String, Object> payload = new ConcurrentHashMap<>(params);
        payload.put("command", command);
        return new NetworkMessage(MessageType.COMMAND, sourceId, targetId, payload);
    }
    
    /**
     * Creates an emergency stop message.
     * 
     * @param sourceId the source ID
     * @return the emergency stop broadcast message
     */
    public static NetworkMessage emergencyStop(String sourceId) {
        return broadcast(MessageType.EMERGENCY_STOP, sourceId);
    }
    
    @Override
    public MessageType getType() {
        return type;
    }
    
    @Override
    public String getSourceId() {
        return sourceId;
    }
    
    @Override
    public String getTargetId() {
        return targetId;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public long getSequenceNumber() {
        return sequenceNumber;
    }
    
    @Override
    public Map<String, Object> getPayload() {
        return new ConcurrentHashMap<>(payload);
    }
    
    /**
     * Gets a payload value.
     * 
     * @param key the key
     * @param <T> the value type
     * @return the value, or null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) payload.get(key);
    }
    
    /**
     * Sets a payload value.
     * 
     * @param key the key
     * @param value the value
     * @return this message for chaining
     */
    public NetworkMessage set(String key, Object value) {
        payload.put(key, value);
        return this;
    }
    
    @Override
    public String toString() {
        return String.format("Message[type=%s, src=%s, tgt=%s, seq=%d]",
                type, sourceId, targetId, sequenceNumber);
    }
}
