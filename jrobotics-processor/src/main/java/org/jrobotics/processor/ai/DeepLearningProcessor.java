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

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Translator;
import org.jrobotics.processor.AbstractProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Deep Learning Processor using DJL (Deep Java Library).
 *
 * <p>
 * Provides onboard inference capabilities using pre-trained models.
 * Supports image classification out of the box, with extensibility
 * for object detection, semantic segmentation, and custom models.
 * </p>
 *
 * <p>
 * <b>Supported Model Types:</b>
 * </p>
 * <ul>
 * <li>Image Classification (ResNet, MobileNet, etc.)</li>
 * <li>Object Detection (via custom translator)</li>
 * <li>Custom ONNX/PyTorch models</li>
 * </ul>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.6.0
 */
public class DeepLearningProcessor extends AbstractProcessor<Image, Classifications> {

    private static final Logger logger = LoggerFactory.getLogger(DeepLearningProcessor.class);

    private ZooModel<Image, Classifications> model;
    private Predictor<Image, Classifications> predictor;
    private String modelPath;
    private String modelName;
    private int inputWidth = 224;
    private int inputHeight = 224;

    /**
     * Gets the model path.
     * 
     * @return the model path
     */
    public String getModelPath() {
        return modelPath;
    }

    /**
     * Gets the model name.
     * 
     * @return the model name
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Creates a new DeepLearningProcessor.
     *
     * @param id unique identifier
     */
    public DeepLearningProcessor(String id) {
        super(id, "DL-" + id);
    }

    /**
     * Sets the model path for loading a local model.
     *
     * @param modelPath path to the model directory
     * @param modelName name of the model
     */
    public void setModelPath(String modelPath, String modelName) {
        this.modelPath = modelPath;
        this.modelName = modelName;
    }

    /**
     * Sets the input image dimensions.
     *
     * @param width  input width
     * @param height input height
     */
    public void setInputDimensions(int width, int height) {
        this.inputWidth = width;
        this.inputHeight = height;
    }

    /**
     * Loads a pre-trained image classification model from the model zoo.
     *
     * @param application the application type (e.g., "ai.djl.pytorch")
     * @param modelName   the model name (e.g., "resnet18")
     * @throws ModelException if model loading fails
     * @throws IOException    if I/O error occurs
     */
    public void loadZooModel(String application, String modelName) throws ModelException, IOException {
        logger.info("Loading model from zoo: {}/{}", application, modelName);

        Translator<Image, Classifications> translator = ImageClassificationTranslator.builder()
                .addTransform(new Resize(inputWidth, inputHeight))
                .addTransform(new ToTensor())
                .optApplySoftmax(true)
                .build();

        Criteria<Image, Classifications> criteria = Criteria.builder()
                .setTypes(Image.class, Classifications.class)
                .optApplication(ai.djl.Application.CV.IMAGE_CLASSIFICATION)
                .optFilter("backbone", modelName)
                .optTranslator(translator)
                .build();

        try {
            model = criteria.loadModel();
            predictor = model.newPredictor();
            logger.info("Model loaded successfully: {}", model.getName());
        } catch (ModelNotFoundException e) {
            logger.error("Model not found: {}/{}", application, modelName);
            throw e;
        }
    }

    /**
     * Loads a custom model from a local path.
     *
     * @param modelDir  the model directory
     * @param modelName the model name
     * @throws IOException             if I/O error occurs
     * @throws MalformedModelException if model is malformed
     */
    public void loadLocalModel(Path modelDir, String modelName) throws IOException, MalformedModelException {
        logger.info("Loading local model: {}/{}", modelDir, modelName);

        Translator<Image, Classifications> translator = ImageClassificationTranslator.builder()
                .addTransform(new Resize(inputWidth, inputHeight))
                .addTransform(new ToTensor())
                .optApplySoftmax(true)
                .build();

        Model localModel = Model.newInstance(modelName);
        localModel.load(modelDir);

        // Create predictor manually for local models
        predictor = localModel.newPredictor(translator);
        logger.info("Local model loaded: {}", modelName);
    }

    /**
     * Classifies an image.
     *
     * @param image the input image
     * @return classification results
     * @throws Exception if inference fails
     */
    @Override
    protected Classifications doProcess(Image image) throws Exception {
        if (predictor == null) {
            throw new IllegalStateException("Model not loaded. Call loadZooModel() or loadLocalModel() first.");
        }

        long startTime = System.currentTimeMillis();
        Classifications result = predictor.predict(image);
        long elapsed = System.currentTimeMillis() - startTime;

        logger.debug("Inference completed in {}ms", elapsed);
        return result;
    }

    /**
     * Classifies an image from an input stream.
     *
     * @param inputStream the image input stream
     * @return classification results
     * @throws Exception if inference fails
     */
    public Classifications classify(InputStream inputStream) throws Exception {
        Image image = ImageFactory.getInstance().fromInputStream(inputStream);
        return process(image);
    }

    /**
     * Classifies an image from a file path.
     *
     * @param imagePath the image file path
     * @return classification results
     * @throws Exception if inference fails
     */
    public Classifications classify(Path imagePath) throws Exception {
        Image image = ImageFactory.getInstance().fromFile(imagePath);
        return process(image);
    }

    /**
     * Gets the top N classification results.
     *
     * @param image the input image
     * @param topN  number of top results
     * @return list of top classifications
     * @throws Exception if inference fails
     */
    public List<Classifications.Classification> getTopN(Image image, int topN) throws Exception {
        Classifications classifications = process(image);
        return classifications.topK(topN);
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
        logger.info("Deep learning processor shut down");
    }
}
