/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.sensor.vision;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;
import org.jrobotics.sensor.AbstractSensor;
import org.jrobotics.sensor.SensorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

/**
 * Camera sensor implementation using OpenCV via JavaCV.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.4.0
 */
public class OpenCVCamera extends AbstractSensor<Mat> implements CameraSensor {

    private static final Logger logger = LoggerFactory.getLogger(OpenCVCamera.class);

    private final int deviceId;
    private final OpenCVFrameGrabber grabber;
    private final OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

    private int width = 640;
    private int height = 480;

    /**
     * Creates a new OpenCV Camera.
     * 
     * @param id       the sensor ID
     * @param deviceId the camera device ID (0 for default webcam)
     */
    public OpenCVCamera(String id, int deviceId) {
        super(id, "Camera-" + deviceId, "frame");
        this.deviceId = deviceId;
        this.grabber = new OpenCVFrameGrabber(deviceId);
        configureGrabber();
    }

    private void configureGrabber() {
        grabber.setImageWidth(width);
        grabber.setImageHeight(height);
        // Default format: BGR
    }

    @Override
    protected void doStart() throws Exception {
        logger.info("Starting camera device {}", deviceId);
        grabber.start();
        setSampleRate(grabber.getFrameRate());
    }

    @Override
    protected void doStop() throws Exception {
        logger.info("Stopping camera device {}", deviceId);
        grabber.stop();
        grabber.release();
    }

    @Override
    protected Mat doRead(long timeoutMs) throws Exception {
        Frame frame = grabber.grab();
        if (frame == null) {
            return null;
        }

        // Convert to Mat
        // Clone is necessary because the converter reuses the Mat wrapper
        Mat mat = converter.convert(frame);
        return mat != null ? mat.clone() : null;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setResolution(int width, int height) {
        this.width = width;
        this.height = height;
        if (isRunning()) {
            logger.warn("Changing resolution while running requires restart");
            try {
                doStop();
                configureGrabber();
                doStart();
            } catch (Exception e) {
                logger.error("Failed to restart camera after resolution change", e);
            }
        } else {
            configureGrabber();
        }
    }

    @Override
    public void snapshot(String filepath) {
        try {
            Mat frame = read().orElseThrow(() -> new SensorException(getId(), "Could not grab frame for snapshot"));
            boolean success = imwrite(filepath, frame);
            if (!success) {
                throw new SensorException(getId(), "Failed to write image to " + filepath);
            }
            logger.info("Snapshot saved to {}", filepath);
        } catch (Exception e) {
            logger.error("Snapshot failed: {}", e.getMessage());
        }
    }

    @Override
    public void doCalibrate() throws SensorException {
        // Calibration logic (chessboard etc.) could go here
        logger.info("Camera calibration not implemented yet");
    }
}
