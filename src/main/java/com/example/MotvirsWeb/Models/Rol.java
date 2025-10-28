package com.example.MotvirsWeb.Models;

public enum Rol {
    PASAJERO ("ROLE_PASAJERO"),
    CONDUCTOR("ROLE_CONDUCTOR"),
    ADMIN("ROLE_ADMIN");

    private final String authority;

    Rol(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}
