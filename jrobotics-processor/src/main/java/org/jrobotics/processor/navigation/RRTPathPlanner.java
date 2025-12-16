/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor.navigation;

import java.util.*;

/**
 * Rapidly-exploring Random Tree (RRT) path planner.
 * 
 * <p>RRT is a sampling-based algorithm that efficiently explores high-dimensional 
 * configuration spaces by incrementally building a tree from the start to the goal.</p>
 * 
 * <p><b>Algorithm:</b></p>
 * <ol>
 *   <li>Sample random point in configuration space</li>
 *   <li>Find nearest node in tree</li>
 *   <li>Extend tree toward sample (limited step)</li>
 *   <li>Repeat until goal reached</li>
 * </ol>
 * 
 * <p><b>References:</b></p>
 * <ul>
 *   <li>LaValle, S. M. (1998). Rapidly-exploring random trees: A new tool 
 *       for path planning. Technical Report TR 98-11, Computer Science Dept.</li>
 *   <li>LaValle, S. M. (2006). <i>Planning Algorithms</i>. Cambridge University Press.</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class RRTPathPlanner {
    
    private final double minX, maxX, minY, maxY;
    private final double stepSize;
    private final int maxIterations;
    private final double goalThreshold;
    private final Random random;
    
    private CollisionChecker collisionChecker;
    
    /**
     * Creates an RRT path planner.
     * 
     * @param minX minimum X bound
     * @param maxX maximum X bound
     * @param minY minimum Y bound
     * @param maxY maximum Y bound
     * @param stepSize maximum extension step size
     * @param maxIterations maximum iterations
     */
    public RRTPathPlanner(double minX, double maxX, double minY, double maxY,
                          double stepSize, int maxIterations) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.stepSize = stepSize;
        this.maxIterations = maxIterations;
        this.goalThreshold = stepSize * 1.5;
        this.random = new Random();
    }
    
    /**
     * Creates RRT planner with default settings.
     */
    public RRTPathPlanner(double width, double height) {
        this(0, width, 0, height, 0.5, 5000);
    }
    
    /**
     * Sets collision checker for obstacle avoidance.
     */
    public void setCollisionChecker(CollisionChecker checker) {
        this.collisionChecker = checker;
    }
    
    /**
     * Finds a path from start to goal.
     * 
     * @param startX start X
     * @param startY start Y
     * @param goalX goal X
     * @param goalY goal Y
     * @return list of waypoints, or empty if no path found
     */
    public List<double[]> findPath(double startX, double startY, double goalX, double goalY) {
        // Initialize tree with start node
        List<Node> tree = new ArrayList<>();
        Node start = new Node(startX, startY, null);
        tree.add(start);
        
        for (int i = 0; i < maxIterations; i++) {
            // Sample random point (with goal bias ~5%)
            double[] sample;
            if (random.nextDouble() < 0.05) {
                sample = new double[]{goalX, goalY};
            } else {
                sample = sampleRandom();
            }
            
            // Find nearest node
            Node nearest = findNearest(tree, sample[0], sample[1]);
            
            // Extend toward sample
            Node newNode = extend(nearest, sample[0], sample[1]);
            
            if (newNode != null && !isCollision(nearest.x, nearest.y, newNode.x, newNode.y)) {
                tree.add(newNode);
                
                // Check if goal reached
                if (distance(newNode.x, newNode.y, goalX, goalY) < goalThreshold) {
                    if (!isCollision(newNode.x, newNode.y, goalX, goalY)) {
                        Node goal = new Node(goalX, goalY, newNode);
                        tree.add(goal);
                        return extractPath(goal);
                    }
                }
            }
        }
        
        return Collections.emptyList();
    }
    
    /**
     * RRT* variant with path optimization.
     */
    public List<double[]> findPathRRTStar(double startX, double startY, 
                                           double goalX, double goalY, double rewireRadius) {
        List<Node> tree = new ArrayList<>();
        Node start = new Node(startX, startY, null);
        start.cost = 0;
        tree.add(start);
        
        Node bestGoal = null;
        
        for (int i = 0; i < maxIterations; i++) {
            double[] sample;
            if (random.nextDouble() < 0.05) {
                sample = new double[]{goalX, goalY};
            } else {
                sample = sampleRandom();
            }
            
            Node nearest = findNearest(tree, sample[0], sample[1]);
            Node newNode = extend(nearest, sample[0], sample[1]);
            
            if (newNode != null && !isCollision(nearest.x, nearest.y, newNode.x, newNode.y)) {
                // Find best parent within rewire radius
                List<Node> neighbors = findNodesInRadius(tree, newNode.x, newNode.y, rewireRadius);
                Node bestParent = nearest;
                double bestCost = nearest.cost + distance(nearest.x, nearest.y, newNode.x, newNode.y);
                
                for (Node neighbor : neighbors) {
                    double cost = neighbor.cost + distance(neighbor.x, neighbor.y, newNode.x, newNode.y);
                    if (cost < bestCost && !isCollision(neighbor.x, neighbor.y, newNode.x, newNode.y)) {
                        bestCost = cost;
                        bestParent = neighbor;
                    }
                }
                
                newNode.parent = bestParent;
                newNode.cost = bestCost;
                tree.add(newNode);
                
                // Rewire neighbors through new node if cheaper
                for (Node neighbor : neighbors) {
                    double costThrough = newNode.cost + distance(newNode.x, newNode.y, neighbor.x, neighbor.y);
                    if (costThrough < neighbor.cost && !isCollision(newNode.x, newNode.y, neighbor.x, neighbor.y)) {
                        neighbor.parent = newNode;
                        neighbor.cost = costThrough;
                    }
                }
                
                // Check goal
                if (distance(newNode.x, newNode.y, goalX, goalY) < goalThreshold) {
                    if (!isCollision(newNode.x, newNode.y, goalX, goalY)) {
                        if (bestGoal == null || newNode.cost < bestGoal.cost) {
                            bestGoal = new Node(goalX, goalY, newNode);
                            bestGoal.cost = newNode.cost + distance(newNode.x, newNode.y, goalX, goalY);
                        }
                    }
                }
            }
        }
        
        return bestGoal != null ? extractPath(bestGoal) : Collections.emptyList();
    }
    
    private double[] sampleRandom() {
        return new double[]{
            minX + random.nextDouble() * (maxX - minX),
            minY + random.nextDouble() * (maxY - minY)
        };
    }
    
    private Node findNearest(List<Node> tree, double x, double y) {
        Node nearest = tree.get(0);
        double minDist = distance(nearest.x, nearest.y, x, y);
        
        for (Node node : tree) {
            double d = distance(node.x, node.y, x, y);
            if (d < minDist) {
                minDist = d;
                nearest = node;
            }
        }
        return nearest;
    }
    
    private List<Node> findNodesInRadius(List<Node> tree, double x, double y, double radius) {
        List<Node> result = new ArrayList<>();
        for (Node node : tree) {
            if (distance(node.x, node.y, x, y) < radius) {
                result.add(node);
            }
        }
        return result;
    }
    
    private Node extend(Node from, double toX, double toY) {
        double dx = toX - from.x;
        double dy = toY - from.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist < 1e-6) return null;
        
        double actualStep = Math.min(stepSize, dist);
        double newX = from.x + dx / dist * actualStep;
        double newY = from.y + dy / dist * actualStep;
        
        return new Node(newX, newY, from);
    }
    
    private boolean isCollision(double x1, double y1, double x2, double y2) {
        if (collisionChecker != null) {
            return collisionChecker.isCollision(x1, y1, x2, y2);
        }
        return false;
    }
    
    private double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    private List<double[]> extractPath(Node goal) {
        LinkedList<double[]> path = new LinkedList<>();
        Node current = goal;
        while (current != null) {
            path.addFirst(new double[]{current.x, current.y});
            current = current.parent;
        }
        return path;
    }
    
    private static class Node {
        final double x, y;
        Node parent;
        double cost = Double.MAX_VALUE;
        
        Node(double x, double y, Node parent) {
            this.x = x;
            this.y = y;
            this.parent = parent;
        }
    }
    
    /**
     * Collision checker interface.
     */
    @FunctionalInterface
    public interface CollisionChecker {
        boolean isCollision(double x1, double y1, double x2, double y2);
    }
}
