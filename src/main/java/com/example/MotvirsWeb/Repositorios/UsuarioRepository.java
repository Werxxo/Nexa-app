package com.example.MotvirsWeb.Repositorios;

import com.example.MotvirsWeb.Models.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository <Usuario, String>  {

    boolean existsByEmail(String email);

    Optional<Usuario> findByEmail(String email);

    // Búsquedas por rol (discriminador)
    default Optional<Pasajero> findPasajeroByEmail(String email) {
        return findByEmailAndRol(email, Rol.PASAJERO)
                .map(usuario -> (Pasajero) usuario);
    }

    default Optional<Conductor> findConductorByEmail(String email) {
        return findByEmailAndRol(email, Rol.CONDUCTOR)
                .map(usuario -> (Conductor) usuario);
    }

    default Optional<Admin> findAdminByEmail(String email) {
        return findByEmailAndRol(email, Rol.ADMIN)
                .map(usuario -> (Admin) usuario);
    }

    Optional<Usuario> findByEmailAndRol(String email, Rol rol);

    List<Usuario> findByRolAndEstadoValidacion(Rol rol, EstadoValidacion estado);

    //metodos para los graficos panel del admin
    long countByRol (Rol rol);
    long countByRolAndEstadoValidacion (Rol rol, EstadoValidacion estado);

    // Método para conteo de conductores por múltiples estados
    default long countConductoresByEstados(EstadoValidacion... estados) {
        return this.findByRol(Rol.CONDUCTOR)
                .stream()
                .filter(u -> Arrays.asList(estados).contains(u.getEstadoValidacion()))
                .count();
    }

    // Método adicional para obtener conductores por rol
    List<Usuario> findByRol(Rol rol);

    Optional<Usuario> findByResetPasswordToken (String token);


}
