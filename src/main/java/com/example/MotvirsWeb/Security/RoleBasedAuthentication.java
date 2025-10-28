package com.example.MotvirsWeb.Security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class RoleBasedAuthentication implements AuthenticationSuccessHandler {
   //redireccion segun su rol
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            response.sendRedirect("/admin/dashboard");
        }
        else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CONDUCTOR"))) {
            response.sendRedirect("/conductor/panel");
        }
        else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PASAJERO"))) {
            response.sendRedirect("/pasajero/panel");
        }
        else {
            response.sendRedirect("/");
        }
    }
}
