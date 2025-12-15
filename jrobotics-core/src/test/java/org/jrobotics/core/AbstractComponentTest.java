/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AbstractComponent}.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class AbstractComponentTest {

    private TestComponent component;

    @BeforeEach
    void setUp() {
        component = new TestComponent("test-1", "Test Component");
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {

        @Test
        @DisplayName("should start in CREATED state")
        void shouldStartInCreatedState() {
            assertEquals(LifecycleState.CREATED, component.getState());
        }

        @Test
        @DisplayName("should transition to INITIALIZED after initialize()")
        void shouldTransitionToInitialized() {
            component.initialize();
            assertEquals(LifecycleState.INITIALIZED, component.getState());
        }

        @Test
        @DisplayName("should transition to RUNNING after start()")
        void shouldTransitionToRunning() {
            component.initialize();
            component.start();
            assertEquals(LifecycleState.RUNNING, component.getState());
            assertTrue(component.isRunning());
        }

        @Test
        @DisplayName("should transition to PAUSED after pause()")
        void shouldTransitionToPaused() {
            component.initialize();
            component.start();
            component.pause();
            assertEquals(LifecycleState.PAUSED, component.getState());
            assertFalse(component.isRunning());
        }

        @Test
        @DisplayName("should transition to RUNNING after resume()")
        void shouldTransitionToRunningAfterResume() {
            component.initialize();
            component.start();
            component.pause();
            component.resume();
            assertEquals(LifecycleState.RUNNING, component.getState());
            assertTrue(component.isRunning());
        }

        @Test
        @DisplayName("should transition to STOPPED after stop()")
        void shouldTransitionToStopped() {
            component.initialize();
            component.start();
            component.stop();
            assertEquals(LifecycleState.STOPPED, component.getState());
        }

        @Test
        @DisplayName("should transition to DESTROYED after shutdown()")
        void shouldTransitionToDestroyed() {
            component.initialize();
            component.start();
            component.stop();
            component.shutdown();
            assertEquals(LifecycleState.DESTROYED, component.getState());
        }

        @Test
        @DisplayName("should throw when initializing non-CREATED component")
        void shouldThrowWhenInitializingRunningComponent() {
            component.initialize();
            assertThrows(LifecycleException.class, () -> component.initialize());
        }

        @Test
        @DisplayName("should throw when starting non-initialized component")
        void shouldThrowWhenStartingNonInitializedComponent() {
            assertThrows(LifecycleException.class, () -> component.start());
        }

        @Test
        @DisplayName("should throw when pausing non-running component")
        void shouldThrowWhenPausingNonRunningComponent() {
            component.initialize();
            assertThrows(LifecycleException.class, () -> component.pause());
        }

        @Test
        @DisplayName("should throw when resuming non-paused component")
        void shouldThrowWhenResumingNonPausedComponent() {
            component.initialize();
            component.start();
            assertThrows(LifecycleException.class, () -> component.resume());
        }
    }

    @Nested
    @DisplayName("Identity Tests")
    class IdentityTests {

        @Test
        @DisplayName("should return correct ID")
        void shouldReturnCorrectId() {
            assertEquals("test-1", component.getId());
        }

        @Test
        @DisplayName("should return correct name")
        void shouldReturnCorrectName() {
            assertEquals("Test Component", component.getName());
        }

        @Test
        @DisplayName("should return correct type")
        void shouldReturnCorrectType() {
            assertEquals(ComponentType.OTHER, component.getType());
        }
    }

    @Nested
    @DisplayName("Enable/Disable Tests")
    class EnableDisableTests {

        @Test
        @DisplayName("should be enabled by default")
        void shouldBeEnabledByDefault() {
            assertTrue(component.isEnabled());
        }

        @Test
        @DisplayName("should be disabled after setEnabled(false)")
        void shouldBeDisabledAfterSetEnabledFalse() {
            component.setEnabled(false);
            assertFalse(component.isEnabled());
        }
    }

    @Nested
    @DisplayName("Robot Association Tests")
    class RobotAssociationTests {

        @Test
        @DisplayName("should have no robot initially")
        void shouldHaveNoRobotInitially() {
            assertNull(component.getRobot());
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("components with same ID should be equal")
        void componentsWithSameIdShouldBeEqual() {
            TestComponent other = new TestComponent("test-1", "Different Name");
            assertEquals(component, other);
        }

        @Test
        @DisplayName("components with different ID should not be equal")
        void componentsWithDifferentIdShouldNotBeEqual() {
            TestComponent other = new TestComponent("test-2", "Test Component");
            assertNotEquals(component, other);
        }

        @Test
        @DisplayName("hashCode should be consistent with equals")
        void hashCodeShouldBeConsistent() {
            TestComponent other = new TestComponent("test-1", "Different Name");
            assertEquals(component.hashCode(), other.hashCode());
        }
    }

    /**
     * Test implementation of AbstractComponent.
     */
    private static class TestComponent extends AbstractComponent {
        TestComponent(String id, String name) {
            super(id, name, ComponentType.OTHER);
        }
    }
}
