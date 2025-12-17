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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LifecycleTest {

    static class TestComponent implements Lifecycle {
        private LifecycleState state = LifecycleState.CREATED;

        @Override
        public void initialize() throws LifecycleException {
            state = LifecycleState.INITIALIZED;
        }

        @Override
        public void start() throws LifecycleException {
            if (state != LifecycleState.INITIALIZED && state != LifecycleState.STOPPED) {
                throw new LifecycleException("Invalid state");
            }
            state = LifecycleState.RUNNING;
        }

        @Override
        public void stop() throws LifecycleException {
            state = LifecycleState.STOPPED;
        }

        @Override
        public void shutdown() throws LifecycleException {
            state = LifecycleState.DESTROYED;
        }

        @Override
        public void pause() throws LifecycleException {
            state = LifecycleState.PAUSED;
        }

        @Override
        public void resume() throws LifecycleException {
            state = LifecycleState.RUNNING;
        }

        @Override
        public boolean isRunning() {
            return state == LifecycleState.RUNNING;
        }

        public LifecycleState getState() {
            return state;
        }
    }

    @Test
    void testStandardLifecycle() throws LifecycleException {
        TestComponent comp = new TestComponent();
        assertEquals(LifecycleState.CREATED, comp.getState());

        comp.initialize();
        assertEquals(LifecycleState.INITIALIZED, comp.getState());

        comp.start();
        assertEquals(LifecycleState.RUNNING, comp.getState());
        assertTrue(comp.isRunning());

        comp.pause();
        assertEquals(LifecycleState.PAUSED, comp.getState());

        comp.resume();
        assertEquals(LifecycleState.RUNNING, comp.getState());

        comp.stop();
        assertEquals(LifecycleState.STOPPED, comp.getState());
        assertFalse(comp.isRunning());

        comp.shutdown();
        assertEquals(LifecycleState.DESTROYED, comp.getState());
    }
}
