/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor;

import org.jrobotics.core.AbstractComponent;
import org.jrobotics.core.ComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Abstract base implementation of the {@link Processor} interface.
 * 
 * <p>Provides common functionality for all processors including
 * configuration management, async execution, and lifecycle handling.</p>
 * 
 * @param <I> the input data type
 * @param <O> the output/result type
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public abstract class AbstractProcessor<I, O> extends AbstractComponent implements Processor<I, O> {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractProcessor.class);
    
    private final Map<String, Object> configuration = new ConcurrentHashMap<>();
    private volatile double confidence = 1.0;
    private final ExecutorService executor;
    
    /**
     * Constructs a new processor.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     */
    protected AbstractProcessor(String id, String name) {
        super(id, name, ComponentType.PROCESSOR);
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Processor-" + id);
            t.setDaemon(true);
            return t;
        });
    }
    
    @Override
    public O process(I input) throws ProcessorException {
        if (!isRunning()) {
            throw new ProcessorException(getId(), "Processor is not running");
        }
        if (!isEnabled()) {
            throw new ProcessorException(getId(), "Processor is disabled");
        }
        
        try {
            logger.debug("[{}] Processing input on {}", System.currentTimeMillis(), getId());
            O result = doProcess(input);
            logger.debug("[{}] Processing complete on {}", System.currentTimeMillis(), getId());
            return result;
        } catch (Exception e) {
            logger.error("[{}] Processing failed on {}: {}", 
                    System.currentTimeMillis(), getId(), e.getMessage());
            throw new ProcessorException(getId(), "Processing failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public CompletableFuture<O> processAsync(I input) {
        return CompletableFuture.supplyAsync(() -> process(input), executor);
    }
    
    /**
     * Template method for processing input.
     * 
     * <p>Subclasses must implement this to perform actual processing.</p>
     * 
     * @param input the input to process
     * @return the processing result
     * @throws Exception if processing fails
     */
    protected abstract O doProcess(I input) throws Exception;
    
    @Override
    public Map<String, Object> getConfiguration() {
        return new ConcurrentHashMap<>(configuration);
    }
    
    @Override
    public void setConfiguration(String key, Object value) {
        configuration.put(key, value);
        logger.debug("[{}] Configuration set: {} = {}", System.currentTimeMillis(), key, value);
    }
    
    /**
     * Gets a configuration value.
     * 
     * @param key the configuration key
     * @param defaultValue the default value
     * @param <T> the value type
     * @return the configuration value or default
     */
    @SuppressWarnings("unchecked")
    protected <T> T getConfig(String key, T defaultValue) {
        Object value = configuration.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    @Override
    public void reset() {
        configuration.clear();
        confidence = 1.0;
        doReset();
        logger.info("[{}] Processor {} reset", System.currentTimeMillis(), getId());
    }
    
    /**
     * Template method for resetting processor state.
     * 
     * <p>Subclasses can override to reset additional internal state.</p>
     */
    protected void doReset() {
        // Default: no additional reset needed
    }
    
    @Override
    public double getConfidence() {
        return confidence;
    }
    
    /**
     * Sets the confidence level.
     * 
     * @param confidence the confidence from 0.0 to 1.0
     */
    protected void setConfidence(double confidence) {
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
    }
    
    @Override
    public boolean supportsLearning() {
        return false; // Override in learning-capable processors
    }
    
    @Override
    public void train(I input, O expectedOutput) {
        throw new UnsupportedOperationException("This processor does not support learning");
    }
    
    @Override
    protected void doShutdown() throws Exception {
        executor.shutdownNow();
        super.doShutdown();
    }
}
