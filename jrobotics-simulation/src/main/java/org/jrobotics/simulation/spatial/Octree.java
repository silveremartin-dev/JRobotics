/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.simulation.spatial;

import org.jrobotics.simulation.PhysicsBody;
import org.jrobotics.simulation.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * Octree spatial partitioning for 3D collision detection.
 * 
 * <p>Extension of Quadtree to 3D space for volumetric robots and environments.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class Octree {
    
    private static final int MAX_OBJECTS = 8;
    private static final int MAX_LEVELS = 6;
    
    private final int level;
    private final List<PhysicsBody> objects;
    private final double x, y, z, width, height, depth;
    private Octree[] children;
    
    /**
     * Creates an octree node.
     */
    public Octree(int level, double x, double y, double z, 
                  double width, double height, double depth) {
        this.level = level;
        this.objects = new ArrayList<>();
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }
    
    /**
     * Creates a root octree.
     */
    public Octree(double width, double height, double depth) {
        this(0, 0, 0, 0, width, height, depth);
    }
    
    /**
     * Clears the octree.
     */
    public void clear() {
        objects.clear();
        if (children != null) {
            for (int i = 0; i < 8; i++) {
                children[i].clear();
            }
            children = null;
        }
    }
    
    private void split() {
        double hw = width / 2, hh = height / 2, hd = depth / 2;
        
        children = new Octree[8];
        children[0] = new Octree(level + 1, x + hw, y + hh, z + hd, hw, hh, hd);
        children[1] = new Octree(level + 1, x, y + hh, z + hd, hw, hh, hd);
        children[2] = new Octree(level + 1, x, y, z + hd, hw, hh, hd);
        children[3] = new Octree(level + 1, x + hw, y, z + hd, hw, hh, hd);
        children[4] = new Octree(level + 1, x + hw, y + hh, z, hw, hh, hd);
        children[5] = new Octree(level + 1, x, y + hh, z, hw, hh, hd);
        children[6] = new Octree(level + 1, x, y, z, hw, hh, hd);
        children[7] = new Octree(level + 1, x + hw, y, z, hw, hh, hd);
    }
    
    private int getIndex(PhysicsBody body) {
        int index = -1;
        double midX = x + width / 2;
        double midY = y + height / 2;
        double midZ = z + depth / 2;
        
        Vector3 pos = body.position();
        double r = body.boundingRadius();
        
        boolean front = pos.z() - r > midZ;
        boolean back = pos.z() + r < midZ;
        boolean top = pos.y() - r > midY;
        boolean bottom = pos.y() + r < midY;
        boolean right = pos.x() - r > midX;
        boolean left = pos.x() + r < midX;
        
        if (front) {
            if (top && right) index = 0;
            else if (top && left) index = 1;
            else if (bottom && left) index = 2;
            else if (bottom && right) index = 3;
        } else if (back) {
            if (top && right) index = 4;
            else if (top && left) index = 5;
            else if (bottom && left) index = 6;
            else if (bottom && right) index = 7;
        }
        
        return index;
    }
    
    /**
     * Inserts a body.
     */
    public void insert(PhysicsBody body) {
        if (children != null) {
            int index = getIndex(body);
            if (index != -1) {
                children[index].insert(body);
                return;
            }
        }
        
        objects.add(body);
        
        if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (children == null) split();
            
            int i = 0;
            while (i < objects.size()) {
                int index = getIndex(objects.get(i));
                if (index != -1) {
                    children[index].insert(objects.remove(i));
                } else {
                    i++;
                }
            }
        }
    }
    
    /**
     * Retrieves potential collision candidates.
     */
    public List<PhysicsBody> retrieve(PhysicsBody body) {
        List<PhysicsBody> result = new ArrayList<>();
        retrieve(body, result);
        return result;
    }
    
    private void retrieve(PhysicsBody body, List<PhysicsBody> result) {
        int index = getIndex(body);
        if (index != -1 && children != null) {
            children[index].retrieve(body, result);
        } else if (children != null) {
            for (Octree child : children) {
                child.retrieve(body, result);
            }
        }
        result.addAll(objects);
    }
}
