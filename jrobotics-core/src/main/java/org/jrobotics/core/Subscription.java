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

/**
 * Represents a subscription to events on the event bus.
 * 
 * <p>
 * Subscriptions can be cancelled by calling {@link #unsubscribe()}.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface Subscription {

    /**
     * Cancels this subscription.
     * 
     * <p>
     * After calling this method, the handler will no longer receive events.
     * </p>
     */
    void unsubscribe();

    /**
     * Checks if this subscription is still active.
     * 
     * @return true if active
     */
    boolean isActive();

    /**
     * Gets the event type this subscription is for.
     * 
     * @return the event type class
     */
    Class<? extends Event> getEventType();
}
