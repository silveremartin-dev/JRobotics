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
 * A* path planning algorithm implementation.
 * 
 * <p>Finds optimal paths in a 2D grid-based environment with obstacles.</p>
 * 
 * <p><b>Algorithm:</b> A* uses a best-first search with a heuristic function
 * f(n) = g(n) + h(n), where g(n) is the cost from start to n, and h(n) is
 * the estimated cost from n to goal.</p>
 * 
 * <p><b>References:</b></p>
 * <ul>
 *   <li>Hart, P. E., Nilsson, N. J., &amp; Raphael, B. (1968). A Formal Basis 
 *       for the Heuristic Determination of Minimum Cost Paths. 
 *       <i>IEEE Trans. Systems Science and Cybernetics</i>, 4(2).</li>
 *   <li>LaValle, S. M. (2006). <i>Planning Algorithms</i>. Cambridge University Press.</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class AStarPathPlanner {
    
    /** Grid cell states */
    public enum CellState { FREE, OBSTACLE, START, GOAL, PATH }
    
    /** Movement directions (8-connected) */
    private static final int[][] DIRECTIONS = {
        {0, 1}, {1, 0}, {0, -1}, {-1, 0},   // Cardinal
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // Diagonal
    };
    
    private static final double SQRT2 = Math.sqrt(2);
    
    private final int width;
    private final int height;
    private final boolean[][] obstacles;
    private final boolean allowDiagonal;
    
    /**
     * Creates an A* planner.
     * 
     * @param width grid width
     * @param height grid height
     * @param allowDiagonal allow diagonal movement
     */
    public AStarPathPlanner(int width, int height, boolean allowDiagonal) {
        this.width = width;
        this.height = height;
        this.obstacles = new boolean[width][height];
        this.allowDiagonal = allowDiagonal;
    }
    
    /**
     * Creates an A* planner with 8-connected movement.
     */
    public AStarPathPlanner(int width, int height) {
        this(width, height, true);
    }
    
    /**
     * Sets a cell as obstacle.
     */
    public void setObstacle(int x, int y, boolean obstacle) {
        if (isValid(x, y)) {
            obstacles[x][y] = obstacle;
        }
    }
    
    /**
     * Clears all obstacles.
     */
    public void clearObstacles() {
        for (int x = 0; x < width; x++) {
            Arrays.fill(obstacles[x], false);
        }
    }
    
    /**
     * Finds an optimal path from start to goal.
     * 
     * @param startX start X coordinate
     * @param startY start Y coordinate
     * @param goalX goal X coordinate
     * @param goalY goal Y coordinate
     * @return list of (x,y) coordinates, or empty if no path
     */
    public List<int[]> findPath(int startX, int startY, int goalX, int goalY) {
        if (!isValid(startX, startY) || !isValid(goalX, goalY)) {
            return Collections.emptyList();
        }
        if (obstacles[startX][startY] || obstacles[goalX][goalY]) {
            return Collections.emptyList();
        }
        
        // Open set (priority queue by f-score)
        PriorityQueue<Node> openSet = new PriorityQueue<>(
            Comparator.comparingDouble(n -> n.fScore));
        
        // Track visited nodes
        Map<Long, Node> allNodes = new HashMap<>();
        Set<Long> closedSet = new HashSet<>();
        
        // Initialize start node
        Node start = new Node(startX, startY);
        start.gScore = 0;
        start.fScore = heuristic(startX, startY, goalX, goalY);
        openSet.add(start);
        allNodes.put(key(startX, startY), start);
        
        int maxIterations = width * height * 2;
        int iterations = 0;
        
        while (!openSet.isEmpty() && iterations++ < maxIterations) {
            Node current = openSet.poll();
            
            // Goal reached
            if (current.x == goalX && current.y == goalY) {
                return reconstructPath(current);
            }
            
            closedSet.add(key(current.x, current.y));
            
            // Explore neighbors
            int numDirs = allowDiagonal ? 8 : 4;
            for (int d = 0; d < numDirs; d++) {
                int nx = current.x + DIRECTIONS[d][0];
                int ny = current.y + DIRECTIONS[d][1];
                
                if (!isValid(nx, ny) || obstacles[nx][ny]) continue;
                if (closedSet.contains(key(nx, ny))) continue;
                
                // Movement cost
                double moveCost = (d < 4) ? 1.0 : SQRT2;
                double tentativeG = current.gScore + moveCost;
                
                Node neighbor = allNodes.computeIfAbsent(key(nx, ny), 
                    k -> new Node(nx, ny));
                
                if (tentativeG < neighbor.gScore) {
                    neighbor.parent = current;
                    neighbor.gScore = tentativeG;
                    neighbor.fScore = tentativeG + heuristic(nx, ny, goalX, goalY);
                    
                    openSet.remove(neighbor);
                    openSet.add(neighbor);
                }
            }
        }
        
        return Collections.emptyList(); // No path found
    }
    
    /**
     * Finds path and returns as world coordinates.
     */
    public List<double[]> findPathWorld(double startX, double startY, 
            double goalX, double goalY, double cellSize) {
        int sx = (int) (startX / cellSize);
        int sy = (int) (startY / cellSize);
        int gx = (int) (goalX / cellSize);
        int gy = (int) (goalY / cellSize);
        
        List<int[]> gridPath = findPath(sx, sy, gx, gy);
        List<double[]> worldPath = new ArrayList<>();
        
        for (int[] cell : gridPath) {
            worldPath.add(new double[]{
                (cell[0] + 0.5) * cellSize,
                (cell[1] + 0.5) * cellSize
            });
        }
        
        return worldPath;
    }
    
    /**
     * Euclidean heuristic (admissible for 8-connected).
     */
    private double heuristic(int x1, int y1, int x2, int y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    private boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    private long key(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }
    
    private List<int[]> reconstructPath(Node goal) {
        LinkedList<int[]> path = new LinkedList<>();
        Node current = goal;
        while (current != null) {
            path.addFirst(new int[]{current.x, current.y});
            current = current.parent;
        }
        return path;
    }
    
    /**
     * Gets grid dimensions.
     */
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    
    /**
     * Internal node class for A*.
     */
    private static class Node {
        final int x, y;
        double gScore = Double.MAX_VALUE;
        double fScore = Double.MAX_VALUE;
        Node parent = null;
        
        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
