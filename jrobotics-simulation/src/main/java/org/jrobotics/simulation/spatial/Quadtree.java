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
import org.jrobotics.core.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * Quadtree spatial partitioning for efficient 2D collision detection.
 * 
 * <p>
 * Reduces collision detection from O(n²) to O(n log n) average case
 * by only checking objects in the same or adjacent tree cells.
 * </p>
 * 
 * <p>
 * <b>References:</b>
 * </p>
 * <ul>
 * <li>Ericson, C. (2004). <i>Real-Time Collision Detection</i>. Morgan
 * Kaufmann.</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class Quadtree {

    private static final int MAX_OBJECTS = 10;
    private static final int MAX_LEVELS = 8;

    private final int level;
    private final List<PhysicsBody> objects;
    private final double x, y, width, height;
    private Quadtree[] nodes;

    /**
     * Creates a quadtree.
     * 
     * @param level  current tree level (0 = root)
     * @param x      left bound
     * @param y      bottom bound
     * @param width  width
     * @param height height
     */
    public Quadtree(int level, double x, double y, double width, double height) {
        this.level = level;
        this.objects = new ArrayList<>();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.nodes = null;
    }

    /**
     * Creates a root quadtree.
     */
    public Quadtree(double width, double height) {
        this(0, 0, 0, width, height);
    }

    /**
     * Clears the quadtree and all sub-nodes.
     */
    public void clear() {
        objects.clear();
        if (nodes != null) {
            for (int i = 0; i < 4; i++) {
                nodes[i].clear();
            }
            nodes = null;
        }
    }

    /**
     * Splits this node into 4 quadrants.
     */
    private void split() {
        double subWidth = width / 2;
        double subHeight = height / 2;

        nodes = new Quadtree[4];
        nodes[0] = new Quadtree(level + 1, x + subWidth, y + subHeight, subWidth, subHeight); // NE
        nodes[1] = new Quadtree(level + 1, x, y + subHeight, subWidth, subHeight); // NW
        nodes[2] = new Quadtree(level + 1, x, y, subWidth, subHeight); // SW
        nodes[3] = new Quadtree(level + 1, x + subWidth, y, subWidth, subHeight); // SE
    }

    /**
     * Gets the quadrant index for a body.
     * 
     * @return quadrant index (0-3), or -1 if body spans multiple quadrants
     */
    private int getIndex(PhysicsBody body) {
        int index = -1;
        double midX = x + width / 2;
        double midY = y + height / 2;

        Vector3 pos = body.position();
        double radius = body.boundingRadius();

        boolean topQuadrant = pos.y() - radius > midY;
        boolean bottomQuadrant = pos.y() + radius < midY;
        boolean leftQuadrant = pos.x() + radius < midX;
        boolean rightQuadrant = pos.x() - radius > midX;

        if (topQuadrant) {
            if (rightQuadrant)
                index = 0;
            else if (leftQuadrant)
                index = 1;
        } else if (bottomQuadrant) {
            if (leftQuadrant)
                index = 2;
            else if (rightQuadrant)
                index = 3;
        }

        return index;
    }

    /**
     * Inserts a body into the quadtree.
     */
    public void insert(PhysicsBody body) {
        if (nodes != null) {
            int index = getIndex(body);
            if (index != -1) {
                nodes[index].insert(body);
                return;
            }
        }

        objects.add(body);

        if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes == null) {
                split();
            }

            int i = 0;
            while (i < objects.size()) {
                int index = getIndex(objects.get(i));
                if (index != -1) {
                    nodes[index].insert(objects.remove(i));
                } else {
                    i++;
                }
            }
        }
    }

    /**
     * Returns all bodies that could collide with the given body.
     */
    public List<PhysicsBody> retrieve(PhysicsBody body) {
        List<PhysicsBody> result = new ArrayList<>();
        retrieve(body, result);
        return result;
    }

    private void retrieve(PhysicsBody body, List<PhysicsBody> result) {
        int index = getIndex(body);

        if (index != -1 && nodes != null) {
            nodes[index].retrieve(body, result);
        } else if (nodes != null) {
            // Body spans multiple quadrants, check all children
            for (Quadtree node : nodes) {
                node.retrieve(body, result);
            }
        }

        result.addAll(objects);
    }

    /**
     * Returns all potential collision pairs.
     */
    public List<BodyPair> getPotentialCollisions() {
        List<BodyPair> pairs = new ArrayList<>();
        List<PhysicsBody> allBodies = new ArrayList<>();
        getAllBodies(allBodies);

        for (PhysicsBody body : allBodies) {
            List<PhysicsBody> candidates = retrieve(body);
            for (PhysicsBody candidate : candidates) {
                if (body != candidate && !containsPair(pairs, body, candidate)) {
                    pairs.add(new BodyPair(body, candidate));
                }
            }
        }

        return pairs;
    }

    private void getAllBodies(List<PhysicsBody> result) {
        result.addAll(objects);
        if (nodes != null) {
            for (Quadtree node : nodes) {
                node.getAllBodies(result);
            }
        }
    }

    private boolean containsPair(List<BodyPair> pairs, PhysicsBody a, PhysicsBody b) {
        for (BodyPair pair : pairs) {
            if ((pair.a == a && pair.b == b) || (pair.a == b && pair.b == a)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets tree statistics.
     */
    public Stats getStats() {
        int[] counts = { 0, 0 }; // [nodes, objects]
        countStats(counts);
        return new Stats(counts[0], counts[1], level);
    }

    private void countStats(int[] counts) {
        counts[0]++;
        counts[1] += objects.size();
        if (nodes != null) {
            for (Quadtree node : nodes) {
                node.countStats(counts);
            }
        }
    }

    /**
     * Pair of bodies for collision checking.
     */
    public record BodyPair(PhysicsBody a, PhysicsBody b) {
    }

    /**
     * Tree statistics.
     */
    public record Stats(int nodeCount, int objectCount, int maxDepth) {
    }
}
