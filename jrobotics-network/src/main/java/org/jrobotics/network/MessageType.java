/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.network;

/**
 * Types of network messages.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public enum MessageType {
    
    // Connection messages
    /** Connection request */
    CONNECT,
    /** Connection accepted */
    CONNECT_ACK,
    /** Disconnect notification */
    DISCONNECT,
    /** Heartbeat/keep-alive */
    HEARTBEAT,
    
    // Discovery messages
    /** Discovery broadcast */
    DISCOVER,
    /** Discovery response */
    DISCOVER_RESPONSE,
    
    // Control messages
    /** Command from master to slave */
    COMMAND,
    /** Command acknowledgment */
    COMMAND_ACK,
    /** Stop all motion */
    EMERGENCY_STOP,
    
    // Data messages
    /** Sensor data */
    SENSOR_DATA,
    /** State update */
    STATE_UPDATE,
    /** Telemetry data */
    TELEMETRY,
    
    // System messages
    /** Error notification */
    ERROR,
    /** Status request */
    STATUS_REQUEST,
    /** Status response */
    STATUS_RESPONSE,
    
    // Custom
    /** User-defined message type */
    CUSTOM
}
