package net.awords.agriecombackend.security;

import java.util.Set;

/**
 * Central definition for platform roles.
 */
public final class RoleConstants {

    private RoleConstants() {
    }

    public static final String USER = "USER";
    public static final String MERCHANT = "MERCHANT";
    public static final String ADMIN = "ADMIN";

    public static Set<String> allRoles() {
        return Set.of(USER, MERCHANT, ADMIN);
    }
}
