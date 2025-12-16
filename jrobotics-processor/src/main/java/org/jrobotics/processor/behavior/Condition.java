/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor.behavior;

import java.util.function.Predicate;

/**
 * Condition node - checks a condition without side effects.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class Condition implements BehaviorNode {
    
    private final String name;
    private final Predicate<BehaviorContext> predicate;
    
    public Condition(String name, Predicate<BehaviorContext> predicate) {
        this.name = name;
        this.predicate = predicate;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Status execute(BehaviorContext context) {
        return predicate.test(context) ? Status.SUCCESS : Status.FAILURE;
    }
    
    @Override
    public void reset() {
        // Stateless
    }
}
