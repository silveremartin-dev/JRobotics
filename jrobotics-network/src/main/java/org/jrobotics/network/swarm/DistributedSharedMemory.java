/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.network.swarm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jrobotics.core.Lifecycle;
import org.jrobotics.core.LifecycleException;
import org.jrobotics.core.LifecycleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Distributed Shared Memory (DSM) for Swarm Robotics.
 * 
 * <p>
 * Provides a key-value store that is eventually consistent across all nodes in
 * the swarm.
 * Uses UDP Multicast (or Broadcast) to efficiently share updates.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.6.0
 */
public class DistributedSharedMemory implements Lifecycle {

    private static final Logger logger = LoggerFactory.getLogger(DistributedSharedMemory.class);
    private static final int DEFAULT_PORT = 9999;
    private static final String DEFAULT_GROUP = "230.0.0.1";

    private final String nodeId;
    private final Transport transport;
    private final ObjectMapper mapper = new ObjectMapper();

    // In-memory storage: Key -> DistributedEntry
    private final Map<String, DistributedEntry> storage = new ConcurrentHashMap<>();

    private volatile LifecycleState state = LifecycleState.CREATED;
    private ScheduledExecutorService broadcaster;
    private volatile boolean running = false;

    /**
     * Creates a DSM node using default UDP Multicast transport.
     * 
     * @param nodeId unique ID of this node
     */
    public DistributedSharedMemory(String nodeId) {
        this(nodeId, new UdpMulticastTransport(DEFAULT_PORT, DEFAULT_GROUP));
    }

    /**
     * Creates a DSM node with custom settings.
     * 
     * @param nodeId         unique ID of this node
     * @param port           UDP port
     * @param multicastGroup multicast group address
     */
    public DistributedSharedMemory(String nodeId, int port, String multicastGroup) {
        this(nodeId, new UdpMulticastTransport(port, multicastGroup));
    }

    /**
     * Creates a DSM node with a specific transport (useful for testing).
     * 
     * @param nodeId    unique ID of this node
     * @param transport the transport implementation
     */
    public DistributedSharedMemory(String nodeId, Transport transport) {
        this.nodeId = nodeId;
        this.transport = transport;
    }

    @Override
    public void initialize() throws LifecycleException {
        transport.initialize(this::processIncomingUpdate);
        state = LifecycleState.INITIALIZED;
        logger.info("DSM initialized for node {}", nodeId);
    }

    @Override
    public void start() throws LifecycleException {
        if (state != LifecycleState.INITIALIZED && state != LifecycleState.STOPPED) {
            throw new LifecycleException("Cannot start from state: " + state);
        }

        running = true;
        transport.start();

        // Start periodic broadcaster for owned keys (heartbeat/sync)
        broadcaster = Executors.newSingleThreadScheduledExecutor();
        broadcaster.scheduleAtFixedRate(this::broadcastOwnedKeys, 1000, 1000, TimeUnit.MILLISECONDS);

        state = LifecycleState.RUNNING;
        logger.info("DSM started for node {}", nodeId);
    }

    @Override
    public void stop() throws LifecycleException {
        running = false;
        if (broadcaster != null) {
            broadcaster.shutdownNow();
        }
        transport.stop();
        state = LifecycleState.STOPPED;
        logger.info("DSM stopped");
    }

    @Override
    public void shutdown() throws LifecycleException {
        stop();
        storage.clear();
        state = LifecycleState.DESTROYED;
    }

    @Override
    public void pause() {
        if (state == LifecycleState.RUNNING) {
            state = LifecycleState.STOPPED;
        }
    }

    @Override
    public void resume() {
        if (state == LifecycleState.STOPPED) {
            state = LifecycleState.RUNNING;
        }
    }

    @Override
    public boolean isRunning() {
        return state == LifecycleState.RUNNING;
    }

    public LifecycleState getState() {
        return state;
    }

    /**
     * Puts a value into the distributed map.
     * Use this ONLY for keys that this node "owns" or is authoritative for.
     * 
     * @param key   the key
     * @param value the value (must be serializable to JSON)
     */
    public void put(String key, Object value) {
        DistributedEntry entry = new DistributedEntry(key, value, nodeId, System.currentTimeMillis());
        storage.put(key, entry);
        broadcastEntry(entry);
    }

