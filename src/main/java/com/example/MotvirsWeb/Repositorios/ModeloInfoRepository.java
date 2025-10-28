package com.example.MotvirsWeb.Repositorios;

import com.example.MotvirsWeb.Models.ModeloInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ModeloInfoRepository extends MongoRepository <ModeloInfo , String> {

    Optional<ModeloInfo> findFirstByNombreModeloOrderByFechaEntrenamientoDesc(String nombreModelo);

}
