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

/**
 * Exception thrown when a processor operation fails.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class ProcessorException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String processorId;
    
    /**
     * Constructs a new processor exception.
     * 
     * @param message the detail message
     */
    public ProcessorException(String message) {
        super(message);
        this.processorId = null;
    }
    
    /**
     * Constructs a new processor exception with processor ID.
     * 
     * @param processorId the processor ID
     * @param message the detail message
     */
    public ProcessorException(String processorId, String message) {
        super("[" + processorId + "] " + message);
        this.processorId = processorId;
    }
    
    /**
     * Constructs a new processor exception with cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public ProcessorException(String message, Throwable cause) {
        super(message, cause);
        this.processorId = null;
    }
    
    /**
     * Constructs a new processor exception with processor ID and cause.
     * 
     * @param processorId the processor ID
     * @param message the detail message
     * @param cause the cause
     */
    public ProcessorException(String processorId, String message, Throwable cause) {
        super("[" + processorId + "] " + message, cause);
        this.processorId = processorId;
    }
    
    /**
     * Gets the processor ID associated with this exception.
     * 
     * @return the processor ID, or null if not available
     */
    public String getProcessorId() {
        return processorId;
    }
}