    /**
     * Gets a value locally.
     * 
     * @param key the key
     * @return the value, or null if not present
     */
    public Object get(String key) {
        DistributedEntry entry = storage.get(key);
        return entry != null ? entry.value : null;
    }

    /**
     * Gets the full entry (value + metadata).
     * 
     * @param key the key
     * @return the entry
     */
    public DistributedEntry getEntry(String key) {
        return storage.get(key);
    }

    /**
     * Finds all entries whose keys start with the given prefix.
     * 
     * @param prefix the key prefix
     * @return list of matching entries
     */
    public java.util.List<DistributedEntry> findEntries(String prefix) {
        return storage.values().stream()
                .filter(e -> e.key.startsWith(prefix))
                .collect(java.util.stream.Collectors.toList());
    }

    private void broadcastEntry(DistributedEntry entry) {
        if (!running)
            return;
        try {
            String json = mapper.writeValueAsString(entry);
            transport.send(json);
        } catch (Exception e) {
            logger.error("Failed to broadcast entry: {}", entry.key, e);
        }
    }

    private void broadcastOwnedKeys() {
        storage.values().stream()
                .filter(e -> nodeId.equals(e.owner))
                .forEach(this::broadcastEntry);
    }

    // Callback from Transport
    private void processIncomingUpdate(String json) {
        try {
            DistributedEntry newEntry = mapper.readValue(json, DistributedEntry.class);

            // Ignore if we own it (loopback)
            if (newEntry.owner.equals(nodeId))
                return;

            storage.compute(newEntry.key, (k, existing) -> {
                if (existing == null) {
                    return newEntry;
                }
                // Last Write Wins (LWW) resolution based on timestamp
                if (newEntry.timestamp > existing.timestamp) {
                    return newEntry;
                }
                return existing;
            });
        } catch (Exception e) {
            logger.warn("Failed to process incoming DSM update", e);
        }
    }

    /**
     * DTO for a shared memory entry.
     */
    public static class DistributedEntry {
        public String key;
        public Object value;
        public String owner;
        public long timestamp;

        public DistributedEntry() {
        }

        public DistributedEntry(String key, Object value, String owner, long timestamp) {
            this.key = key;
            this.value = value;
            this.owner = owner;
            this.timestamp = timestamp;
        }
    }

    /**
     * Transport abstraction for DSM.
     */
    public interface Transport {
        void initialize(java.util.function.Consumer<String> messageHandler) throws LifecycleException;

        void start() throws LifecycleException;

        void stop() throws LifecycleException;

        void send(String message) throws Exception;
    }

    /**
     * Default UDP Multicast implementation.
     */
    public static class UdpMulticastTransport implements Transport {
        private final int port;
        private final String groupAddress;
        private MulticastSocket socket;
        private InetAddress group;
        private Thread listenerThread;
        private volatile boolean active = false;
        private java.util.function.Consumer<String> handler;

        public UdpMulticastTransport(int port, String groupAddress) {
            this.port = port;
            this.groupAddress = groupAddress;
        }

        @Override
        public void initialize(java.util.function.Consumer<String> messageHandler) throws LifecycleException {
            this.handler = messageHandler;
            try {
                group = InetAddress.getByName(groupAddress);
                socket = new MulticastSocket(port);
                socket.setTimeToLive(1);
                socket.joinGroup(new InetSocketAddress(group, port), null);
            } catch (IOException e) {
                throw new LifecycleException("Failed to init UDP transport", e);
            }
        }

        @Override
        public void start() {
            active = true;
            listenerThread = new Thread(this::listenLoop, "DSM-UDP-Listener");
            listenerThread.setDaemon(true);
            listenerThread.start();
        }

        @Override
        public void stop() {
            active = false;
            if (socket != null && !socket.isClosed()) {
                try {
                    if (group != null) {
                        socket.leaveGroup(new InetSocketAddress(group, port), null);
                    }
                } catch (Exception ignored) {
                }
                socket.close();
            }
        }

        @Override
        public void send(String message) throws Exception {
            byte[] data = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(data, data.length, group, port);
            socket.send(packet);
        }

        private void listenLoop() {
            byte[] buffer = new byte[4096];
            while (active && !socket.isClosed()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength(), java.nio.charset.StandardCharsets.UTF_8);
                    if (handler != null) {
                        handler.accept(msg);
                    }
                } catch (IOException e) {
                    if (active)
                        logger.warn("UDP receive error", e);
                }
            }
        }
    }
}
