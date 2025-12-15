# Credits

## JRobotics v2 - World-Class General Purpose Robotics API

### Authors

**Silvère Martin-Michiellot** - Lead Developer & Architect

- Email: <silvere.martin@gmail.com>
- Original concept, architecture design, and project leadership

**Gemini AI Assistant** - Co-Developer

- Implementation assistance, code generation, and documentation

### License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

### Third-Party Dependencies

#### Core Dependencies

| Library | License | Usage |
|---------|---------|-------|
| [SLF4J](https://www.slf4j.org/) | MIT | Logging facade |
| [Logback](https://logback.qos.ch/) | EPL/LGPL | Logging implementation |
| [Jackson](https://github.com/FasterXML/jackson) | Apache 2.0 | JSON processing |

#### Physics & Simulation

| Library | License | Usage |
|---------|---------|-------|
| [jMonkeyEngine](https://jmonkeyengine.org/) | BSD 3-Clause | 3D engine, physics |
| [jBullet](http://jbullet.advel.cz/) | Zlib | Physics simulation |

#### Testing

| Library | License | Usage |
|---------|---------|-------|
| [JUnit 5](https://junit.org/junit5/) | EPL 2.0 | Unit testing |
| [Mockito](https://site.mockito.org/) | MIT | Mocking framework |
| [JMH](https://openjdk.org/projects/code-tools/jmh/) | GPL 2.0 (CE) | Benchmarking |

### Algorithm Credits

#### Path Planning

- **A* Algorithm**: Hart, P. E., Nilsson, N. J., & Raphael, B. (1968). A Formal Basis for the Heuristic Determination of Minimum Cost Paths.
- **RRT (Rapidly-exploring Random Trees)**: LaValle, S. M. (1998). Rapidly-exploring random trees: A new tool for path planning.
- **Dijkstra's Algorithm**: Dijkstra, E. W. (1959). A note on two problems in connexion with graphs.

#### Sensor Fusion

- **Kalman Filter**: Kalman, R. E. (1960). A New Approach to Linear Filtering and Prediction Problems.
- **Complementary Filter**: Based on works by Mahony, R., Hamel, T., & Pflimlin, J. M.

#### Control Systems

- **PID Control**: Minorsky, N. (1922). Directional Stability of Automatically Steered Bodies.

### Acknowledgments

Special thanks to:

- The open-source robotics community
- ROS/ROS2 project for inspiration
- All contributors and testers

---

*If you use JRobotics in your project, please consider crediting us!*
