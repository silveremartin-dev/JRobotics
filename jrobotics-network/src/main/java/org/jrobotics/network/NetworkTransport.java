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
 * Interface for pluggable network transport implementations.
 * 
 * <p>Allows integration with different networking libraries while keeping
 * the same high-level API. Implementations can use:</p>
 * <ul>
 *   <li>Built-in Java sockets (default)</li>
 *   <li>Netty for high-performance</li>
 *   <li>ZeroMQ for pub/sub patterns</li>
 *   <li>gRPC for RPC-style communication</li>
 * </ul>
 * 
 * <p><b>Recommendation for Robotics:</b> The built-in implementation is 
 * sufficient for most robot communication needs. gRPC/ZeroMQ add complexity
 * and dependencies that may not be justified for embedded systems.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface NetworkTransport {
    
    /**
     * Transport types.
     */
    enum Type {
        /** Java built-in Socket/DatagramSocket */
        JAVA_NIO,
        /** Netty async I/O */
        NETTY,
        /** ZeroMQ messaging */
        ZEROMQ,
        /** gRPC remote procedure calls */
        GRPC,
        /** WebSocket for browser clients */
        WEBSOCKET
    }
    
    /**
     * Gets the transport type.
     * 
     * @return the type
     */
    Type getType();
    
    /**
     * Gets the transport name.
     * 
     * @return the name
     */
    String getName();
    
    /**
     * Starts the transport.
     * 
     * @return true if started
     */
    boolean start();
    
    /**
     * Stops the transport.
     */
    void stop();
    
    /**
     * Checks if running.
     * 
     * @return true if running
     */
    boolean isRunning();
    
    /**
     * Sends raw bytes to a destination.
     * 
     * @param destination the target address
     * @param data the data to send
     * @return true if sent
     */
    boolean send(String destination, byte[] data);
    
    /**
     * Sets the receive handler.
     * 
     * @param handler callback for received data
     */
    void setReceiveHandler(ReceiveHandler handler);
    
    /**
     * Receive handler callback.
     */
    @FunctionalInterface
    interface ReceiveHandler {
        void onReceive(String source, byte[] data);
    }
    
    /**
     * Gets maximum message size.
     * 
     * @return max size in bytes
     */
    int getMaxMessageSize();
    
    /**
     * Checks if transport guarantees delivery.
     * 
     * @return true if reliable
     */
    boolean isReliable();
    
    /**
     * Checks if transport preserves message order.
     * 
     * @return true if ordered
     */
    boolean isOrdered();
    
    /**
     * Gets typical latency in milliseconds.
     * 
     * @return latency estimate
     */
    double getTypicalLatencyMs();
}
