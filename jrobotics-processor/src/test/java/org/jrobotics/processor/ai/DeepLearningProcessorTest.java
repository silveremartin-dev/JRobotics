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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeepLearningProcessorTest {

    private DeepLearningProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new DeepLearningProcessor("dl-1");
    }

    @Test
    void testConfiguration() {
        // Verify default config
        processor.setModelPath("models/", "resnet18");
        assertEquals("resnet18", processor.getModelName());
        assertEquals("models/", processor.getModelPath());

        processor.setInputDimensions(512, 512);
        // No getters for dimensions, but method exists. Verification implies no crash.
    }

    @Test
    void testLifecycleWithoutModel() {
        // Initialize should succeed (Base component init)
        assertDoesNotThrow(() -> processor.initialize());
        assertDoesNotThrow(() -> processor.start());

        // Process (inference) should throw IllegalStateException because loadZooModel
        // wasn't called
        Exception exception = assertThrows(org.jrobotics.processor.ProcessorException.class, () -> {
            // Mock input is hard because Image is interface/class from DJL.
            // We just test the check inside doProcess which throws if predictor is null.
            // But process() wraps exceptions in ProcessorException.
            processor.process(null);
        });

        // The cause should be the IllegalStateException from doProcess
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Model not loaded") ||
                exception.getMessage().contains("Processing failed"));
    }
}
