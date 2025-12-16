/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.sensor.voice;

import org.jrobotics.sensor.AbstractSensor;
import org.jrobotics.sensor.SensorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.IOException;

/**
 * Voice sensor implementation using Vosk offline recognition.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.4.0
 */
public class VoskVoiceSensor extends AbstractSensor<String> implements VoiceSensor {

    private static final Logger logger = LoggerFactory.getLogger(VoskVoiceSensor.class);

    private final String modelPath;
    private Model model;
    private Recognizer recognizer;
    private TargetDataLine microphone;
    private Thread captureThread;

    private volatile boolean capturing = false;

    /**
     * Creates a new Vosk Voice Sensor.
     * 
     * @param id        the sensor ID
     * @param modelPath path to the Vosk model directory
     */
    public VoskVoiceSensor(String id, String modelPath) {
        super(id, "Voice-" + id, "text");
        this.modelPath = modelPath;
    }

    @Override
    protected void doStart() throws Exception {
        logger.info("Initializing Vosk model from: {}", modelPath);
        // LibVosk.setLogLevel(LogLevel.WARNING);

        try {
            this.model = new Model(modelPath);
            this.recognizer = new Recognizer(model, 16000); // 16kHz
        } catch (IOException e) {
            throw new SensorException(getId(), "Failed to load model: " + e.getMessage(), e);
        }

        // Setup microphone
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new SensorException(getId(), "Microphone not supported");
        }

        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        capturing = true;
        captureThread = new Thread(this::captureLoop, "Voice-Capture-" + getId());
        captureThread.start();
    }

    private volatile org.jrobotics.sensor.SensorDataListener<String> localListener;

    @Override
    public void startStreaming(org.jrobotics.sensor.SensorDataListener<String> listener) {
        this.localListener = listener;
        super.startStreaming(listener);
    }

    private void captureLoop() {
        byte[] buffer = new byte[4096];
        logger.info("Voice capture started");

        while (capturing && microphone.isOpen()) {
            int nbytes = microphone.read(buffer, 0, buffer.length);
            if (nbytes > 0) {
                if (recognizer.acceptWaveForm(buffer, nbytes)) {
                    String result = recognizer.getResult();
                    String text = parseText(result);
                    if (!text.isEmpty()) {
                        logger.debug("Recognized: {}", text);
                        if (localListener != null) {
                            localListener.onData(getId(), text, System.nanoTime());
                        }
                    }
                }
            }
        }
    }

    // Simplified JSON parsing for Vosk result
    private String parseText(String json) {
        int idx = json.indexOf("\"text\" :");
        if (idx != -1) {
            int start = json.indexOf("\"", idx + 8);
            int end = json.indexOf("\"", start + 1);
            if (start != -1 && end != -1) {
                return json.substring(start + 1, end).trim();
            }
        }
        return "";
    }

    /**
     * In this implementation, read() returns the last recognized partial result or
     * final result.
     * But since recognition is push-based (audio stream), pulling is tricky.
     * We'll implement read() to peek current final result if available.
     */
    @Override
    protected String doRead(long timeoutMs) throws Exception {
        // For polling usage, this might need redesign.
        // Usually voice is event-driven.
        // We will return the last recognized complete phrase from the recognizer if
        // any.
        // For now, returning null as streaming is preferred.
        return null;
    }

    @Override
    protected void doStop() throws Exception {
        capturing = false;
        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }
        if (recognizer != null) {
            recognizer.close();
        }
        if (model != null) {
            model.close();
        }
    }

    @Override
    public String getModelPath() {
        return modelPath;
    }

    @Override
    public boolean isReady() {
        return model != null && recognizer != null;
    }
}
