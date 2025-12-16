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

import org.bytedeco.opencv.opencv_core.Mat;
import org.jrobotics.sensor.Sensor;

/**
 * Interface for camera sensors.
 * 
 * <p>
 * Produces OpenCV {@link Mat} frames.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.4.0
 */
public interface CameraSensor extends Sensor<Mat> {

    /**
     * Gets the camera width resolution.
     * 
     * @return width in pixels
     */
    int getWidth();

    /**
     * Gets the camera height resolution.
     * 
     * @return height in pixels
     */
    int getHeight();

    /**
     * Sets the camera resolution.
     * 
     * @param width  width in pixels
     * @param height height in pixels
     */
    void setResolution(int width, int height);

    /**
     * Snapshots a frame and saves it to a file.
     * 
     * @param filepath destination path
     */
    void snapshot(String filepath);
}
