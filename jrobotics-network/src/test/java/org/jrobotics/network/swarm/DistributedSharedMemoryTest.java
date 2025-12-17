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

import org.jrobotics.core.LifecycleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class DistributedSharedMemoryTest {

    private DistributedSharedMemory nodeA;
    private DistributedSharedMemory nodeB;
    private MockTransport transportA;
    private MockTransport transportB;
    private NetworkSimulator network;

    @BeforeEach
    void setUp() throws LifecycleException {
        network = new NetworkSimulator();

        transportA = new MockTransport(network);
        transportB = new MockTransport(network);

        nodeA = new DistributedSharedMemory("node-a", transportA);
        nodeB = new DistributedSharedMemory("node-b", transportB);

        nodeA.initialize();
        nodeB.initialize();
    }

    @AfterEach
    void tearDown() throws LifecycleException {
        if (nodeA != null)
            nodeA.shutdown();
        if (nodeB != null)
            nodeB.shutdown();
    }

    @Test
    void testLocalPutAndGet() throws LifecycleException {
        nodeA.start();
        nodeA.put("key1", "value1");

        assertEquals("value1", nodeA.get("key1"));
        assertEquals("node-a", nodeA.getEntry("key1").owner);
    }

    @Test
    @Timeout(5)
    void testReplication() throws LifecycleException {
        // Start both nodes
        nodeA.start();
        nodeB.start();

        // Put in A
        nodeA.put("shared-key", "hello-swarm");

        // B should receive it eventually (via mock network)
        await().atMost(2, TimeUnit.SECONDS).until(() -> "hello-swarm".equals(nodeB.get("shared-key")));

        assertEquals("hello-swarm", nodeB.get("shared-key"));
        assertEquals("node-a", nodeB.getEntry("shared-key").owner);
    }

    @Test
    void testLastWriteWins() throws LifecycleException, InterruptedException {
        nodeA.start();
        nodeB.start();

        // A writes
        nodeA.put("conflict-key", "version-a");
        await().atMost(1, TimeUnit.SECONDS).until(() -> "version-a".equals(nodeB.get("conflict-key")));

        // Use a small sleep to ensure timestamp difference for LWW
        Thread.sleep(10);

        // B writes same key (timestamp should be higher)
        nodeB.put("conflict-key", "version-b");

        // A should eventually see B's update
        await().atMost(1, TimeUnit.SECONDS).until(() -> "version-b".equals(nodeA.get("conflict-key")));

        // Ensure consistency
        assertEquals("version-b", nodeA.get("conflict-key"));
        assertEquals("version-b", nodeB.get("conflict-key"));
    }

    // --- Helper Classes ---

    static class NetworkSimulator {
        final List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();

        void subscribe(Consumer<String> listener) {
            listeners.add(listener);
        }

        void subscribeAndUnsubscribe(Consumer<String> listener) {
            // simplified for mock
        }

        void broadcast(String message) {
            for (Consumer<String> listener : listeners) {
                // Async delivery to simulate network delay?
                // Or sync for simplicity. Sync is fine for functional test.
                listener.accept(message);
            }
        }
    }

    static class MockTransport implements DistributedSharedMemory.Transport {
        private final NetworkSimulator network;
        private Consumer<String> handler;

        MockTransport(NetworkSimulator network) {
            this.network = network;
        }

        @Override
        public void initialize(Consumer<String> messageHandler) {
            this.handler = messageHandler;
            network.subscribe(msg -> {
                if (handler != null)
                    handler.accept(msg);
            });
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void send(String message) {
            network.broadcast(message);
        }
    }
}
