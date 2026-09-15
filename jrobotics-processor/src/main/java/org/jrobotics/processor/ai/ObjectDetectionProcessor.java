/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor.ai;

import ai.djl.ModelException;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.translator.YoloV5Translator;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Translator;
import org.jrobotics.processor.AbstractProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Object Detection Processor using DJL.
 *
 * <p>
 * Provides real-time object detection for robotics applications using
 * YOLO, SSD, or other detection models. Detects objects and returns
 * bounding boxes with class labels and confidence scores.
 * </p>
 *
 * <p>
 * <b>Supported Models:</b>
 * </p>
 * <ul>
 * <li>YOLOv5 (default)</li>
 * <li>SSD MobileNet</li>
 * <li>Custom ONNX/TorchScript models</li>
 * </ul>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.6.0
 */
public class ObjectDetectionProcessor extends AbstractProcessor<Image, List<ObjectDetectionProcessor.Detection>> {

    private static final Logger logger = LoggerFactory.getLogger(ObjectDetectionProcessor.class);

    private ZooModel<Image, DetectedObjects> model;
    private Predictor<Image, DetectedObjects> predictor;

    // Detection parameters
    private float confidenceThreshold = 0.5f;
    private float nmsThreshold = 0.45f;
    private int inputWidth = 640;
    private int inputHeight = 640;

    // Filter for specific classes (null = all classes)
    private List<String> classFilter = null;

    /**
     * Creates a new ObjectDetectionProcessor.
     *
     * @param id unique identifier
     */
    public ObjectDetectionProcessor(String id) {
        super(id, "ObjectDetection-" + id);
    }

    /**
     * Sets the confidence threshold for detections.
     *
     * @param threshold minimum confidence (0.0 to 1.0)
     */
    public void setConfidenceThreshold(float threshold) {
        this.confidenceThreshold = threshold;
    }

    /**
     * Sets the NMS (Non-Maximum Suppression) threshold.
     *
     * @param threshold NMS threshold
     */
    public void setNmsThreshold(float threshold) {
        this.nmsThreshold = threshold;
    }

    /**
     * Sets a filter to only return specific classes.
     *
     * @param classes list of class names to detect
     */
    public void setClassFilter(List<String> classes) {
        this.classFilter = classes;
    }

    /**
     * Sets input dimensions for the model.
     *
     * @param width  input width
     * @param height input height
     */
    public void setInputDimensions(int width, int height) {
        this.inputWidth = width;
        this.inputHeight = height;
    }

    /**
     * Loads a YOLOv5 model from the model zoo.
     *
     * @throws ModelException if model loading fails
     * @throws IOException    if I/O error occurs
     */
    public void loadYoloV5Model() throws ModelException, IOException {
        closeCurrentModel();
        logger.info("Loading YOLOv5 model...");

        Translator<Image, DetectedObjects> translator = YoloV5Translator.builder()
                .optThreshold(confidenceThreshold)
                .optSynsetArtifactName("coco.names")
                .build();

        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                .setTypes(Image.class, DetectedObjects.class)
                .optApplication(ai.djl.Application.CV.OBJECT_DETECTION)
                .optFilter("backbone", "yolov5s")
                .optTranslator(translator)
                .build();

        model = criteria.loadModel();
        predictor = model.newPredictor();
        logger.info("YOLOv5 model loaded successfully (input: {}x{}, NMS threshold: {})", 
                inputWidth, inputHeight, nmsThreshold);
    }

    /**
     * Loads an SSD model from the model zoo.
     *
     * @throws ModelException if model loading fails
     * @throws IOException    if I/O error occurs
     */
    public void loadSSDModel() throws ModelException, IOException {
        closeCurrentModel();
        logger.info("Loading SSD model...");

        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                .setTypes(Image.class, DetectedObjects.class)
                .optApplication(ai.djl.Application.CV.OBJECT_DETECTION)
                .optFilter("backbone", "mobilenet_v2")
                .build();

        model = criteria.loadModel();
        predictor = model.newPredictor();
        logger.info("SSD model loaded successfully");
    }

    private void closeCurrentModel() {
        if (predictor != null) {
            try { predictor.close(); } catch (Exception ignored) {}
            predictor = null;
        }
        if (model != null) {
            try { model.close(); } catch (Exception ignored) {}
            model = null;
        }
    }

