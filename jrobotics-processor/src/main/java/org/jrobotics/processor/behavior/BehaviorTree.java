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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Behavior tree executor.
 * 
 * <p>Runs a behavior tree at regular intervals.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class BehaviorTree {
    
    private static final Logger logger = LoggerFactory.getLogger(BehaviorTree.class);
    
    private final String name;
    private final BehaviorNode root;
    private final BehaviorContext context;
    
    public BehaviorTree(String name, BehaviorNode root) {
        this.name = name;
        this.root = root;
        this.context = new BehaviorContext();
    }
    
    public BehaviorTree(String name, BehaviorNode root, BehaviorContext context) {
        this.name = name;
        this.root = root;
        this.context = context;
    }
    
    /**
     * Executes one tick of the behavior tree.
     * 
     * @return the root status
     */
    public BehaviorNode.Status tick() {
        context.tick();
        BehaviorNode.Status status = root.execute(context);
        
        logger.debug("BehaviorTree '{}' tick {}: {}", name, context.getTickCount(), status);
        
        if (status != BehaviorNode.Status.RUNNING) {
            root.reset();
        }
        
        return status;
    }
    
    /**
     * Gets the name.
     * 
     * @return the tree name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the context.
     * 
     * @return the behavior context
     */
    public BehaviorContext getContext() {
        return context;
    }
    
    /**
     * Resets the tree.
     */
    public void reset() {
        root.reset();
        context.clear();
    }
}
