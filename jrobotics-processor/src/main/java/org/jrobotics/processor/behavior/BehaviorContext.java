/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor.behavior;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context for behavior tree execution.
 * 
 * <p>Provides shared state between behavior nodes.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class BehaviorContext {
    
    private final Map<String, Object> blackboard = new ConcurrentHashMap<>();
    private long tickCount = 0;
    private long lastTickTime = System.currentTimeMillis();
    
    /**
     * Gets a value from the blackboard.
     * 
     * @param key the key
     * @param <T> the value type
     * @return the value, or null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) blackboard.get(key);
    }
    
    /**
     * Gets a value with a default.
     * 
     * @param key the key
     * @param defaultValue the default value
     * @param <T> the value type
     * @return the value or default
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        Object value = blackboard.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    /**
     * Sets a value on the blackboard.
     * 
     * @param key the key
     * @param value the value
     */
    public void set(String key, Object value) {
        blackboard.put(key, value);
    }
    
    /**
     * Checks if a key exists.
     * 
     * @param key the key
     * @return true if exists
     */
    public boolean has(String key) {
        return blackboard.containsKey(key);
    }
    
    /**
     * Removes a value.
     * 
     * @param key the key
     */
    public void remove(String key) {
        blackboard.remove(key);
    }
    
    /**
     * Clears all values.
     */
    public void clear() {
        blackboard.clear();
    }
    
    /**
     * Called at the start of each tick.
     */
    public void tick() {
        tickCount++;
        lastTickTime = System.currentTimeMillis();
    }
    
    /**
     * Gets the tick count.
     * 
     * @return the tick count
     */
    public long getTickCount() {
        return tickCount;
    }
    
    /**
     * Gets the time since last tick in milliseconds.
     * 
     * @return the delta time
     */
    public long getDeltaTime() {
        return System.currentTimeMillis() - lastTickTime;
    }
}