    /**
     * Detects objects in an image.
     *
     * @param image the input image
     * @return list of detections
     * @throws Exception if inference fails
     */
    @Override
    protected List<Detection> doProcess(Image image) throws Exception {
        if (predictor == null) {
            throw new IllegalStateException("Model not loaded. Call loadYoloV5Model() or loadSSDModel() first.");
        }

        long startTime = System.currentTimeMillis();
        DetectedObjects results = predictor.predict(image);
        long elapsed = System.currentTimeMillis() - startTime;

        logger.debug("Detection completed in {}ms, found {} objects", elapsed, results.getNumberOfObjects());

        return convertResults(results, image.getWidth(), image.getHeight());
    }

    private List<Detection> convertResults(DetectedObjects results, int imageWidth, int imageHeight) {
        List<Detection> detections = new ArrayList<>();

        for (DetectedObjects.DetectedObject obj : results.<DetectedObjects.DetectedObject>items()) {
            String className = obj.getClassName();
            double probability = obj.getProbability();

            // Apply confidence threshold
            if (probability < confidenceThreshold) {
                continue;
            }

            // Apply class filter
            if (classFilter != null && !classFilter.contains(className)) {
                continue;
            }

            BoundingBox bbox = obj.getBoundingBox();
            Rectangle rect = bbox.getBounds();

            Detection detection = new Detection();
            detection.className = className;
            detection.confidence = probability;
            detection.x = (int) (rect.getX() * imageWidth);
            detection.y = (int) (rect.getY() * imageHeight);
            detection.width = (int) (rect.getWidth() * imageWidth);
            detection.height = (int) (rect.getHeight() * imageHeight);

            detections.add(detection);
        }

        return detections;
    }

    /**
     * Detects objects from an input stream.
     *
     * @param inputStream the image input stream
     * @return list of detections
     * @throws Exception if detection fails
     */
    public List<Detection> detect(InputStream inputStream) throws Exception {
        Image image = ImageFactory.getInstance().fromInputStream(inputStream);
        return process(image);
    }

    /**
     * Detects objects from a file path.
     *
     * @param imagePath the image file path
     * @return list of detections
     * @throws Exception if detection fails
     */
    public List<Detection> detect(Path imagePath) throws Exception {
        Image image = ImageFactory.getInstance().fromFile(imagePath);
        return process(image);
    }

    /**
     * Finds detections of a specific class.
     *
     * @param image     the input image
     * @param className the class to find
     * @return list of matching detections
     * @throws Exception if detection fails
     */
    public List<Detection> findClass(Image image, String className) throws Exception {
        List<Detection> all = process(image);
        List<Detection> filtered = new ArrayList<>();
        for (Detection d : all) {
            if (d.className.equalsIgnoreCase(className)) {
                filtered.add(d);
            }
        }
        return filtered;
    }

    /**
     * Gets the closest detection of a specific class.
     *
     * @param image     the input image
     * @param className the class to find
     * @return the largest/closest detection, or null if not found
     * @throws Exception if detection fails
     */
    public Detection findClosest(Image image, String className) throws Exception {
        List<Detection> matches = findClass(image, className);
        if (matches.isEmpty())
            return null;

        // Find largest bounding box (proxy for closest)
        Detection closest = matches.get(0);
        int maxArea = closest.width * closest.height;

        for (Detection d : matches) {
            int area = d.width * d.height;
            if (area > maxArea) {
                maxArea = area;
                closest = d;
            }
        }

        return closest;
    }

    /**
     * Checks if a model is loaded.
     *
     * @return true if model is ready
     */
    public boolean isModelLoaded() {
        return predictor != null;
    }

    @Override
    protected void doShutdown() throws Exception {
        if (predictor != null) {
            predictor.close();
            predictor = null;
        }
        if (model != null) {
            model.close();
            model = null;
        }
        super.doShutdown();
        logger.info("Object detection processor shut down");
    }

    /**
     * Represents a detected object.
     */
    public static class Detection {
        /** Class name (e.g., "person", "car"). */
        public String className;

        /** Confidence score (0.0 to 1.0). */
        public double confidence;

        /** Bounding box X coordinate (pixels). */
        public int x;

        /** Bounding box Y coordinate (pixels). */
        public int y;

        /** Bounding box width (pixels). */
        public int width;

        /** Bounding box height (pixels). */
        public int height;

        /** Gets the center X of the bounding box. */
        public int getCenterX() {
            return x + width / 2;
        }

        /** Gets the center Y of the bounding box. */
        public int getCenterY() {
            return y + height / 2;
        }

        /** Gets the area of the bounding box. */
        public int getArea() {
            return width * height;
        }

        @Override
        public String toString() {
            return String.format("%s (%.2f): [%d, %d, %d, %d]",
                    className, confidence, x, y, width, height);
        }
    }
}
