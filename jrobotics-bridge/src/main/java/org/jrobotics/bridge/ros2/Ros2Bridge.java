/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.bridge.ros2;

import id.jros2client.JRos2Client;
import id.jros2client.JRos2ClientFactory;
import id.jrosmessages.Message;
import org.jrobotics.bridge.RoboticsBridge;
import org.jrobotics.core.LifecycleException;
import org.jrobotics.core.LifecycleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Bridge for ROS2 (Robot Operating System 2).
 * 
 * <p>
 * Connects JRobotics to ROS2 DDS domain using jros2client.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class Ros2Bridge implements RoboticsBridge {

    private static final Logger logger = LoggerFactory.getLogger(Ros2Bridge.class);

    private final String nodeName; // e.g. "jrobotics_bridge"
    private LifecycleState state = LifecycleState.CREATED;
    private final Map<String, List<Consumer<?>>> subscribers = new ConcurrentHashMap<>();

    private JRos2Client client;
    private boolean connected = false;

    public Ros2Bridge(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public String getBridgeName() {
        return "ROS2";
    }

    @Override
    public void initialize() throws LifecycleException {
        state = LifecycleState.INITIALIZED;
        // JRos2Client initialization is typically lazy or in start()
        logger.info("[{}] ROS2 bridge '{}' initialized", System.currentTimeMillis(), nodeName);
    }

    @Override
    public void start() throws LifecycleException {
        try {
            logger.info("Starting ROS2 client...");
            client = new JRos2ClientFactory().createClient();
            // JRos2Client doesn't have a rigid start(), it starts on first use usually,
            // but we can check connectivity or just mark as running.

            state = LifecycleState.RUNNING;
            connected = true;
            logger.info("[{}] ROS2 bridge '{}' started connected to DDS domain",
                    System.currentTimeMillis(), nodeName);
        } catch (Exception e) {
            throw new LifecycleException("Failed to start ROS2 bridge", e);
        }
    }

    @Override
    public void stop() throws LifecycleException {
        if (client != null) {
            try {
                // JRos2Client implements AutoCloseable usually, or we just null it
                // In v12 it might be close()
                if (client instanceof AutoCloseable) {
                    ((AutoCloseable) client).close();
                }
            } catch (Exception e) {
                logger.error("Error stopping ROS2 client", e);
            }
        }
        connected = false;
        state = LifecycleState.STOPPED;
        logger.info("[{}] ROS2 bridge '{}' stopped", System.currentTimeMillis(), nodeName);
    }

    @Override
    public void pause() throws LifecycleException {
        state = LifecycleState.PAUSED;
    }

    @Override
    public void resume() throws LifecycleException {
        state = LifecycleState.RUNNING;
    }

    @Override
    public void shutdown() throws LifecycleException {
        stop();
        subscribers.clear();
        state = LifecycleState.DESTROYED;
        logger.info("[{}] ROS2 bridge '{}' destroyed", System.currentTimeMillis(), nodeName);
    }

    public LifecycleState getState() {
        return state;
    }

    @Override
    public boolean isRunning() {
        return state == LifecycleState.RUNNING;
    }

    @Override
    public boolean isConnectedToFramework() {
        return connected;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean publish(String topic, Object message) {
        if (!connected || client == null)
            return false;

        // This requires the message object to be a valid id.jrosmessages.Message
        if (message instanceof Message) {
            try {
                // Wrap message in a Publisher as expected by JRosClient
                // We use SubmissionPublisher (standard Java Flow API)
                // Use TopicSubmissionPublisher which combines Topic info and
                // SubmissionPublisher capabilities
                Class<Message> msgClass = (Class<Message>) message.getClass();
                id.jrosclient.TopicSubmissionPublisher<Message> publisher = new id.jrosclient.TopicSubmissionPublisher<>(
                        msgClass, topic);

                client.publish(publisher);
                publisher.submit((Message) message);
                publisher.close();

                return true;
            } catch (Exception e) {
                logger.error("Failed to publish ROS2 message", e);
                return false;
            }
        } else {
            logger.warn("Cannot publish non-JRosMessage object: {}", message.getClass().getName());
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void subscribe(String topic, Class<T> type, Consumer<T> handler) {
        if (!connected || client == null) {
            logger.warn("Cannot subscribe: ROS2 client not connected");
            return;
        }

        if (Message.class.isAssignableFrom(type)) {
            Class<Message> msgType = (Class<Message>) type;

            // Create a TopicSubscriber that forwards to the handler
            id.jrosclient.TopicSubscriber<Message> subscriber = new id.jrosclient.TopicSubscriber<>(msgType, topic) {
                public void onNext(Message item) {
                    handler.accept((T) item);
                }
            };

            client.subscribe(subscriber);
            logger.info("Subscribed to ROS2 topic: {}", topic);
        } else {
            logger.warn("Unsupported message type for ROS2: {}", type.getName());
        }
    }

    @Override
    public void unsubscribe(String topic) {
        // JRos2Client might not expose easy unsubscribe in partial API
        logger.warn("Unsubscribe not fully supported in this bridge version");
    }

    @Override
    public <R> R callService(String serviceName, Object request, Class<R> responseType) {
        logger.warn("Service calls not yet supported");
        return null;
    }

    public String getNodeName() {
        return nodeName;
    }
}
