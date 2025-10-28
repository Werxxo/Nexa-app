package com.example.MotvirsWeb.Models;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "Usuarios")
@TypeAlias("CONDUCTOR")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder

public class Conductor extends Usuario{
    private String nombre_y_Apellido;

    private Long ntelefono;
    @Indexed(unique = true)
    private int licencia;
    @Indexed(unique = true)
    private Long soat;
    @Indexed(unique = true)
    private Long tarjeta_de_propiedad;

    @Field(name = "conductor_reset_password_token")
    @Indexed
    private String resetPasswordToken;

    @Field(name = "conductor_reset_password_token_expiry")
    @Indexed
    private LocalDateTime resetPasswordTokenExpiry;


    private List<ObjectId> vehiculos;


    public void agregarVehiculo(Vehiculo vehiculo) {
        if (this.vehiculos == null) {
            this.vehiculos = new ArrayList<>();
        }
        if (vehiculo != null && vehiculo.getId() != null) {
            this.vehiculos.add(new ObjectId(vehiculo.getId()));
        }
    }


    public Conductor() {
        this.setRol(Rol.CONDUCTOR);
        this.setEstadoValidacion(EstadoValidacion.PENDIENTE);
    }
}
