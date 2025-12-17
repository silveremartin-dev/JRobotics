/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.network.security;

import java.util.*;

/**
 * Role-Based Access Control (RBAC) for Robot Dashboard and API.
 *
 * <p>
 * Defines roles and permissions for controlling access to robot
 * operations, telemetry, and configuration.
 * </p>
 *
 * <p>
 * <b>Default Roles:</b>
 * </p>
 * <ul>
 * <li>ADMIN - Full access to all operations</li>
 * <li>OPERATOR - Control and monitor robots</li>
 * <li>VIEWER - Read-only access to telemetry</li>
 * <li>GUEST - Limited public access</li>
 * </ul>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.6.0
 */
public class RBACManager {

    private final Map<String, Role> roles = new HashMap<>();
    private final Map<String, User> users = new HashMap<>();

    /**
     * Creates a new RBAC manager with default roles.
     */
    public RBACManager() {
        initializeDefaultRoles();
    }

    private void initializeDefaultRoles() {
        // Admin role - full access
        Role admin = new Role("ADMIN", "Administrator with full access");
        admin.addPermission(Permission.ALL);
        roles.put(admin.getName(), admin);

        // Operator role - control and monitor
        Role operator = new Role("OPERATOR", "Can control and monitor robots");
        operator.addPermission(Permission.ROBOT_CONTROL);
        operator.addPermission(Permission.ROBOT_MONITOR);
        operator.addPermission(Permission.TELEMETRY_READ);
        operator.addPermission(Permission.CONFIG_READ);
        roles.put(operator.getName(), operator);

        // Viewer role - read-only
        Role viewer = new Role("VIEWER", "Read-only access");
        viewer.addPermission(Permission.TELEMETRY_READ);
        viewer.addPermission(Permission.STATUS_READ);
        roles.put(viewer.getName(), viewer);

        // Guest role - minimal access
        Role guest = new Role("GUEST", "Limited public access");
        guest.addPermission(Permission.STATUS_READ);
        roles.put(guest.getName(), guest);
    }

    /**
     * Registers a new user.
     *
     * @param username     the username
     * @param passwordHash the password hash (SHA-256)
     * @param roleName     the role to assign
     * @return the created user
     */
    public User registerUser(String username, String passwordHash, String roleName) {
        Role role = roles.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Unknown role: " + roleName);
        }

        User user = new User(username, passwordHash, role);
        users.put(username, user);
        return user;
    }

    /**
     * Authenticates a user.
     *
     * @param username     the username
     * @param passwordHash the password hash
     * @return the user if authenticated, null otherwise
     */
    public User authenticate(String username, String passwordHash) {
        User user = users.get(username);
        if (user != null && user.getPasswordHash().equals(passwordHash)) {
            return user;
        }
        return null;
    }

    /**
     * Checks if a user has a specific permission.
     *
     * @param username   the username
     * @param permission the permission to check
     * @return true if user has permission
     */
    public boolean hasPermission(String username, Permission permission) {
        User user = users.get(username);
        if (user == null)
            return false;
        return user.getRole().hasPermission(permission);
    }

    /**
     * Adds a custom role.
     *
     * @param role the role to add
     */
    public void addRole(Role role) {
        roles.put(role.getName(), role);
    }

    /**
     * Gets all roles.
     *
     * @return collection of roles
     */
    public Collection<Role> getRoles() {
        return Collections.unmodifiableCollection(roles.values());
    }

    /**
     * Gets a role by name.
     *
     * @param name role name
     * @return the role, or null
     */
    public Role getRole(String name) {
        return roles.get(name);
    }

    /**
     * Available permissions.
     */
    public enum Permission {
        ALL, // Full access
        ROBOT_CONTROL, // Start, stop, move robots
        ROBOT_MONITOR, // View robot status
        ROBOT_CONFIGURE, // Change robot configuration
        TELEMETRY_READ, // Read sensor data
        TELEMETRY_WRITE, // Inject test data
        STATUS_READ, // Read system status
        CONFIG_READ, // Read configuration
        CONFIG_WRITE, // Modify configuration
        USER_MANAGE, // Manage users
        FIRMWARE_UPDATE // OTA updates
    }

    /**
     * Role definition.
     */
    public static class Role {
        private final String name;
        private final String description;
        private final Set<Permission> permissions = EnumSet.noneOf(Permission.class);

        public Role(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public void addPermission(Permission permission) {
            permissions.add(permission);
        }

        public void removePermission(Permission permission) {
            permissions.remove(permission);
        }

        public boolean hasPermission(Permission permission) {
            return permissions.contains(Permission.ALL) || permissions.contains(permission);
        }

        public Set<Permission> getPermissions() {
            return Collections.unmodifiableSet(permissions);
        }
    }

    /**
     * User definition.
     */
    public static class User {
        private final String username;
        private final String passwordHash;
        private final Role role;
        private String token;
        private long tokenExpiry;

        public User(String username, String passwordHash, Role role) {
            this.username = username;
            this.passwordHash = passwordHash;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public String getPasswordHash() {
            return passwordHash;
        }

        public Role getRole() {
            return role;
        }

        public void setToken(String token, long expiryMs) {
            this.token = token;
            this.tokenExpiry = System.currentTimeMillis() + expiryMs;
        }

        public String getToken() {
            return token;
        }

        public boolean isTokenValid() {
            return token != null && System.currentTimeMillis() < tokenExpiry;
        }

        public void invalidateToken() {
            this.token = null;
            this.tokenExpiry = 0;
        }
    }
}
