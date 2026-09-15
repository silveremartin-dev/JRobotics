/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.network.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.jrobotics.core.Robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Integrated Web Dashboard Server.
 * 
 * <p>
 * Provides a WebSocket server for real-time telemetry and a lightweight
 * HTTP server to serve the static dashboard files.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.2.0
 */
public class DashboardServer {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServer.class);

    private final TelemetryServer wsServer;
    private final HttpServer httpServer;
    private final ScheduledExecutorService telemetryLoop;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<Robot> robots = ConcurrentHashMap.newKeySet();

    // Singleton-ish access for inner class usage (simpler than passing ref)
    static DashboardServer instance;

    /**
     * Creates a dashboard server.
     * 
     * @param port base port (HTTP=port, WS=port+1)
     */
    public DashboardServer(int port) {
        instance = this;
        this.wsServer = new TelemetryServer(port + 1);
        this.httpServer = new HttpServer(port);
        this.telemetryLoop = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Dashboard-Telemetry");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Adds a robot to be monitored.
     */
    public void monitor(Robot robot) {
        robots.add(robot);
    }

    /**
     * Starts the dashboard server.
     */
    public void start() {
        new Thread(httpServer::start, "Dashboard-HTTP").start();
        wsServer.start();

        // Broadcast telemetry at 10Hz
        telemetryLoop.scheduleAtFixedRate(this::broadcastTelemetry, 0, 100, TimeUnit.MILLISECONDS);

        logger.info("Dashboard started. Access at http://localhost:{}", httpServer.port);
    }

    /**
     * Stops the dashboard server.
     */
    public void stop() {
        try {
            wsServer.stop();
            httpServer.stop();
            telemetryLoop.shutdownNow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            logger.error("Error stopping dashboard", e);
        }
    }

    private void broadcastTelemetry() {
        if (robots.isEmpty())
            return;

        try {
            ObjectNode root = mapper.createObjectNode();
            root.put("type", "telemetry");
            root.put("timestamp", System.currentTimeMillis());

            ObjectNode robotsNode = root.putObject("robots");

            for (Robot robot : robots) {
                ObjectNode rNode = robotsNode.putObject(robot.getId());
                rNode.put("name", robot.getName());
                rNode.put("state", robot.getState().name());

                // Dynamic reflection to avoid circular dependency on robot module
                try {
                    Class<?> clazz = robot.getClass();
                    // Check for pose methods
                    try {
                        java.lang.reflect.Method getX = clazz.getMethod("getX");
                        java.lang.reflect.Method getY = clazz.getMethod("getY");
                        java.lang.reflect.Method getTheta = clazz.getMethod("getTheta");

                        ObjectNode pose = rNode.putObject("pose");
                        pose.put("x", (Double) getX.invoke(robot));
                        pose.put("y", (Double) getY.invoke(robot));
                        pose.put("theta", (Double) getTheta.invoke(robot));
                    } catch (NoSuchMethodException ignored) {
                        /* Not a mobile robot with 2D pose */ }

                    // Check for velocity methods
                    try {
                        java.lang.reflect.Method getLin = clazz.getMethod("getLinearVelocity");
                        java.lang.reflect.Method getAng = clazz.getMethod("getAngularVelocity");

                        ObjectNode vel = rNode.putObject("velocity");
                        vel.put("linear", (Double) getLin.invoke(robot));
                        vel.put("angular", (Double) getAng.invoke(robot));
                    } catch (NoSuchMethodException ignored) {
                        /* Not a differential drive robot */ }

                } catch (Exception e) {
                    logger.debug("Telemetry reflection failed for {}", robot.getName(), e);
                }
            }

            String json = mapper.writeValueAsString(root);
            wsServer.broadcast(json);

        } catch (Exception e) {
            logger.warn("Telemetry broadcast failed", e);
        }
    }

    protected String getRobotsJson() {
        try {
            ObjectNode root = mapper.createObjectNode();
            for (Robot robot : robots) {
                ObjectNode rNode = root.putObject(robot.getId());
                rNode.put("name", robot.getName());
                rNode.put("state", robot.getState().name());
                // (Skip full telemetry for simple list)
            }
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            return "[]";
        }
    }

    // --- WebSocket Server ---

    private class TelemetryServer extends WebSocketServer {

        public TelemetryServer(int port) {
            super(new InetSocketAddress(port));
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            logger.debug("Dashboard client connected: {}", conn.getRemoteSocketAddress());
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            logger.debug("Dashboard client disconnected");
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            // Handle control commands from dashboard here
            logger.debug("Received command: {}", message);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            logger.error("WebSocket error", ex);
        }

        @Override
        public void onStart() {
            logger.info("Telemetry WebSocket server started on port {}", getPort());
        }
    }

    // --- Simple HTTP Server ---

    private static class HttpServer {
        private final int port;
        private volatile boolean running = true;
        private ServerSocket socket;
        private final ExecutorService httpExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "Dashboard-HTTP-Worker");
            t.setDaemon(true);
            return t;
        });

        public HttpServer(int port) {
            this.port = port;
        }

        public void start() {
            try {
                socket = new ServerSocket(port);
                logger.info("HTTP Server listening on port {}", port);

                while (running && !socket.isClosed()) {
                    Socket client = socket.accept();
                    client.setSoTimeout(5000); // 5s timeout to prevent slowloris
                    httpExecutor.submit(() -> handle(client));
                }
            } catch (IOException e) {
                if (running)
                    logger.error("HTTP Server error", e);
            }
        }

        public void stop() throws IOException {
            running = false;
            if (socket != null && !socket.isClosed())
                socket.close();
            httpExecutor.shutdownNow();
        }

        private void handle(Socket client) {
            try (client;
                    OutputStream out = client.getOutputStream();
                    java.io.InputStream in = client.getInputStream()) {

                // Read request line
                try (java.util.Scanner scanner = new java.util.Scanner(in).useDelimiter("\\r\\n")) {
                    if (scanner.hasNext()) {
                        String requestLine = scanner.next();
                        String[] parts = requestLine.split(" ");
                        if (parts.length >= 2) {

                            String path = parts[1];

                            if (path.startsWith("/api/")) {
                                handleApi(out, path);
                            } else {
                                handleStatic(out);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                logger.warn("HTTP request failed", e);
            }
        }

        private void handleApi(OutputStream out, String path) throws IOException {
            String json = "{}";

            if (path.equals("/api/status")) {
                // Return simple status
                json = "{\"status\":\"ok\", \"robots\":" + DashboardServer.instance.getRobotsJson() + "}";
            } else if (path.equals("/api/config")) {
                json = "{\"version\":\"2.2.0\", \"ws_port\":" + (port + 1) + "}";
            } else {
                String response = "HTTP/1.1 404 Not Found\r\n\r\n";
                out.write(response.getBytes(StandardCharsets.UTF_8));
                return;
            }

            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            String header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Content-Length: " + bytes.length + "\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +
                    "\r\n";
            out.write(header.getBytes(StandardCharsets.UTF_8));
            out.write(bytes);
        }

        private void handleStatic(OutputStream out) throws IOException {
            // Determine WebSocket port (hacky but works for this integrated server)
            int wsPort = port + 1;

            String html = DASHBOARD_HTML.replace("{{WS_PORT}}", String.valueOf(wsPort));
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);

            String header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + bytes.length + "\r\n" +
                    "\r\n";

            out.write(header.getBytes(StandardCharsets.UTF_8));
            out.write(bytes);
        }
    }

    // Embedded Dashboard HTML/JS
    private static final String DASHBOARD_HTML = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>JRobotics Dashboard</title>
                <style>
                    body { font-family: 'Segoe UI', sans-serif; background: #1e1e1e; color: #eee; margin: 0; display: flex; height: 100vh; }
                    #sidebar { width: 300px; background: #252526; padding: 20px; border-right: 1px solid #333; }
                    #main { flex: 1; position: relative; overflow: hidden; }
                    h1 { font-size: 1.2rem; margin-top: 0; color: #007acc; }
                    .card { background: #333; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
                    .stat { display: flex; justify-content: space-between; margin-bottom: 5px; }
                    .label { color: #888; }
                    .value { font-family: monospace; font-weight: bold; }
                    canvas { display: block; width: 100%; height: 100%; }
                </style>
            </head>
            <body>
                <div id="sidebar">
                    <h1>JRobotics Dashboard</h1>
                    <div id="status" class="card" style="color: #aaa;">Connecting...</div>
                    <div id="robots"></div>
                </div>
                <div id="main">
                    <canvas id="field"></canvas>
                </div>
                <script>
                    const wsPort = {{WS_PORT}};
                    const ws = new WebSocket(`ws://${location.hostname}:${wsPort}`);
                    const robotsDiv = document.getElementById('robots');
                    const canvas = document.getElementById('field');
                    const ctx = canvas.getContext('2d');
                    let robotData = {};

                    // Resizing
                    function resize() {
                        canvas.width = canvas.parentElement.clientWidth;
                        canvas.height = canvas.parentElement.clientHeight;
                    }
                    window.onresize = resize;
                    resize();

                    ws.onopen = () => {
                        document.getElementById('status').innerText = 'Connected';
                        document.getElementById('status').style.color = '#4ec9b0';
                    };

                    ws.onclose = () => {
                        document.getElementById('status').innerText = 'Disconnected';
                        document.getElementById('status').style.color = '#f44747';
                    };

                    ws.onmessage = (event) => {
                        const msg = JSON.parse(event.data);
                        if (msg.type === 'telemetry') {
                            robotData = msg.robots;
                            updateUI();
                            draw();
                        }
                    };

                    function updateUI() {
                        robotsDiv.innerHTML = '';
                        for (const [id, data] of Object.entries(robotData)) {
                            const card = document.createElement('div');
                            card.className = 'card';
                            let html = `<strong>${data.name}</strong><br>`;
                            html += `<div class="stat"><span class="label">State</span><span class="value">${data.state}</span></div>`;

                            if (data.pose) {
                                html += `<div class="stat"><span class="label">X</span><span class="value">${data.pose.x.toFixed(2)}</span></div>`;
                                html += `<div class="stat"><span class="label">Y</span><span class="value">${data.pose.y.toFixed(2)}</span></div>`;
                                html += `<div class="stat"><span class="label">θ</span><span class="value">${(data.pose.theta * 180 / Math.PI).toFixed(1)}°</span></div>`;
                            }
                            if (data.velocity) {
                                html += `<div class="stat"><span class="label">V</span><span class="value">${data.velocity.linear.toFixed(2)} m/s</span></div>`;
                            }
                            card.innerHTML = html;
                            robotsDiv.appendChild(card);
                        }
                    }

                    function draw() {
                        ctx.fillStyle = '#1e1e1e';
                        ctx.fillRect(0, 0, canvas.width, canvas.height);

                        // Draw grid (1 meter)
                        const centerX = canvas.width / 2;
                        const centerY = canvas.height / 2;
                        const scale = 50; // pixels per meter

                        ctx.strokeStyle = '#333';
                        ctx.lineWidth = 1;
                        ctx.beginPath();

                        for (let x = 0; x < canvas.width; x += scale) {
                            ctx.moveTo(x, 0); ctx.lineTo(x, canvas.height);
                        }
                        for (let y = 0; y < canvas.height; y += scale) {
                            ctx.moveTo(0, y); ctx.lineTo(canvas.width, y);
                        }
                        ctx.stroke();

                        // Draw origin
                        ctx.strokeStyle = '#444';
                        ctx.lineWidth = 2;
                        ctx.beginPath();
                        ctx.moveTo(centerX, 0); ctx.lineTo(centerX, canvas.height);
                        ctx.moveTo(0, centerY); ctx.lineTo(canvas.width, centerY);
                        ctx.stroke();

                        // Draw robots
                        for (const [id, data] of Object.entries(robotData)) {
                            if (!data.pose) continue;

                            const x = centerX + data.pose.x * scale;
                            const y = centerY - data.pose.y * scale; // Invert Y for screen coords
                            const r = 0.2 * scale; // 20cm radius visual

                            ctx.save();
                            ctx.translate(x, y);
                            ctx.rotate(-data.pose.theta); // Invert rotation for screen Y

                            // Body
                            ctx.fillStyle = '#007acc';
                            ctx.beginPath();
                            ctx.arc(0, 0, r, 0, Math.PI * 2);
                            ctx.fill();

                            // Heading indicator
                            ctx.strokeStyle = '#fff';
                            ctx.lineWidth = 2;
                            ctx.beginPath();
                            ctx.moveTo(0, 0);
                            ctx.lineTo(r, 0);
                            ctx.stroke();

                            ctx.restore();

                            // Trail/Ghost
                            // (Could add history here)
                        }
                    }
                </script>
            </body>
            </html>
            """;
}
