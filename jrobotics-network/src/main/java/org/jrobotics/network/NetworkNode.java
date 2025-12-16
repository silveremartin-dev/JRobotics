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

import org.jrobotics.core.Lifecycle;

import java.util.Set;

/**
 * Base interface for network nodes (robots or control stations).
 * 
 * <p>A network node can send and receive messages over the network.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface NetworkNode extends Lifecycle {
    
    /**
     * Gets this node's unique identifier.
     * 
     * @return the node ID
     */
    String getNodeId();
    
    /**
     * Gets the set of connected peer IDs.
     * 
     * @return the set of connected peers
     */
    Set<String> getConnectedPeers();
    
    /**
     * Sends a message to a specific target.
     * 
     * @param message the message to send
     * @return true if sent successfully
     */
    boolean send(Message message);
    
    /**
     * Broadcasts a message to all connected peers.
     * 
     * @param message the message to broadcast
     */
    void broadcast(Message message);
    
    /**
     * Registers a handler for a specific message type.
     * 
     * @param type the message type
     * @param handler the handler
     */
    void registerHandler(MessageType type, MessageHandler handler);
    
    /**
     * Unregisters a handler for a specific message type.
     * 
     * @param type the message type
     * @param handler the handler to remove
     */
    void unregisterHandler(MessageType type, MessageHandler handler);
    
    /**
     * Checks if connected to the network.
     * 
     * @return true if connected
     */
    boolean isConnected();
    
    /**
     * Connects to a peer by address.
     * 
     * @param address the peer address (e.g., "host:port")
     * @return true if connection initiated successfully
     */
    boolean connectTo(String address);
    
    /**
     * Disconnects from the network.
     */
    void disconnect();
}
