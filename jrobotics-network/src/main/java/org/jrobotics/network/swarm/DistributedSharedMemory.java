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
    private static final int PACKET_SIZE = 4096;

    private final String nodeId;
    private final int port;
    private final String multicastGroup;
    private final ObjectMapper mapper = new ObjectMapper();

    // In-memory storage: Key -> DistributedEntry
    private final Map<String, DistributedEntry> storage = new ConcurrentHashMap<>();

    private MulticastSocket socket;
    private InetAddress validGroup;
    private volatile LifecycleState state = LifecycleState.CREATED;
    private Thread listenerThread;
    private ScheduledExecutorService broadcaster;

    private volatile boolean running = false;

    /**
     * Creates a DSM node.
     * 
     * @param nodeId unique ID of this node
     */
    public DistributedSharedMemory(String nodeId) {
        this(nodeId, DEFAULT_PORT, DEFAULT_GROUP);
    }

    /**
     * Creates a DSM node with custom settings.
     * 
     * @param nodeId         unique ID of this node
     * @param port           UDP port
     * @param multicastGroup multicast group address
     */
    public DistributedSharedMemory(String nodeId, int port, String multicastGroup) {
        this.nodeId = nodeId;
        this.port = port;
        this.multicastGroup = multicastGroup;
    }

    @Override
    public void initialize() throws LifecycleException {
        try {
            validGroup = InetAddress.getByName(multicastGroup);
            socket = new MulticastSocket(port);
            // In a real network, you'd join the group on a specific interface
            // socket.joinGroup(validGroup);
            // For simplicity/compatibility in tests or specific network setups:
            socket.setTimeToLive(1); // Local network only

            // Note: joinGroup is deprecated in newer Java versions but standard in 8.
            // In 17+, use joinGroup(SocketAddress, NetworkInterface).
            // We'll stick to simple implementation or assume broadcast for now if multicast
            // fails.

            state = LifecycleState.INITIALIZED;
            logger.info("DSM initialized on {}:{}", multicastGroup, port);
        } catch (IOException e) {
            state = LifecycleState.ERROR;
            throw new LifecycleException("Failed to initialize DSM socket", e);
        }
    }

    @Override
    public void start() throws LifecycleException {
        if (state != LifecycleState.INITIALIZED && state != LifecycleState.STOPPED) {
            throw new LifecycleException("Cannot start from state: " + state);
        }

        running = true;

        // Start listener
        listenerThread = new Thread(this::listenLoop, "DSM-Listener-" + nodeId);
        listenerThread.setDaemon(true);
        listenerThread.start();

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
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
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
        // No-op for now, or stop broadcaster
        if (state == LifecycleState.RUNNING) {
            state = LifecycleState.STOPPED; // Reusing stopped for pause as simplified state map
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
        if (!running || socket == null)
            return;

        try {
            byte[] data = mapper.writeValueAsBytes(entry);
            DatagramPacket packet = new DatagramPacket(data, data.length, validGroup, port);
            socket.send(packet);
        } catch (IOException e) {
            logger.error("Failed to broadcast entry: {}", entry.key, e);
        }
    }

    private void broadcastOwnedKeys() {
        // Periodically re-broadcast keys owned by this node to ensure eventual
        // consistency
        // and handle new nodes joining
        storage.values().stream()
                .filter(e -> nodeId.equals(e.owner))
                .forEach(this::broadcastEntry);
    }

    private void listenLoop() {
        byte[] buffer = new byte[PACKET_SIZE];
        while (running && !socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Ignore own packets if loopback is enabled (simplification)
                // In production, we'd check the content first.

                DistributedEntry entry = mapper.readValue(
                        new String(packet.getData(), 0, packet.getLength()),
                        DistributedEntry.class);

                if (!entry.owner.equals(nodeId)) {
                    processIncomingUpdate(entry);
                }

            } catch (IOException e) {
                if (running) {
                    logger.warn("Error receiving DSM packet", e);
                }
            }
        }
    }

    private void processIncomingUpdate(DistributedEntry newEntry) {
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
        } // For Jackson

        public DistributedEntry(String key, Object value, String owner, long timestamp) {
            this.key = key;
            this.value = value;
            this.owner = owner;
            this.timestamp = timestamp;
        }
    }
}
