package com.example.MotvirsWeb.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "info_modelos_ml") // Nueva colección en MongoDB
@Data
@NoArgsConstructor

public class ModeloInfo {
    @Id
    private String id;
    private String nombreModelo;
    private LocalDateTime fechaEntrenamiento;
    private String arbolDecisionTexto; //  String del árbol J48
    private int instanciasUsadas;

    public ModeloInfo(String nombreModelo, LocalDateTime fecha, String arbol, int instancias) {
        this.nombreModelo = nombreModelo;
        this.fechaEntrenamiento = fecha;
        this.arbolDecisionTexto = arbol;
        this.instanciasUsadas = instancias;
    }

}