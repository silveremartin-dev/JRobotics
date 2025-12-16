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

import java.util.ArrayList;
import java.util.List;

/**
 * Selector node - tries children until one succeeds.
 * 
 * <p>Returns SUCCESS on first child success. Returns FAILURE only if all fail.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class Selector implements BehaviorNode {
    
    private final String name;
    private final List<BehaviorNode> children = new ArrayList<>();
    private int currentIndex = 0;
    
    public Selector(String name) {
        this.name = name;
    }
    
    /**
     * Adds a child node.
     * 
     * @param child the child node
     * @return this for chaining
     */
    public Selector addChild(BehaviorNode child) {
        children.add(child);
        return this;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Status execute(BehaviorContext context) {
        while (currentIndex < children.size()) {
            Status status = children.get(currentIndex).execute(context);
            
            if (status == Status.SUCCESS) {
                return Status.SUCCESS;
            } else if (status == Status.RUNNING) {
                return Status.RUNNING;
            }
            
            // FAILURE - try next child
            currentIndex++;
        }
        
        return Status.FAILURE;
    }
    
    @Override
    public void reset() {
        currentIndex = 0;
        children.forEach(BehaviorNode::reset);
    }
}
