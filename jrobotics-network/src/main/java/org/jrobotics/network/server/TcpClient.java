/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.network.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jrobotics.core.LifecycleException;
import org.jrobotics.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * TCP client for connecting to a central server.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class TcpClient extends AbstractNetworkNode {

    private static final Logger logger = LoggerFactory.getLogger(TcpClient.class);
    private static final int MAX_MESSAGE_SIZE = 10 * 1024 * 1024; // 10MB limit

    private final ObjectMapper mapper = new ObjectMapper();
    private String serverHost;
    private int serverPort;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final ExecutorService readExecutor;
    private volatile boolean running = false;

    /**
     * Creates a TCP client.
     * 
     * @param nodeId the client node ID
     */
    public TcpClient(String nodeId) {
        super(nodeId);
        this.readExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "TcpClient-" + nodeId);
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    protected void doStart() throws LifecycleException {
        // Client starts but doesn't connect until connectTo is called
        logger.info("[{}] TCP client {} started", System.currentTimeMillis(), getNodeId());
    }

    @Override
    public boolean connectTo(String address) {
        // Parse "host:port"
        String[] parts = address.split(":");
        if (parts.length != 2) {
            logger.error("[{}] Invalid address: {}", System.currentTimeMillis(), address);
            return false;
        }

        try {
            serverHost = parts[0];
            serverPort = Integer.parseInt(parts[1]);

            socket = new Socket(serverHost, serverPort);
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            running = true;

            // Send connect message
            NetworkMessage connect = NetworkMessage.create(MessageType.CONNECT, getNodeId(), "server");
            writeMessage(connect);

            // Wait for ack
            Message ack = readMessage();
            if (ack != null && ack.getType() == MessageType.CONNECT_ACK) {
                addPeer(ack.getSourceId());
                readExecutor.submit(this::readLoop);
                logger.info("[{}] Connected to server at {}", System.currentTimeMillis(), address);
                return true;
            }
        } catch (Exception e) {
            logger.error("[{}] Connect failed: {}", System.currentTimeMillis(), e.getMessage());
            disconnect();
        }

        return false;
    }

    /**
     * Read loop for incoming messages.
     */
    private void readLoop() {
        while (running && socket != null && socket.isConnected() && !socket.isClosed()) {
            try {
                Message msg = readMessage();
                if (msg != null) {
                    dispatchMessage(msg);
                } else {
                    break;
                }
            } catch (EOFException | SocketException e) {
                break;
            } catch (Exception e) {
                logger.error("[{}] Read error: {}", System.currentTimeMillis(), e.getMessage());
            }
        }
    }

    @Override
    public boolean send(Message message) {
        if (!isConnected())
            return false;

        try {
            writeMessage(message);
            return true;
        } catch (IOException e) {
            logger.error("[{}] Send failed: {}", System.currentTimeMillis(), e.getMessage());
            return false;
        }
    }

    @Override
    public void broadcast(Message message) {
        // Client broadcasts go through server
        send(message);
    }

    @Override
    public boolean isConnected() {
        return running && socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public void disconnect() {
        running = false;

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }

        readExecutor.shutdownNow();
        logger.info("[{}] TCP client {} disconnected", System.currentTimeMillis(), getNodeId());
    }

    private Message readMessage() throws IOException {
        try {
            int length = in.readInt();
            if (length <= 0 || length > MAX_MESSAGE_SIZE) {
                throw new IOException("Invalid message length: " + length);
            }
            byte[] bytes = in.readNBytes(length);
            if (bytes.length < length) {
                return null;
            }
            return mapper.readValue(bytes, NetworkMessage.class);
        } catch (EOFException | SocketException e) {
            return null;
        }
    }

    private void writeMessage(Message msg) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(msg);
        synchronized (out) {
            out.writeInt(bytes.length);
            out.write(bytes);
            out.flush();
        }
    }
}
