package com.galasd.thesisx.security;

public class RoleEntity {
    public static final String COMMON = "common";
    // This role implicitly allows access to all views.
    public static final String ADMIN = "admin";

    private RoleEntity() {
        // Static methods and fields only
    }

    public static String[] getAllRoles() {
        return new String[]{COMMON, ADMIN};
    }
}
