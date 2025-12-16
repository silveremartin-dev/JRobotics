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

import org.jrobotics.core.LifecycleException;
import org.jrobotics.core.LifecycleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract base implementation of {@link NetworkNode}.
 * 
 * <p>Provides message handling, handler registration, and lifecycle management.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public abstract class AbstractNetworkNode implements NetworkNode {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractNetworkNode.class);
    
    private final String nodeId;
    private final Map<MessageType, List<MessageHandler>> handlers = new ConcurrentHashMap<>();
    private final Set<String> connectedPeers = ConcurrentHashMap.newKeySet();
    private final AtomicReference<LifecycleState> state = new AtomicReference<>(LifecycleState.CREATED);
    private final ExecutorService handlerExecutor;
    
    /**
     * Constructs a new network node.
     * 
     * @param nodeId the unique node identifier
     */
    protected AbstractNetworkNode(String nodeId) {
        this.nodeId = Objects.requireNonNull(nodeId, "nodeId must not be null");
        this.handlerExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "NetworkHandler-" + nodeId);
            t.setDaemon(true);
            return t;
        });
    }
    
    @Override
    public String getNodeId() {
        return nodeId;
    }
    
    @Override
    public Set<String> getConnectedPeers() {
        return Collections.unmodifiableSet(connectedPeers);
    }
    
    /**
     * Adds a peer to the connected set.
     * 
     * @param peerId the peer ID
     */
    protected void addPeer(String peerId) {
        connectedPeers.add(peerId);
        logger.info("[{}] Peer connected: {}", System.currentTimeMillis(), peerId);
    }
    
    /**
     * Removes a peer from the connected set.
     * 
     * @param peerId the peer ID
     */
    protected void removePeer(String peerId) {
        connectedPeers.remove(peerId);
        logger.info("[{}] Peer disconnected: {}", System.currentTimeMillis(), peerId);
    }
    
    @Override
    public void registerHandler(MessageType type, MessageHandler handler) {
        handlers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(handler);
        logger.debug("[{}] Handler registered for {}", System.currentTimeMillis(), type);
    }
    
    @Override
    public void unregisterHandler(MessageType type, MessageHandler handler) {
        List<MessageHandler> typeHandlers = handlers.get(type);
        if (typeHandlers != null) {
            typeHandlers.remove(handler);
        }
    }
    
    /**
     * Dispatches a received message to registered handlers.
     * 
     * @param message the received message
     */
    protected void dispatchMessage(Message message) {
        // Check if message is for us
        if (!message.isBroadcast() && !nodeId.equals(message.getTargetId())) {
            logger.trace("[{}] Ignoring message not for us: {}", System.currentTimeMillis(), message);
            return;
        }
        
        List<MessageHandler> typeHandlers = handlers.get(message.getType());
        if (typeHandlers != null && !typeHandlers.isEmpty()) {
            for (MessageHandler handler : typeHandlers) {
                handlerExecutor.submit(() -> {
                    try {
                        handler.onMessage(message);
                    } catch (Exception e) {
                        logger.error("[{}] Handler error for {}: {}", 
                                System.currentTimeMillis(), message.getType(), e.getMessage());
                    }
                });
            }
        } else {
            logger.trace("[{}] No handlers for message type: {}", System.currentTimeMillis(), message.getType());
        }
    }
    
    public LifecycleState getState() {
        return state.get();
    }
    
    public boolean isRunning() {
        return state.get() == LifecycleState.RUNNING;
    }
    
    protected void setState(LifecycleState newState) {
        state.set(newState);
    }
    
    @Override
    public void initialize() throws LifecycleException {
        if (state.get() != LifecycleState.CREATED) {
            throw new LifecycleException("Already initialized");
        }
        state.set(LifecycleState.INITIALIZED);
        logger.info("[{}] Network node {} initialized", System.currentTimeMillis(), nodeId);
    }
    
    @Override
    public void start() throws LifecycleException {
        if (!state.get().canStart()) {
            throw new LifecycleException("Cannot start from current state");
        }
        state.set(LifecycleState.RUNNING);
        doStart();
        logger.info("[{}] Network node {} started", System.currentTimeMillis(), nodeId);
    }
    
    @Override
    public void pause() throws LifecycleException {
        state.set(LifecycleState.PAUSED);
    }
    
    @Override
    public void resume() throws LifecycleException {
        state.set(LifecycleState.RUNNING);
    }
    
    @Override
    public void stop() throws LifecycleException {
        disconnect();
        state.set(LifecycleState.STOPPED);
        logger.info("[{}] Network node {} stopped", System.currentTimeMillis(), nodeId);
    }
    
    @Override
    public void shutdown() throws LifecycleException {
        disconnect();
        handlerExecutor.shutdownNow();
        state.set(LifecycleState.DESTROYED);
        logger.info("[{}] Network node {} shut down", System.currentTimeMillis(), nodeId);
    }
    
    /**
     * Template method for starting the network layer.
     * 
     * @throws LifecycleException if start fails
     */
    protected abstract void doStart() throws LifecycleException;
}
