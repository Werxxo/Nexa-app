package com.example.MotvirsWeb.Models;

import com.example.MotvirsWeb.Repositorios.UsuarioRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "Usuarios")
@TypeAlias("USUARIO")
@CompoundIndex(name = "email_rol_unique_idx",
        def = "{'email': 1, 'rol': 1}",
        unique = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

public abstract class Usuario {
    @Id
    private ObjectId id;

    @Indexed(unique = false)
    private String email;
    private String password;
    private LocalDateTime fechaRegistro;
    private Rol rol;
    private EstadoValidacion estadoValidacion;

    @Field(name = "usuario_reset_password_token")
    @Indexed
    private String resetPasswordToken;

    @Field(name = "usuario_reset_password_token_expiry")
    @Indexed
    private LocalDateTime resetPasswordTokenExpiry;

    /**
     * Factory method para crear instancias concretas
     */


    public static Usuario crearUsuario(Rol rol , String email, UsuarioRepository repository) {

        return switch (rol) {
            case PASAJERO -> Pasajero.builder()
                    .rol(Rol.PASAJERO)
                    .estadoValidacion(EstadoValidacion.APROBADO)
                    .build();
            case CONDUCTOR -> Conductor.builder()
                    .rol(Rol.CONDUCTOR)
                    .estadoValidacion(EstadoValidacion.PENDIENTE)
                    .licencia(0)
                    .soat(0L)
                    .tarjeta_de_propiedad(0L)
                    .build();
            case ADMIN -> Admin.builder()
                    .rol(Rol.ADMIN)
                    .estadoValidacion(EstadoValidacion.APROBADO)
                    .build();
            default -> throw new IllegalArgumentException("Rol no válido");
        };
    }

    public String getIdString() {
        return id != null ? id.toString() : null;
    }
}