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

import org.jrobotics.core.LifecycleException;
import org.jrobotics.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * TCP server for centralized robot communication.
 * 
 * <p>Provides reliable message delivery for command and control scenarios.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class TcpServer extends AbstractNetworkNode {
    
    private static final Logger logger = LoggerFactory.getLogger(TcpServer.class);
    
    private final int port;
    private ServerSocket serverSocket;
    private final Map<String, ClientConnection> clients = new ConcurrentHashMap<>();
    private final ExecutorService acceptExecutor;
    private volatile boolean running = false;
    
    /**
     * Creates a TCP server.
     * 
     * @param nodeId the server node ID
     * @param port the port to listen on
     */
    public TcpServer(String nodeId, int port) {
        super(nodeId);
        this.port = port;
        this.acceptExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "TcpServer-" + nodeId);
            t.setDaemon(true);
            return t;
        });
    }
    
    @Override
    protected void doStart() throws LifecycleException {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            
            acceptExecutor.submit(this::acceptLoop);
            
            logger.info("[{}] TCP server started on port {}", System.currentTimeMillis(), port);
        } catch (IOException e) {
            throw new LifecycleException("Failed to start TCP server", e);
        }
    }
    
    /**
     * Accept loop for incoming connections.
     */
    private void acceptLoop() {
        while (running && !serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                acceptExecutor.submit(() -> handleClient(socket));
            } catch (IOException e) {
                if (running) {
                    logger.error("[{}] Accept error: {}", System.currentTimeMillis(), e.getMessage());
                }
            }
        }
    }
    
    /**
     * Handles a client connection.
     */
    private void handleClient(Socket socket) {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            
            // Wait for connect message
            Message connectMsg = readMessage(in);
            if (connectMsg == null || connectMsg.getType() != MessageType.CONNECT) {
                socket.close();
                return;
            }
            
            String clientId = connectMsg.getSourceId();
            ClientConnection conn = new ClientConnection(clientId, socket, in, out);
            clients.put(clientId, conn);
            addPeer(clientId);
            
            // Send ack
            NetworkMessage ack = NetworkMessage.create(MessageType.CONNECT_ACK, getNodeId(), clientId);
            writeMessage(out, ack);
            
            logger.info("[{}] Client connected: {}", System.currentTimeMillis(), clientId);
            
            // Read loop
            while (running && socket.isConnected()) {
                Message msg = readMessage(in);
                if (msg == null) break;
                
                dispatchMessage(msg);
                
                // Forward to target if specified
                if (!msg.isBroadcast() && !getNodeId().equals(msg.getTargetId())) {
                    ClientConnection target = clients.get(msg.getTargetId());
                    if (target != null) {
                        writeMessage(target.out, msg);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("[{}] Client handler error: {}", System.currentTimeMillis(), e.getMessage());
        } finally {
            // Clean up
            clients.values().removeIf(c -> {
                if (!c.socket.isConnected()) {
                    removePeer(c.clientId);
                    return true;
                }
                return false;
            });
        }
    }
    
    @Override
    public boolean send(Message message) {
        ClientConnection conn = clients.get(message.getTargetId());
        if (conn != null) {
            try {
                writeMessage(conn.out, message);
                return true;
            } catch (IOException e) {
                logger.error("[{}] Send failed: {}", System.currentTimeMillis(), e.getMessage());
            }
        }
        return false;
    }
    
    @Override
    public void broadcast(Message message) {
        for (ClientConnection conn : clients.values()) {
            try {
                writeMessage(conn.out, message);
            } catch (IOException e) {
                logger.error("[{}] Broadcast failed to {}: {}", 
                        System.currentTimeMillis(), conn.clientId, e.getMessage());
            }
        }
    }
    
    @Override
    public boolean connectTo(String address) {
        return false; // Server doesn't connect to others
    }
    
    @Override
    public boolean isConnected() {
        return running && serverSocket != null && !serverSocket.isClosed();
    }
    
    @Override
    public void disconnect() {
        running = false;
        
        for (ClientConnection conn : clients.values()) {
            try {
                conn.socket.close();
            } catch (IOException ignored) {}
        }
        clients.clear();
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
        }
        
        acceptExecutor.shutdownNow();
    }
    
    /**
     * Gets the port.
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Gets the number of connected clients.
     */
    public int getClientCount() {
        return clients.size();
    }
    
    private Message readMessage(ObjectInputStream in) throws IOException, ClassNotFoundException {
        try {
            return (Message) in.readObject();
        } catch (EOFException | SocketException e) {
            return null;
        }
    }
    
    private void writeMessage(ObjectOutputStream out, Message msg) throws IOException {
        synchronized (out) {
            out.writeObject(msg);
            out.flush();
        }
    }
    
    /**
     * Client connection holder.
     */
    private static class ClientConnection {
        final String clientId;
        final Socket socket;
        final ObjectInputStream in;
        final ObjectOutputStream out;
        
        ClientConnection(String clientId, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
            this.clientId = clientId;
            this.socket = socket;
            this.in = in;
            this.out = out;
        }
    }
}
