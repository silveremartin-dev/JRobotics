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
 * Enumeration of component types in the robotics system.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public enum ComponentType {

    /**
     * Sensor component - acquires data from the environment.
     */
    SENSOR("Sensor", "sensor"),

    /**
     * Actuator component - performs physical actions.
     */
    ACTUATOR("Actuator", "actuator"),

    /**
     * Processor component - processes data and makes decisions.
     */
    PROCESSOR("Processor", "processor"),

    /**
     * Communication component - handles networking.
     */
    COMMUNICATION("Communication", "communication"),

    /**
     * Controller component - orchestrates other components.
     */
    CONTROLLER("Controller", "controller"),

    /**
     * Generic or custom component type.
     */
    OTHER("Other", "other");

    private final String displayName;
    private final String code;

    ComponentType(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    /**
     * Gets the human-readable display name.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the code for serialization.
     * 
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Finds a component type by its code.
     * 
     * @param code the code to search for
     * @return the component type, or OTHER if not found
     */
    public static ComponentType fromCode(String code) {
        for (ComponentType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return OTHER;
    }
}
