# JRobotics v2 — Networking & Communication Architecture

## Overview

This document outlines the networking subsystem of **JRobotics v2**, covering socket architecture, secure wire protocols, peer-to-peer swarm synchronization, remote teleoperation, and external IoT/ROS bridges.

---

## 1. Network Subsystem Architecture

The `jrobotics-network` module provides high-throughput, low-latency, and cryptographically secured communications designed specifically for resource-constrained embedded robotics (Raspberry Pi, Jetson) and high-performance simulation clusters.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        High-Level Robot Services                       │
│    (Remote Master/Slave, Swarm DSM, Federated Learning, OTA Updates)   │
├────────────────────────────────────────────────────────────────────────┤
│                       Security & Access Control Layer                  │
│        (TLS 1.3 Transport, RBAC Token Auth, SHA-256 Digest Engine)     │
├────────────────────────────────────────────────────────────────────────┤
│                       Wire Protocol & Serialization                    │
│      (Length-Prefixed Jackson JSON Framing, Bounded Packet Allocator)  │
├───────────────────────────────────┬────────────────────────────────────┤
│         TCP Connection Pool       │        UDP Mesh / Multicast        │
│    (Point-to-Point Streaming)     │      (Swarm Discovery & DSM)       │
└───────────────────────────────────┴────────────────────────────────────┘
```

---

## 2. Wire Protocol & Framing

To prevent Java Native Deserialization Remote Code Execution (RCE, CWE-502) and memory-exhaustion Denial of Service (DoS), all TCP and UDP messages follow a structured binary/JSON format:

### 2.1 Packet Frame Format

```
+--------------------------+-------------------------------------------+
| Length Header (4 Bytes)  | Payload (JSON UTF-8 String, Max 10 MB)   |
| Big-Endian 32-bit uint   | {"id":"...", "type":"...", "data":{...}}  |
+--------------------------+-------------------------------------------+
```

1. **Header**: 4-byte integer indicating the payload length in bytes.
2. **Payload Protection**: Allocator bounds reject any packet claiming $> 10\text{ MB}$ before reading data into memory.
3. **Data Mapping**: Deserialized directly into strongly-typed `NetworkMessage` records via Jackson ObjectMapper.

---

## 3. Security & Cryptographic Controls

### 3.1 Transport Layer Security (TLS 1.3)
`SecureTransport` provisions non-blocking and standard SSL/TLS sockets:
- Enforces strict server and client mutual authentication (mTLS).
- Applies `SSLParameters.setEndpointIdentificationAlgorithm("HTTPS")` for automated TLS hostname verification (mitigating CWE-297 man-in-the-middle attacks).

### 3.2 Side-Channel Resistant Authentication (RBAC)
`AuthenticationService` and `RBACManager` manage session tokens and user credentials:
- Constant-time verification using `MessageDigest.isEqual()` to prevent timing side-channel attacks (CWE-208).
- Role-Based Access Control enforcing `ADMIN`, `OPERATOR`, `VIEWER`, and `ROBOT_NODE` permissions.

### 3.3 Hardened Over-The-Air (OTA) Updates
`OTAUpdateManager` manages remote binary firmware distribution:
- **Sandbox Isolation**: Path containment validation prevents directory traversal (CWE-22).
- **Streaming Validation**: Constant-memory SHA-256 checksum computation via `DigestInputStream`.
- **Atomic Swap & Rollback**: Automatic rollback if the new firmware fails initial health check probe.

---

## 4. Swarm & Distributed Systems

### 4.1 Distributed Shared Memory (DSM)
`DistributedSharedMemory` allows a fleet of robots to share a synchronized distributed state table:
- **Multicast Discovery**: UDP Multicast group join/leave notifications.
- **Conflict Resolution**: Last-Write-Wins (LWW) with Lamport / monotonic timestamps.
- **Resilience**: Heartbeat monitoring and dynamic node discovery.

### 4.2 Federated Swarm Learning
`FederatedLearningCoordinator` coordinates distributed AI model training across peer robots:
- Periodically collects local parameter updates from member robots.
- Executes Federated Averaging (FedAvg) aggregation.
- Redistributes the updated global model weights without sharing raw sensory training datasets.

---

## 5. Transport Protocols Comparison

| Transport | Implementation | Latency (LAN) | Overhead | Best Use Case |
| :--- | :--- | :--- | :--- | :--- |
| **Java NIO TCP (TLS)** | `TcpServer` / `TcpClient` | $< 1.0\text{ ms}$ | Low (JSON) | Telemetry, Teleoperation, Mission control |
| **UDP Multicast** | `UdpPeerNode` / `DSM` | $< 0.3\text{ ms}$ | Minimal | Swarm discovery, Broadcast state sync |
| **WebSocket / HTTP** | `DashboardServer` | $1.5 - 3.0\text{ ms}$ | Moderate | Browser UI, Telemetry visualization |
| **MQTT** | `MqttBridge` | $5.0 - 15.0\text{ ms}$ | Minimal | Enterprise IoT Cloud (AWS/Azure) |
| **ROS 2 DDS** | `Ros2Bridge` | $< 1.0\text{ ms}$ | Low | ROS 2 ecosystem interoperability |

---

## 6. Code Example: Secure Teleoperation Client

```java
import org.jrobotics.network.server.TcpClient;
import org.jrobotics.network.security.TLSConfig;
import org.jrobotics.network.NetworkMessage;

// 1. Configure mutual TLS
TLSConfig tls = TLSConfig.builder()
    .keyStorePath("/etc/jrobotics/certs/client-keystore.p12")
    .keyStorePassword("SecretClientPass")
    .requireClientAuth(true)
    .build();

// 2. Connect client to robot base
TcpClient client = new TcpClient("192.168.1.120", 8443, tls);
client.setMessageHandler((msg) -> {
    System.out.println("Received telemetry: " + msg.getType());
});
client.connect();

// 3. Authenticate and send command
client.sendMessage("operator-session", "AUTH_TOKEN", "eyJhbGciOi...");
client.sendMessage("operator-session", "DRIVE_VELOCITY", Map.of("vx", 0.75, "wz", -0.2));
```

---

*Networking Specification Version 2.0.0 — JRobotics Framework*

