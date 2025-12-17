/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.actuator.servo;

import org.jrobotics.core.AbstractComponent;
import org.jrobotics.core.ComponentType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base implementation of {@link ServoBank}.
 * 
 * <p>
 * Manages the collection of {@link Servo} objects and delegates logic to
 * specific driver implementations.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.5.0
 */
public abstract class AbstractServoBank extends AbstractComponent implements ServoBank {

    private final int channelCount;
    private final Map<Integer, Servo> servos = new ConcurrentHashMap<>();

    @SuppressWarnings("this-escape")
    protected AbstractServoBank(String id, String name, int channelCount) {
        super(id, name, ComponentType.ACTUATOR);
        this.channelCount = channelCount;

        // Initialize servos
        for (int i = 0; i < channelCount; i++) {
            servos.put(i, createServoForChannel(i));
        }
    }

    /**
     * Creates a Servo instance for a specific channel.
     * can be overridden to provide specialized Servo subclasses.
     */
    protected Servo createServoForChannel(int channel) {
        return new BankedServo(getId() + "-s" + channel, "Servo " + channel, this, channel);
    }

    @Override
    public int getChannelCount() {
        return channelCount;
    }

    @Override
    public Servo getServo(int channel) {
        return servos.get(channel);
    }

    // Default implementation for bulk set (can be optimized by subclasses)
    @Override
    public void setPositions(int[] channels, double[] angles) {
        if (channels.length != angles.length) {
            throw new IllegalArgumentException("Channels and angles arrays must have same length");
        }
        for (int i = 0; i < channels.length; i++) {
            setPosition(channels[i], angles[i]);
        }
    }

    /**
     * Inner class representing a Servo that delegates to the bank.
     */
    protected static class BankedServo extends Servo {
        private final AbstractServoBank bank;
        private final int channel;

        public BankedServo(String id, String name, AbstractServoBank bank, int channel) {
            super(id, name);
            this.bank = bank;
            this.channel = channel;
        }

        @Override
        protected void doExecute(ServoCommand command) throws Exception {
            super.doExecute(command); // Update local state

            // Delegate to bank
            bank.setPosition(channel, command.angle());

            // Speed control (if supported by command/bank)
            if (command.speed() < 1.0) {
                // Map normalized speed (0-1) to bank specific units?
                // For now, simple pass-through if feasible, or Bank handles it
                // Note: ServoCommand currently uses normalized speed.
                // We might need to extend ServoCommand or interpret it.
            }
        }

        @Override
        protected void doEmergencyStop() {
            super.doEmergencyStop();
            bank.setEnabled(channel, false);
        }
    }
}
