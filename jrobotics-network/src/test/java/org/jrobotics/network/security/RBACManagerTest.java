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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jrobotics.network.security.RBACManager.Permission;
import org.jrobotics.network.security.RBACManager.Role;
import org.jrobotics.network.security.RBACManager.User;

import static org.junit.jupiter.api.Assertions.*;

class RBACManagerTest {

    private RBACManager rbac;

    @BeforeEach
    void setUp() {
        rbac = new RBACManager();
    }

    @Test
    void testDefaultRolesExist() {
        assertNotNull(rbac.getRole("ADMIN"));
        assertNotNull(rbac.getRole("OPERATOR"));
        assertNotNull(rbac.getRole("VIEWER"));
        assertNotNull(rbac.getRole("GUEST"));
    }

    @Test
    void testAdminPermissions() {
        Role admin = rbac.getRole("ADMIN");
        assertTrue(admin.hasPermission(Permission.ALL));
        assertTrue(admin.hasPermission(Permission.ROBOT_CONTROL)); // ALL implies everything
    }

    @Test
    void testGuestPermissions() {
        Role guest = rbac.getRole("GUEST");
        assertFalse(guest.hasPermission(Permission.ROBOT_CONTROL));
        assertTrue(guest.hasPermission(Permission.STATUS_READ));
    }

    @Test
    void testCustomRoleCreation() {
        Role tech = new Role("TECHNICIAN", "Fixes things");
        tech.addPermission(Permission.ROBOT_CONTROL);
        tech.addPermission(Permission.TELEMETRY_READ);

        rbac.addRole(tech);

        Role retrieved = rbac.getRole("TECHNICIAN");
        assertNotNull(retrieved);
        assertTrue(retrieved.hasPermission(Permission.ROBOT_CONTROL));
        assertFalse(retrieved.hasPermission(Permission.USER_MANAGE));
    }

    @Test
    void testUserRegistrationAndAuth() {
        rbac.registerUser("user1", "hash123", "OPERATOR");

        User user = rbac.authenticate("user1", "hash123");
        assertNotNull(user);
        assertEquals("OPERATOR", user.getRole().getName());

        assertTrue(rbac.hasPermission("user1", Permission.ROBOT_CONTROL));
        assertFalse(rbac.hasPermission("user1", Permission.USER_MANAGE));

        assertNull(rbac.authenticate("user1", "wronghash"));
        assertNull(rbac.authenticate("unknown", "hash123"));
    }

    @Test
    void testRegisterWithUnknownRole() {
        assertThrows(IllegalArgumentException.class, () -> {
            rbac.registerUser("user2", "pass", "SUPERMAN");
        });
    }
}
