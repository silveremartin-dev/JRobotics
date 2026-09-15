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
import java.util.function.*;

/**
 * Remote control slave for receiving commands from a master.
 * 
 * <p>
 * Provides callbacks for handling remote commands.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class RemoteSlave {

    private static final Logger logger = LoggerFactory.getLogger(RemoteSlave.class);

    private final NetworkNode networkNode;
    private BiConsumer<Double, Double> velocityHandler;
    private Runnable stopHandler;
    private Runnable emergencyStopHandler;
    private Function<String, Map<String, Object>> sensorDataProvider;

    /**
     * Creates a remote slave.
     * 
     * @param networkNode the network node for communication
     */
    public RemoteSlave(NetworkNode networkNode) {
        this.networkNode = networkNode;

        // Register handlers
        networkNode.registerHandler(MessageType.COMMAND, this::handleCommand);
        networkNode.registerHandler(MessageType.REQUEST, this::handleRequest);
        networkNode.registerHandler(MessageType.EMERGENCY_STOP, this::handleEmergencyStop);
    }

    /**
     * Sets the velocity command handler.
     * 
     * @param handler receives (linear, angular)
     */
    public void setVelocityHandler(BiConsumer<Double, Double> handler) {
        this.velocityHandler = handler;
    }

    /**
     * Sets the stop handler.
     */
    public void setStopHandler(Runnable handler) {
        this.stopHandler = handler;
    }

    /**
     * Sets the emergency stop handler.
     */
    public void setEmergencyStopHandler(Runnable handler) {
        this.emergencyStopHandler = handler;
    }

    /**
     * Sets the sensor data provider.
     * 
     * @param provider function that returns sensor data for a sensor ID
     */
    public void setSensorDataProvider(Function<String, Map<String, Object>> provider) {
        this.sensorDataProvider = provider;
    }

    /**
     * Sends telemetry to the master.
     */
    public void sendTelemetry(double x, double y, double theta, double battery) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("x", x);
        payload.put("y", y);
        payload.put("theta", theta);
        payload.put("battery", battery);

        NetworkMessage msg = NetworkMessage.broadcast(MessageType.TELEMETRY, networkNode.getNodeId(), payload);
        networkNode.broadcast(msg);
    }

    private void handleCommand(Message message) {
        Map<String, Object> payload = message.getPayload();
        String command = (String) payload.get("command");

        boolean success = false;

        if ("velocity".equals(command) && velocityHandler != null) {
            double linear = toDouble(payload.get("linear"), 0.0);
            double angular = toDouble(payload.get("angular"), 0.0);
            velocityHandler.accept(linear, angular);
            success = true;
            logger.debug("[{}] Velocity command: linear={}, angular={}",
                    System.currentTimeMillis(), linear, angular);
        } else if ("stop".equals(command) && stopHandler != null) {
            stopHandler.run();
            success = true;
            logger.info("[{}] Stop command received", System.currentTimeMillis());
        }

        // Send acknowledgment
        Map<String, Object> ackPayload = new HashMap<>();
        ackPayload.put("sequence", message.getSequenceNumber());
        ackPayload.put("success", success);

        NetworkMessage ack = NetworkMessage.create(
                MessageType.COMMAND_ACK, networkNode.getNodeId(), message.getSourceId(), ackPayload);
        networkNode.send(ack);
    }

    private void handleRequest(Message message) {
        Map<String, Object> payload = message.getPayload();
        String request = (String) payload.get("request");

        if ("sensor_data".equals(request) && sensorDataProvider != null) {
            String sensorId = (String) payload.get("sensor_id");
            Map<String, Object> sensorData = sensorDataProvider.apply(sensorId);

            Map<String, Object> responsePayload = sensorData != null ? new HashMap<>(sensorData) : new HashMap<>();
            responsePayload.put("request_sequence", message.getSequenceNumber());

            NetworkMessage response = NetworkMessage.create(
                    MessageType.SENSOR_DATA, networkNode.getNodeId(), message.getSourceId(), responsePayload);
            networkNode.send(response);
        }
    }

    private static double toDouble(Object obj, double defaultValue) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        return defaultValue;
    }

    private void handleEmergencyStop(Message message) {
        logger.warn("[{}] Emergency stop received from {}",
                System.currentTimeMillis(), message.getSourceId());

        if (emergencyStopHandler != null) {
            emergencyStopHandler.run();
        }

        if (stopHandler != null) {
            stopHandler.run();
        }
    }
}
