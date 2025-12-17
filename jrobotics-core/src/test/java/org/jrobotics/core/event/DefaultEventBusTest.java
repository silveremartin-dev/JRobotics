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

import org.jrobotics.core.Subscription;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DefaultEventBus}.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class DefaultEventBusTest {

    private DefaultEventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new DefaultEventBus();
    }

    @AfterEach
    void tearDown() {
        eventBus.shutdown();
    }

    @Nested
    @DisplayName("Subscribe Tests")
    class SubscribeTests {

        @Test
        @DisplayName("should receive events after subscribing")
        void shouldReceiveEventsAfterSubscribing() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<TestEvent> received = new AtomicReference<>();

            eventBus.subscribe(TestEvent.class, event -> {
                received.set(event);
                latch.countDown();
            });

            TestEvent sent = new TestEvent("test-source");
            eventBus.publish(sent);

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertEquals(sent, received.get());
        }

        @Test
        @DisplayName("should return active subscription")
        void shouldReturnActiveSubscription() {
            Subscription subscription = eventBus.subscribe(TestEvent.class, event -> {
            });
            assertTrue(subscription.isActive());
            assertEquals(TestEvent.class, subscription.getEventType());
        }

        @Test
        @DisplayName("should not receive events after unsubscribe")
        void shouldNotReceiveEventsAfterUnsubscribe() throws InterruptedException {
            AtomicInteger count = new AtomicInteger(0);

            Subscription subscription = eventBus.subscribe(TestEvent.class, event -> count.incrementAndGet());
            subscription.unsubscribe();

            eventBus.publishSync(new TestEvent("test"));

            assertEquals(0, count.get());
            assertFalse(subscription.isActive());
        }
    }

    @Nested
    @DisplayName("Priority Tests")
    class PriorityTests {

        @Test
        @DisplayName("should call higher priority handlers first")
        void shouldCallHigherPriorityFirst() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(2);
            AtomicInteger order = new AtomicInteger(0);
            AtomicInteger firstOrder = new AtomicInteger(-1);
            AtomicInteger secondOrder = new AtomicInteger(-1);

            // Lower priority (called second)
            eventBus.subscribe(TestEvent.class, event -> {
                secondOrder.set(order.incrementAndGet());
                latch.countDown();
            }, 1);

            // Higher priority (called first)
            eventBus.subscribe(TestEvent.class, event -> {
                firstOrder.set(order.incrementAndGet());
                latch.countDown();
            }, 10);

            eventBus.publish(new TestEvent("test"));

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertEquals(1, firstOrder.get());
            assertEquals(2, secondOrder.get());
        }
    }

    @Nested
    @DisplayName("Sync Publish Tests")
    class SyncPublishTests {

        @Test
        @DisplayName("publishSync should block until handlers complete")
        void publishSyncShouldBlock() {
            AtomicInteger count = new AtomicInteger(0);

            eventBus.subscribe(TestEvent.class, event -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                count.incrementAndGet();
            });

            eventBus.publishSync(new TestEvent("test"));

            // Should be 1 immediately after publishSync returns
            assertEquals(1, count.get());
        }
    }

    @Nested
    @DisplayName("Clear Subscriptions Tests")
    class ClearSubscriptionsTests {

        @Test
        @DisplayName("clearSubscriptions should remove handlers for specific type")
        void clearSubscriptionsShouldRemoveHandlers() {
            AtomicInteger count = new AtomicInteger(0);

            eventBus.subscribe(TestEvent.class, event -> count.incrementAndGet());
            eventBus.clearSubscriptions(TestEvent.class);

            eventBus.publishSync(new TestEvent("test"));

            assertEquals(0, count.get());
        }

        @Test
        @DisplayName("clearAllSubscriptions should remove all handlers")
        void clearAllSubscriptionsShouldRemoveAllHandlers() {
            AtomicInteger count = new AtomicInteger(0);

            eventBus.subscribe(TestEvent.class, event -> count.incrementAndGet());
            eventBus.subscribe(AnotherTestEvent.class, event -> count.incrementAndGet());
            eventBus.clearAllSubscriptions();

            eventBus.publishSync(new TestEvent("test"));
            eventBus.publishSync(new AnotherTestEvent("test"));

            assertEquals(0, count.get());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should continue dispatching after handler throws")
        void shouldContinueAfterHandlerThrows() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicInteger count = new AtomicInteger(0);

            // First handler throws
            eventBus.subscribe(TestEvent.class, event -> {
                throw new RuntimeException("Test exception");
            }, 10);

            // Second handler should still be called
            eventBus.subscribe(TestEvent.class, event -> {
                count.incrementAndGet();
                latch.countDown();
            }, 1);

            eventBus.publish(new TestEvent("test"));

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertEquals(1, count.get());
        }
    }

    /**
     * Test event class.
     */
    private static class TestEvent extends AbstractEvent {
        TestEvent(String sourceId) {
            super(sourceId);
        }
    }

    /**
     * Another test event class for type testing.
     */
    private static class AnotherTestEvent extends AbstractEvent {
        AnotherTestEvent(String sourceId) {
            super(sourceId);
        }
    }
}
