/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.bridge;

import org.jrobotics.core.Lifecycle;

/**
 * Bridge interface for connecting to external robotics frameworks.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface RoboticsBridge extends Lifecycle {
    
    /**
     * Gets the bridge name.
     * 
     * @return the name (e.g., "ROS2", "Webots")
     */
    String getBridgeName();
    
    /**
     * Checks if the bridge is connected to the external framework.
     * 
     * @return true if connected
     */
    boolean isConnectedToFramework();
    
    /**
     * Publishes a message to the external framework.
     * 
     * @param topic the topic name
     * @param message the message data
     * @return true if published successfully
     */
    boolean publish(String topic, Object message);
    
    /**
     * Subscribes to a topic from the external framework.
     * 
     * @param topic the topic name
     * @param handler the message handler
     * @param <T> the message type
     */
    <T> void subscribe(String topic, Class<T> type, java.util.function.Consumer<T> handler);
    
    /**
     * Unsubscribes from a topic.
     * 
     * @param topic the topic name
     */
    void unsubscribe(String topic);
    
    /**
     * Calls a service.
     * 
     * @param serviceName the service name
     * @param request the request data
     * @param <R> the response type
     * @return the response
     */
    <R> R callService(String serviceName, Object request, Class<R> responseType);
}
