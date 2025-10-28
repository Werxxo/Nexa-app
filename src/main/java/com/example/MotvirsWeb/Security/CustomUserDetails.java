package com.example.MotvirsWeb.Security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {
    private final String id;

    public CustomUserDetails(String id, String username, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             boolean enabled) {
        super(username, password, enabled, true, true, true, authorities);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}