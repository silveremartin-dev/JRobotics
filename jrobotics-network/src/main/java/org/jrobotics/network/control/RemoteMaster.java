/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.network.control;

import org.jrobotics.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Remote control master for controlling slave robots.
 * 
 * <p>Provides high-level methods for sending commands to remote robots.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class RemoteMaster {
    
    private static final Logger logger = LoggerFactory.getLogger(RemoteMaster.class);
    
    private final NetworkNode networkNode;
    private final Map<String, SlaveInfo> slaves = new ConcurrentHashMap<>();
    private final Map<Long, CompletableFuture<Message>> pendingResponses = new ConcurrentHashMap<>();
    private Consumer<TelemetryData> telemetryCallback;
    
    /**
     * Creates a remote master.
     * 
     * @param networkNode the network node for communication
     */
    public RemoteMaster(NetworkNode networkNode) {
        this.networkNode = networkNode;
        
        // Register handlers
        networkNode.registerHandler(MessageType.TELEMETRY, this::handleTelemetry);
        networkNode.registerHandler(MessageType.COMMAND_ACK, this::handleCommandAck);
        networkNode.registerHandler(MessageType.SENSOR_DATA, this::handleSensorData);
    }
    
    /**
     * Sends a velocity command to a slave robot.
     * 
     * @param slaveId the slave ID
     * @param linear linear velocity in m/s
     * @param angular angular velocity in rad/s
     * @return future for the acknowledgment
     */
    public CompletableFuture<Boolean> sendVelocity(String slaveId, double linear, double angular) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("command", "velocity");
        payload.put("linear", linear);
        payload.put("angular", angular);
        
        return sendCommand(slaveId, payload);
    }
    
    /**
     * Sends a stop command to a slave robot.
     */
    public CompletableFuture<Boolean> sendStop(String slaveId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("command", "stop");
        
        return sendCommand(slaveId, payload);
    }
    
    /**
     * Sends an emergency stop to all slaves.
     */
    public void emergencyStopAll() {
        NetworkMessage msg = NetworkMessage.broadcast(MessageType.EMERGENCY_STOP, networkNode.getNodeId());
        networkNode.broadcast(msg);
        logger.warn("[{}] Emergency stop sent to all slaves", System.currentTimeMillis());
    }
    
    /**
     * Requests sensor data from a slave.
     */
    public CompletableFuture<Map<String, Object>> requestSensorData(String slaveId, String sensorId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("request", "sensor_data");
        payload.put("sensor_id", sensorId);
        
        NetworkMessage msg = NetworkMessage.create(MessageType.REQUEST, networkNode.getNodeId(), slaveId, payload);
        
        CompletableFuture<Message> responseFuture = new CompletableFuture<>();
        pendingResponses.put(msg.getSequenceNumber(), responseFuture);
        
        networkNode.send(msg);
        
        return responseFuture.thenApply(Message::getPayload);
    }
    
    /**
     * Sends a generic command.
     */
    private CompletableFuture<Boolean> sendCommand(String slaveId, Map<String, Object> payload) {
        NetworkMessage msg = NetworkMessage.create(MessageType.COMMAND, networkNode.getNodeId(), slaveId, payload);
        
        CompletableFuture<Message> responseFuture = new CompletableFuture<>();
        pendingResponses.put(msg.getSequenceNumber(), responseFuture);
        
        networkNode.send(msg);
        
        return responseFuture.thenApply(m -> {
            Object success = m.getPayload().get("success");
            return success != null && (Boolean) success;
        }).orTimeout(5, TimeUnit.SECONDS).exceptionally(e -> false);
    }
    
    /**
     * Sets the telemetry callback.
     */
    public void setTelemetryCallback(Consumer<TelemetryData> callback) {
        this.telemetryCallback = callback;
    }
    
    private void handleTelemetry(Message message) {
        Map<String, Object> payload = message.getPayload();
        
        TelemetryData data = new TelemetryData(
            message.getSourceId(),
            toDouble(payload.get("x"), 0.0),
            toDouble(payload.get("y"), 0.0),
            toDouble(payload.get("theta"), 0.0),
            toDouble(payload.get("battery"), 100.0),
            message.getTimestamp()
        );
        
        slaves.computeIfAbsent(message.getSourceId(), SlaveInfo::new).update(data);
        
        if (telemetryCallback != null) {
            telemetryCallback.accept(data);
        }
    }
    
    private void handleCommandAck(Message message) {
        Long seq = toLong(message.getPayload().get("sequence"));
        if (seq != null) {
            CompletableFuture<Message> future = pendingResponses.remove(seq);
            if (future != null) {
                future.complete(message);
            }
        }
    }
    
    private void handleSensorData(Message message) {
        Long seq = toLong(message.getPayload().get("request_sequence"));
        if (seq != null) {
            CompletableFuture<Message> future = pendingResponses.remove(seq);
            if (future != null) {
                future.complete(message);
            }
        }
    }

    private static double toDouble(Object obj, double defaultValue) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        return defaultValue;
    }

    private static Long toLong(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return null;
    }
    
    /**
     * Gets info about a slave.
     */
    public SlaveInfo getSlaveInfo(String slaveId) {
        return slaves.get(slaveId);
    }
    
    /**
     * Gets all connected slaves.
     */
    public Collection<SlaveInfo> getSlaves() {
        return Collections.unmodifiableCollection(slaves.values());
    }
    
    /**
     * Slave robot information.
     */
    public static class SlaveInfo {
        private final String id;
        private TelemetryData lastTelemetry;
        private long lastUpdateTime;
        
        public SlaveInfo(String id) {
            this.id = id;
        }
        
        public void update(TelemetryData data) {
            this.lastTelemetry = data;
            this.lastUpdateTime = System.currentTimeMillis();
        }
        
        public String getId() { return id; }
        public TelemetryData getLastTelemetry() { return lastTelemetry; }
        public long getLastUpdateTime() { return lastUpdateTime; }
    }
    
    /**
     * Telemetry data from a slave.
     */
    public record TelemetryData(
        String slaveId,
        double x,
        double y,
        double theta,
        double battery,
        long timestamp
    ) {}
}
