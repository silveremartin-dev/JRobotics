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

import java.util.function.Consumer;

/**
 * Event bus interface for asynchronous component communication.
 * 
 * <p>
 * The event bus follows a publish-subscribe pattern, allowing components
 * to communicate without direct dependencies.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Subscribe to events
 * eventBus.subscribe(SensorDataEvent.class, event -> {
 *     System.out.println("Received: " + event.getData());
 * });
 * 
 * // Publish an event
 * eventBus.publish(new SensorDataEvent("sensor1", data));
 * }</pre>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface EventBus {

    /**
     * Subscribes a handler to events of a specific type.
     * 
     * @param <T>       the event type
     * @param eventType the class of events to subscribe to
     * @param handler   the event handler
     * @return a subscription that can be used to unsubscribe
     */
    <T extends Event> Subscription subscribe(Class<T> eventType, Consumer<T> handler);

    /**
     * Subscribes a handler to events of a specific type with a priority.
     * 
     * <p>
     * Higher priority handlers are called first.
     * </p>
     * 
     * @param <T>       the event type
     * @param eventType the class of events to subscribe to
     * @param handler   the event handler
     * @param priority  the handler priority (higher = called first)
     * @return a subscription that can be used to unsubscribe
     */
    <T extends Event> Subscription subscribe(Class<T> eventType, Consumer<T> handler, int priority);

    /**
     * Publishes an event to all subscribed handlers.
     * 
     * <p>
     * This method is asynchronous by default.
     * </p>
     * 
     * @param event the event to publish
     */
    void publish(Event event);

    /**
     * Publishes an event and waits for all handlers to complete.
     * 
     * @param event the event to publish
     */
    void publishSync(Event event);

    /**
     * Removes all subscriptions for a specific event type.
     * 
     * @param eventType the event type to clear
     */
    void clearSubscriptions(Class<? extends Event> eventType);

    /**
     * Removes all subscriptions.
     */
    void clearAllSubscriptions();

    /**
     * Shuts down the event bus, releasing all resources.
     */
    void shutdown();
}
