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
 * Represents a capability that a robot or component can have.
 * 
 * <p>
 * Capabilities are used for discovery and feature negotiation between
 * robots and between a robot and its controller.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public enum Capability {

    // Locomotion capabilities
    /**
     * Robot can move using wheels.
     */
    WHEELED_LOCOMOTION("wheeled_locomotion", "Wheeled movement", Category.LOCOMOTION),

    /**
     * Robot can move using legs.
     */
    LEGGED_LOCOMOTION("legged_locomotion", "Legged movement", Category.LOCOMOTION),

    /**
     * Robot can fly.
     */
    FLYING("flying", "Aerial movement", Category.LOCOMOTION),

    /**
     * Robot can move underwater.
     */
    UNDERWATER("underwater", "Underwater movement", Category.LOCOMOTION),

    // Sensing capabilities
    /**
     * Robot has vision sensors (cameras).
     */
    VISION("vision", "Visual sensing", Category.SENSING),

    /**
     * Robot has depth perception (stereo, depth camera, or LIDAR).
     */
    DEPTH_SENSING("depth_sensing", "Depth perception", Category.SENSING),

    /**
     * Robot has LIDAR sensors.
     */
    LIDAR("lidar", "LIDAR sensing", Category.SENSING),

    /**
     * Robot has ultrasonic sensors.
     */
    ULTRASONIC("ultrasonic", "Ultrasonic sensing", Category.SENSING),

    /**
     * Robot has IMU (accelerometer, gyroscope).
     */
    IMU("imu", "Inertial measurement", Category.SENSING),

    /**
     * Robot has GPS/GNSS.
     */
    GPS("gps", "GPS positioning", Category.SENSING),

    /**
     * Robot has touch/force sensors.
     */
    TOUCH("touch", "Touch sensing", Category.SENSING),

    // Manipulation capabilities
    /**
     * Robot can grasp objects.
     */
    GRASPING("grasping", "Object grasping", Category.MANIPULATION),

    /**
     * Robot has a robotic arm.
     */
    ARM("arm", "Robotic arm", Category.MANIPULATION),

    /**
     * Robot can manipulate small/precise objects.
     */
    FINE_MANIPULATION("fine_manipulation", "Fine manipulation", Category.MANIPULATION),

    // Communication capabilities
    /**
     * Robot supports peer-to-peer communication.
     */
    P2P_COMMUNICATION("p2p_communication", "Peer-to-peer communication", Category.COMMUNICATION),

    /**
     * Robot can be remotely controlled.
     */
    REMOTE_CONTROL("remote_control", "Remote control", Category.COMMUNICATION),

    /**
     * Robot supports swarm coordination.
     */
    SWARM("swarm", "Swarm coordination", Category.COMMUNICATION),

    // AI capabilities
    /**
     * Robot supports SLAM (Simultaneous Localization and Mapping).
     */
    SLAM("slam", "SLAM", Category.AI),

    /**
     * Robot has path planning capabilities.
     */
    PATH_PLANNING("path_planning", "Path planning", Category.AI),

    /**
     * Robot has object recognition.
     */
    OBJECT_RECOGNITION("object_recognition", "Object recognition", Category.AI),

    /**
     * Robot supports speech recognition.
     */
    SPEECH_RECOGNITION("speech_recognition", "Speech recognition", Category.AI),

    /**
     * Robot supports natural language processing.
     */
    NLP("nlp", "Natural language processing", Category.AI);

    /**
     * Capability category.
     */
    public enum Category {
        LOCOMOTION,
        SENSING,
        MANIPULATION,
        COMMUNICATION,
        AI,
        OTHER
    }

    private final String code;
    private final String displayName;
    private final Category category;

    Capability(String code, String displayName, Category category) {
        this.code = code;
        this.displayName = displayName;
        this.category = category;
    }

    /**
     * Gets the unique code for this capability.
     * 
     * @return the code
     */
    public String getCode() {
        return code;
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
     * Gets the category of this capability.
     * 
     * @return the category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Finds a capability by its code.
     * 
     * @param code the code to search for
     * @return the capability, or null if not found
     */
    public static Capability fromCode(String code) {
        for (Capability capability : values()) {
            if (capability.code.equalsIgnoreCase(code)) {
                return capability;
            }
        }
        return null;
    }
}
