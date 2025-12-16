/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.network.peer;

import org.jrobotics.core.LifecycleException;
import org.jrobotics.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * UDP-based peer-to-peer network node.
 * 
 * <p>Uses UDP for low-latency communication suitable for real-time
 * robot control and sensor data exchange.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class UdpPeerNode extends AbstractNetworkNode {
    
    private static final Logger logger = LoggerFactory.getLogger(UdpPeerNode.class);
    private static final int DEFAULT_PORT = 9876;
    private static final int BUFFER_SIZE = 65535;
    
    private final int port;
    private final Map<String, InetSocketAddress> peerAddresses = new ConcurrentHashMap<>();
    private DatagramSocket socket;
    private volatile boolean running = false;
    private Thread receiverThread;
    
    /**
     * Constructs a UDP peer node on the default port.
     * 
     * @param nodeId the unique node identifier
     */
    public UdpPeerNode(String nodeId) {
        this(nodeId, DEFAULT_PORT);
    }
    
    /**
     * Constructs a UDP peer node on a specific port.
     * 
     * @param nodeId the unique node identifier
     * @param port the UDP port
     */
    public UdpPeerNode(String nodeId, int port) {
        super(nodeId);
        this.port = port;
    }
    
    @Override
    protected void doStart() throws LifecycleException {
        try {
            socket = new DatagramSocket(port);
            running = true;
            
            receiverThread = new Thread(this::receiveLoop, "UdpReceiver-" + getNodeId());
            receiverThread.setDaemon(true);
            receiverThread.start();
            
            logger.info("[{}] UDP peer node started on port {}", System.currentTimeMillis(), port);
        } catch (SocketException e) {
            throw new LifecycleException("Failed to start UDP socket", e);
        }
    }
    
    /**
     * Receiver loop for incoming UDP packets.
     */
    private void receiveLoop() {
        byte[] buffer = new byte[BUFFER_SIZE];
        
        while (running && !socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                // Deserialize message
                Message message = deserialize(packet.getData(), packet.getLength());
                if (message != null) {
                    // Track peer address
                    peerAddresses.put(message.getSourceId(), 
                            new InetSocketAddress(packet.getAddress(), packet.getPort()));
                    addPeer(message.getSourceId());
                    
                    dispatchMessage(message);
                }
            } catch (SocketException e) {
                if (running) {
                    logger.error("[{}] Socket error: {}", System.currentTimeMillis(), e.getMessage());
                }
            } catch (IOException e) {
                logger.error("[{}] Receive error: {}", System.currentTimeMillis(), e.getMessage());
            }
        }
    }
    
    @Override
    public boolean send(Message message) {
        if (!isConnected()) {
            return false;
        }
        
        String targetId = message.getTargetId();
        InetSocketAddress address = peerAddresses.get(targetId);
        if (address == null) {
            logger.warn("[{}] Unknown target: {}", System.currentTimeMillis(), targetId);
            return false;
        }
        
        return sendToAddress(message, address);
    }
    
    @Override
    public void broadcast(Message message) {
        if (!isConnected()) {
            return;
        }
        
        for (InetSocketAddress address : peerAddresses.values()) {
            sendToAddress(message, address);
        }
    }
    
    /**
     * Sends a message to a specific address.
     * 
     * @param message the message
     * @param address the target address
     * @return true if sent
     */
    private boolean sendToAddress(Message message, InetSocketAddress address) {
        try {
            byte[] data = serialize(message);
            DatagramPacket packet = new DatagramPacket(data, data.length, address);
            socket.send(packet);
            return true;
        } catch (IOException e) {
            logger.error("[{}] Send failed: {}", System.currentTimeMillis(), e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean connectTo(String address) {
        // Parse address as "host:port"
        String[] parts = address.split(":");
        if (parts.length != 2) {
            logger.error("[{}] Invalid address format: {}", System.currentTimeMillis(), address);
            return false;
        }
        
        try {
            InetSocketAddress sockAddr = new InetSocketAddress(
                    InetAddress.getByName(parts[0]), Integer.parseInt(parts[1]));
            
            // Send discovery message
            NetworkMessage discover = NetworkMessage.broadcast(MessageType.DISCOVER, getNodeId());
            sendToAddress(discover, sockAddr);
            
            return true;
        } catch (Exception e) {
            logger.error("[{}] Connect failed: {}", System.currentTimeMillis(), e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isConnected() {
        return running && socket != null && !socket.isClosed();
    }
    
    @Override
    public void disconnect() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        peerAddresses.clear();
    }
    
    /**
     * Serializes a message to bytes.
     * 
     * @param message the message
     * @return the serialized bytes
     * @throws IOException if serialization fails
     */
    private byte[] serialize(Message message) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(message.getType().name());
            oos.writeObject(message.getSourceId());
            oos.writeObject(message.getTargetId());
            oos.writeLong(message.getTimestamp());
            oos.writeLong(message.getSequenceNumber());
            oos.writeObject(new HashMap<>(message.getPayload()));
            return bos.toByteArray();
        }
    }
    
    /**
     * Deserializes bytes to a message.
     * 
     * @param data the byte data
     * @param length the data length
     * @return the message, or null if failed
     */
    @SuppressWarnings("unchecked")
    private Message deserialize(byte[] data, int length) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data, 0, length);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            MessageType type = MessageType.valueOf((String) ois.readObject());
            String sourceId = (String) ois.readObject();
            String targetId = (String) ois.readObject();
            long timestamp = ois.readLong();
            long sequence = ois.readLong();
            Map<String, Object> payload = (Map<String, Object>) ois.readObject();
            
            return new NetworkMessage(type, sourceId, targetId, payload);
        } catch (Exception e) {
            logger.error("[{}] Deserialize failed: {}", System.currentTimeMillis(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets the UDP port.
     * 
     * @return the port
     */
    public int getPort() {
        return port;
    }
}
