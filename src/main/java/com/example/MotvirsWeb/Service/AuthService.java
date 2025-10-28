package com.example.MotvirsWeb.Service;

import com.example.MotvirsWeb.Models.*;
import com.example.MotvirsWeb.Repositorios.UsuarioRepository;
import com.example.MotvirsWeb.Security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Registra un nuevo usuario en el sistema - Versión mejorada
     */
    public Usuario registrarUsuario(String email, String password, Rol rol, String nombre) {
        // Creación del usuario con validación integrada
        Usuario usuario = Usuario.crearUsuario(rol, email, usuarioRepository);

        usuario.setPassword(passwordEncoder.encode(password));

        // Campos específicos por tipo de usuario
        if (usuario instanceof Pasajero pasajero) {
            pasajero.setNombreCompleto(nombre);
        } else if (usuario instanceof Conductor conductor) {
            conductor.setNombre_y_Apellido(nombre);
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Método para registro de conductores con documentos
     */
    public Conductor registrarConductor(String nombre, String email, String password,
                                        int licencia, Long soat, Long tarjetaPropiedad) {
        // Validación mejorada
        if (usuarioRepository.findByEmailAndRol(email, Rol.CONDUCTOR).isPresent()) {
            throw new IllegalStateException("Ya existe un conductor registrado con este email: " + email);
        }

        Conductor conductor = Conductor.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .rol(Rol.CONDUCTOR)
                .estadoValidacion(EstadoValidacion.PENDIENTE)
                .nombre_y_Apellido(nombre)
                .licencia(licencia)
                .soat(soat)
                .tarjeta_de_propiedad(tarjetaPropiedad)
                .build();

        return usuarioRepository.save(conductor);
    }


    public UserDetails autenticarUsuario(String email, String password) {
        try {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                throw new BadCredentialsException("Credenciales inválidas");
            }

            // Verificación adicional de estado para conductores
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            if (usuario.getRol() == Rol.CONDUCTOR &&
                    usuario.getEstadoValidacion() != EstadoValidacion.APROBADO) {
                throw new DisabledException("Cuenta de conductor no aprobada");
            }

            return userDetails;
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("Credenciales inválidas", e);
        }
    }
}