/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.visualization;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import org.jrobotics.simulation.PhysicsBody;
import org.jrobotics.simulation.PhysicsWorld;
import org.jrobotics.simulation.Vector3;
import org.jrobotics.simulation.visualization.Visualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 3D visualizer using JMonkeyEngine.
 * 
 * <p>Provides real-time 3D rendering of the simulation.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class JMonkey3DVisualizer extends SimpleApplication implements Visualizer {
    
    private static final Logger logger = LoggerFactory.getLogger(JMonkey3DVisualizer.class);
    
    private PhysicsWorld currentWorld;
    private Node robotsNode;
    private Node obstaclesNode;
    private final Map<String, Geometry> bodyGeometries = new ConcurrentHashMap<>();
    
    private volatile boolean initialized = false;
    private volatile boolean running = false;
    
    private Material robotMaterial;
    private Material obstacleMaterial;
    private Material groundMaterial;
    
    /**
     * Creates a JMonkey 3D visualizer.
     */
    public JMonkey3DVisualizer() {
        super();
    }
    
    /**
     * Creates and starts the visualizer.
     */
    public static JMonkey3DVisualizer create(String title, int width, int height) {
        JMonkey3DVisualizer viz = new JMonkey3DVisualizer();
        
        AppSettings settings = new AppSettings(true);
        settings.setTitle(title);
        settings.setWidth(width);
        settings.setHeight(height);
        settings.setVSync(true);
        settings.setSamples(4);  // Anti-aliasing
        
        viz.setSettings(settings);
        viz.setShowSettings(false);
        viz.setPauseOnLostFocus(false);
        
        return viz;
    }
    
    @Override
    public void simpleInitApp() {
        // Disable default fly camera stats
        setDisplayStatView(false);
        setDisplayFps(true);
        
        // Create scene nodes
        robotsNode = new Node("Robots");
        obstaclesNode = new Node("Obstacles");
        rootNode.attachChild(robotsNode);
        rootNode.attachChild(obstaclesNode);
        
        // Create materials
        robotMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        robotMaterial.setColor("Diffuse", ColorRGBA.Cyan);
        robotMaterial.setColor("Ambient", ColorRGBA.Cyan.mult(0.3f));
        robotMaterial.setBoolean("UseMaterialColors", true);
        
        obstacleMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        obstacleMaterial.setColor("Diffuse", ColorRGBA.Gray);
        obstacleMaterial.setColor("Ambient", ColorRGBA.Gray.mult(0.3f));
        obstacleMaterial.setBoolean("UseMaterialColors", true);
        
        groundMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        groundMaterial.setColor("Diffuse", new ColorRGBA(0.2f, 0.3f, 0.2f, 1f));
        groundMaterial.setColor("Ambient", new ColorRGBA(0.1f, 0.15f, 0.1f, 1f));
        groundMaterial.setBoolean("UseMaterialColors", true);
        
        // Add ground plane
        Box groundBox = new Box(50, 0.1f, 50);
        Geometry ground = new Geometry("Ground", groundBox);
        ground.setMaterial(groundMaterial);
        ground.setLocalTranslation(0, -0.1f, 0);
        rootNode.attachChild(ground);
        
        // Add grid lines
        addGridLines();
        
        // Setup lighting
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -1f, -0.5f).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(1.2f));
        rootNode.addLight(sun);
        
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.4f));
        rootNode.addLight(ambient);
        
        // Setup camera
        cam.setLocation(new Vector3f(0, 15, 20));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(10);
        
        initialized = true;
        running = true;
        
        logger.info("[{}] JMonkey3DVisualizer initialized", System.currentTimeMillis());
    }
    
    private void addGridLines() {
        // Grid lines would require a custom mesh or line shapes
        // Simplified: just the ground plane
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        if (currentWorld == null) return;
        
        // Update robot positions
        for (PhysicsBody body : currentWorld.getBodies()) {
            Geometry geom = bodyGeometries.get(body.id());
            
            if (geom == null) {
                // Create new geometry
                geom = createBodyGeometry(body);
                bodyGeometries.put(body.id(), geom);
                
                if (body.isStatic()) {
                    obstaclesNode.attachChild(geom);
                } else {
                    robotsNode.attachChild(geom);
                }
            }
            
            // Update position
            Vector3 pos = body.position();
            geom.setLocalTranslation((float) pos.x(), (float) pos.z() + 0.5f, (float) -pos.y());
        }
    }
    
    private Geometry createBodyGeometry(PhysicsBody body) {
        float radius = (float) body.boundingRadius();
        
        if (body.isStatic()) {
            // Static obstacles as boxes
            Box box = new Box(radius, radius, radius);
            Geometry geom = new Geometry(body.id(), box);
            geom.setMaterial(obstacleMaterial);
            return geom;
        } else {
            // Dynamic robots as spheres
            Sphere sphere = new Sphere(16, 16, radius);
            Geometry geom = new Geometry(body.id(), sphere);
            geom.setMaterial(robotMaterial);
            return geom;
        }
    }
    
    @Override
    public void initialize() {
        // Start in a new thread
        Thread jmeThread = new Thread(() -> {
            start();
        }, "JME3-Visualizer");
        jmeThread.setDaemon(true);
        jmeThread.start();
        
        // Wait for initialization
        while (!initialized) {
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }
    }
    
    @Override
    public void update(PhysicsWorld world) {
        this.currentWorld = world;
    }
    
    @Override
    public void render() {
        // JME handles rendering internally
    }
    
    @Override
    public boolean isActive() {
        return running && initialized;
    }
    
    @Override
    public void shutdown() {
        running = false;
        stop();
        logger.info("[{}] JMonkey3DVisualizer shut down", System.currentTimeMillis());
    }
    
    @Override
    public void setCameraPosition(double x, double y, double z) {
        if (cam != null) {
            cam.setLocation(new Vector3f((float) x, (float) z, (float) -y));
        }
    }
    
    @Override
    public void setCameraTarget(double x, double y, double z) {
        if (cam != null) {
            cam.lookAt(new Vector3f((float) x, (float) z, (float) -y), Vector3f.UNIT_Y);
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        running = false;
    }
}
