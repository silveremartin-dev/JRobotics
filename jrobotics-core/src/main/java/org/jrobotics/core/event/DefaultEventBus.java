/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.core.event;

import org.jrobotics.core.Event;
import org.jrobotics.core.EventBus;
import org.jrobotics.core.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Default implementation of {@link EventBus} using concurrent collections
 * and a dedicated thread pool for async event dispatch.
 * 
 * <p>
 * This implementation is thread-safe and supports priority-based handler
 * ordering.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class DefaultEventBus implements EventBus {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEventBus.class);
    private static final int DEFAULT_SHUTDOWN_TIMEOUT_SECONDS = 10;

    private final Map<Class<? extends Event>, List<HandlerEntry<?>>> handlers;
    private final ExecutorService executor;
    private final AtomicBoolean running;

    /**
     * Constructs a new event bus with default settings.
     */
    public DefaultEventBus() {
        this(Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "EventBus-Worker");
            t.setDaemon(true);
            return t;
        }));
    }

    /**
     * Constructs a new event bus with a custom executor.
     * 
     * @param executor the executor for async event dispatch
     */
    public DefaultEventBus(ExecutorService executor) {
        this.handlers = new ConcurrentHashMap<>();
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
        this.running = new AtomicBoolean(true);
        logger.info("[{}] EventBus initialized", System.currentTimeMillis());
    }

    @Override
    public <T extends Event> Subscription subscribe(Class<T> eventType, Consumer<T> handler) {
        return subscribe(eventType, handler, 0);
    }

    @Override
    public <T extends Event> Subscription subscribe(Class<T> eventType, Consumer<T> handler, int priority) {
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(handler, "handler must not be null");

        HandlerEntry<T> entry = new HandlerEntry<>(eventType, handler, priority);

        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(entry);

        // Sort by priority (highest first)
        handlers.get(eventType).sort((a, b) -> Integer.compare(b.priority, a.priority));

        logger.debug("[{}] Subscribed to {} with priority {}",
                System.currentTimeMillis(), eventType.getSimpleName(), priority);

        return new DefaultSubscription(entry, handlers.get(eventType));
    }

    @Override
    public void publish(Event event) {
        if (!running.get()) {
            logger.warn("[{}] EventBus is shutdown, ignoring event: {}",
                    System.currentTimeMillis(), event.getEventType());
            return;
        }

        Objects.requireNonNull(event, "event must not be null");

        executor.submit(() -> dispatchEvent(event));
    }

    @Override
    public void publishSync(Event event) {
        if (!running.get()) {
            logger.warn("[{}] EventBus is shutdown, ignoring event: {}",
                    System.currentTimeMillis(), event.getEventType());
            return;
        }

        Objects.requireNonNull(event, "event must not be null");
        dispatchEvent(event);
    }

    @SuppressWarnings("unchecked")
    private void dispatchEvent(Event event) {
        Class<?> eventClass = event.getClass();

        // Dispatch to handlers of this exact type and all parent types
        while (eventClass != null && Event.class.isAssignableFrom(eventClass)) {
            List<HandlerEntry<?>> entryList = handlers.get(eventClass);

            if (entryList != null) {
                for (HandlerEntry<?> entry : entryList) {
                    try {
                        if (entry.active.get()) {
                            ((Consumer<Event>) entry.handler).accept(event);
                        }
                    } catch (Exception e) {
                        logger.error("[{}] Error dispatching event {} to handler: {}",
                                System.currentTimeMillis(), event.getEventType(), e.getMessage(), e);
                    }
                }
            }

            eventClass = eventClass.getSuperclass();
        }
    }

    @Override
    public void clearSubscriptions(Class<? extends Event> eventType) {
        List<HandlerEntry<?>> removed = handlers.remove(eventType);
        if (removed != null) {
            removed.forEach(entry -> entry.active.set(false));
            logger.debug("[{}] Cleared {} subscriptions for {}",
                    System.currentTimeMillis(), removed.size(), eventType.getSimpleName());
        }
    }

    @Override
    public void clearAllSubscriptions() {
        handlers.values().forEach(list -> list.forEach(entry -> entry.active.set(false)));
        handlers.clear();
        logger.info("[{}] Cleared all event subscriptions", System.currentTimeMillis());
    }

    @Override
    public void shutdown() {
        if (running.compareAndSet(true, false)) {
            logger.info("[{}] Shutting down EventBus", System.currentTimeMillis());
            clearAllSubscriptions();

            executor.shutdown();
            try {
                if (!executor.awaitTermination(DEFAULT_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    logger.warn("[{}] EventBus executor did not terminate gracefully",
                            System.currentTimeMillis());
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            logger.info("[{}] EventBus shutdown complete", System.currentTimeMillis());
        }
    }

    /**
     * Internal handler entry class.
     */
    private static class HandlerEntry<T extends Event> {
        final Class<T> eventType;
        final Consumer<T> handler;
        final int priority;
        final AtomicBoolean active;

        HandlerEntry(Class<T> eventType, Consumer<T> handler, int priority) {
            this.eventType = eventType;
            this.handler = handler;
            this.priority = priority;
            this.active = new AtomicBoolean(true);
        }
    }

    /**
     * Default subscription implementation.
     */
    private static class DefaultSubscription implements Subscription {
        private final HandlerEntry<?> entry;
        private final List<HandlerEntry<?>> list;

        DefaultSubscription(HandlerEntry<?> entry, List<HandlerEntry<?>> list) {
            this.entry = entry;
            this.list = list;
        }

        @Override
        public void unsubscribe() {
            entry.active.set(false);
            list.remove(entry);
        }

        @Override
        public boolean isActive() {
            return entry.active.get();
        }

        @Override
        public Class<? extends Event> getEventType() {
            return entry.eventType;
        }
    }
}
