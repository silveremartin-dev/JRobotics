/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.bridge.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jrobotics.core.AbstractComponent;
import org.jrobotics.core.ComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Bridge for MQTT connectivity.
 * 
 * <p>
 * Allows the robot to publish telemetry and receive commands via an MQTT
 * broker.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.5.0
 */
public class MqttBridge extends AbstractComponent implements MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(MqttBridge.class);

    private final String brokerUrl;
    private final String clientId;
    private MqttClient client;

    private final String baseTopic;

    public MqttBridge(String id, String brokerUrl, String clientId) {
        super(id, "MQTT-Bridge-" + clientId, ComponentType.COMMUNICATION);
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
        this.baseTopic = "jrobotics/" + clientId;
    }

    @Override
    protected void doStart() throws Exception {
        logger.info("Connecting to MQTT broker: {}", brokerUrl);
        client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        client.setCallback(this);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);

        client.connect(options);
        logger.info("Connected to MQTT broker");

        // Subscribe to commands
        String commandTopic = baseTopic + "/command";
        client.subscribe(commandTopic);
        logger.info("Subscribed to {}", commandTopic);
    }

    @Override
    protected void doStop() throws Exception {
        if (client != null && client.isConnected()) {
            client.disconnect();
            client.close();
            logger.info("Disconnected from MQTT broker");
        }
    }

    /**
     * Publishes a message to a subtopic.
     * 
     * @param subtopic the subtopic (e.g., "telemetry")
     * @param payload  the message payload
     */
    public void publish(String subtopic, String payload) {
        if (!isRunning() || client == null || !client.isConnected()) {
            return;
        }

        String topic = baseTopic + "/" + subtopic;
        try {
            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(0);
            client.publish(topic, message);
        } catch (MqttException e) {
            logger.error("Failed to publish to {}: {}", topic, e.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.warn("MQTT connection lost: {}", cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        logger.debug("MQTT Message [{}]: {}", topic, payload);
        // Dispatch command logic here (e.g. via EventBus)
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // QoS 1/2 confirmation
    }
}
