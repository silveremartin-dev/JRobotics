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

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

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
 * <p>
 * Provides real-time 3D rendering of the simulation.
 * </p>
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
    private final Map<String, com.jme3.scene.Spatial> bodyGeometries = new ConcurrentHashMap<>();
    private final Map<String, String> modelRegistry = new ConcurrentHashMap<>();

    /**
     * Registers a 3D model path for a specific body ID.
     * 
     * @param bodyId    the physics body ID
     * @param assetPath path to .obj, .gltf, or .j3o file
     */
    public void registerModel(String bodyId, String assetPath) {
        modelRegistry.put(bodyId, assetPath);
    }

    // ...

    @Override
    public void simpleUpdate(float tpf) {
        if (currentWorld == null)
            return;

        // Update robot positions
        for (PhysicsBody body : currentWorld.getBodies()) {
            com.jme3.scene.Spatial spatial = bodyGeometries.get(body.id());

            if (spatial == null) {
                // Create new spatial
                spatial = createBodySpatial(body);
                bodyGeometries.put(body.id(), spatial);

                if (body.isStatic()) {
                    obstaclesNode.attachChild(spatial);
                } else {
                    robotsNode.attachChild(spatial);
                }
            }

            // Update position (JME uses Y-up, Physics might differ)
            // JRobotics standard: X=Forward, Y=Left, Z=Up? Or standard math?
            // Assuming standard robotics: Z is up. JME is Y up.
            // Rotations need quaternion conversion from 2D heading

            Vector3 pos = body.position();
            spatial.setLocalTranslation((float) pos.x(), (float) pos.z(), (float) -pos.y());

            // Should update rotation if body has orientation
            // For now, assume simple heading
        }
    }

    private com.jme3.scene.Spatial createBodySpatial(PhysicsBody body) {
        // Check for registered model
        String modelPath = modelRegistry.get(body.id());
        if (modelPath != null) {
            try {
                com.jme3.scene.Spatial model = assetManager.loadModel(modelPath);
                // Approximate scaling
                float r = (float) body.boundingRadius();
                model.setLocalScale(r * 2); // Diameter
                return model;
            } catch (Exception e) {
                logger.warn("Failed to load model {}, falling back to primitive", modelPath);
            }
        }

        float radius = (float) body.boundingRadius();

        if (body.isStatic()) {
            // Static obstacles as boxes
            Box box = new Box(radius, radius, radius);
            Geometry geom = new Geometry(body.id(), box);
            geom.setMaterial(obstacleMaterial);
            return geom;
        } else {
            // Dynamic robots as spheres
            Sphere sphere = new Sphere(32, 32, radius);
            Geometry geom = new Geometry(body.id(), sphere);
            geom.setMaterial(robotMaterial);
            return geom;
        }
    }

    private boolean initialized = false;
    private volatile boolean running = false;

    private com.jme3.material.Material obstacleMaterial;
    private com.jme3.material.Material robotMaterial;

    public JMonkey3DVisualizer() {
        // Constructor
    }

    @Override
    public void simpleInitApp() {
        // Initialize Scene Graph
        robotsNode = new Node("Robots");
        obstaclesNode = new Node("Obstacles");
        rootNode.attachChild(robotsNode);
        rootNode.attachChild(obstaclesNode);

        // Initialize Materials
        obstacleMaterial = new com.jme3.material.Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        obstacleMaterial.setColor("Color", com.jme3.math.ColorRGBA.Blue);

        robotMaterial = new com.jme3.material.Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        robotMaterial.setColor("Color", com.jme3.math.ColorRGBA.Red);

        // Configure Camera
        flyCam.setMoveSpeed(10f);
        cam.setLocation(new Vector3f(0, 10, 10));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        initialized = true;
        running = true;

        // Disable stats view
        setDisplayFps(false);
        setDisplayStatView(false);
    }

    // ...

    @Override
    public void initialize() {
        if (running) {
            return; // Already initialized
        }

        // Start in a new thread - JME must run on its own thread
        Thread jmeThread = new Thread(() -> {
            try {
                // Apply settings before start
                com.jme3.system.AppSettings settings = new com.jme3.system.AppSettings(true);
                settings.setTitle("JRobotics 3D Visualization");
                settings.setResolution(1024, 768);
                setSettings(settings);
                setShowSettings(false);

                start(); // This blocks until the window closes
            } catch (Exception e) {
                logger.error("JME3 start failed", e);
            }
        }, "JME3-Visualizer");
        // jmeThread.setDaemon(true); // JME might need non-daemon?
        jmeThread.start();

        // Wait for initialization (timeout after 10 seconds)
        int timeout = 200; // 200 * 50ms = 10s
        while (!initialized && timeout-- > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }

        if (!initialized) {
            logger.error("JME3 initialization timeout");
        }
    }

    // ... (rest of update logic)

    @Override
    public void setCameraTarget(double x, double y, double z) {
        if (cam != null) {
            cam.lookAt(new Vector3f((float) x, (float) z, (float) -y), Vector3f.UNIT_Y);
        }
    }

    @Override
    public void update(PhysicsWorld world) {
        this.currentWorld = world;
    }

    @Override
    public void render() {
        // JME handles rendering internally via simpleUpdate
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
    public void destroy() {
        super.destroy();
        running = false;
    }
}
