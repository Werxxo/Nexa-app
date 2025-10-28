package com.example.MotvirsWeb.Models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Usuarios")
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@TypeAlias("PASAJERO")
public class Pasajero extends Usuario{
    private String nombreCompleto;
    private String telefono;

    public Pasajero() {
        this.setRol(Rol.PASAJERO);
    }
}
