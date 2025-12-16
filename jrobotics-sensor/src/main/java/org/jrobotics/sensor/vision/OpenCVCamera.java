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

/*
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;
*/
import org.jrobotics.sensor.AbstractSensor;
import org.jrobotics.sensor.SensorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

/**
 * Camera sensor stub (OpenCV disabled).
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.4.0
 */
public class OpenCVCamera extends AbstractSensor<Object> implements CameraSensor {

    private static final Logger logger = LoggerFactory.getLogger(OpenCVCamera.class);

    private final int deviceId;
    // private final OpenCVFrameGrabber grabber;
    // private final OpenCVFrameConverter.ToMat converter = new
    // OpenCVFrameConverter.ToMat();

    private int width = 640;
    private int height = 480;

    public OpenCVCamera(String id, int deviceId) {
        super(id, "Camera-" + deviceId, "frame");
        this.deviceId = deviceId;
        // this.grabber = new OpenCVFrameGrabber(deviceId);
        logger.warn("OpenCVCamera is currently disabled due to dependency issues");
    }

    @Override
    protected void doStart() throws Exception {
        // grabber.start();
    }

    @Override
    protected void doStop() throws Exception {
        // grabber.stop();
    }

    @Override
    protected Object doRead(long timeoutMs) throws Exception {
        // return null for now
        return null;
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
    }

    @Override
    public void snapshot(String filepath) {
        logger.warn("Snapshot disabled");
    }

    @Override
    public void doCalibrate() throws SensorException {
    }
}
