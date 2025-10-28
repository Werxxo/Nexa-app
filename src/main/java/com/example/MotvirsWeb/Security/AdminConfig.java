package com.example.MotvirsWeb.Security;

import com.example.MotvirsWeb.Models.Admin;
import com.example.MotvirsWeb.Repositorios.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminConfig {
    @Bean
    public CommandLineRunner initAdmin(UsuarioRepository usuarioRepository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            if (!usuarioRepository.existsByEmail(Admin.DEFAULT_EMAIL)) {
                Admin admin = new Admin(passwordEncoder);
                usuarioRepository.save(admin);
                System.out.println("✅ Admin creado automáticamente");

            }
        };
    }
}
