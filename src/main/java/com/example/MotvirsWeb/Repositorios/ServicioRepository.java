package com.example.MotvirsWeb.Repositorios;

import com.example.MotvirsWeb.Models.EstadoServicio;
import com.example.MotvirsWeb.Models.Servicio;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ServicioRepository extends MongoRepository<Servicio, String> {
    List<Servicio> findByEstado(EstadoServicio estado);
    List<Servicio> findByConductorIdAndEstado(ObjectId conductorId, EstadoServicio estado);
    List<Servicio> findByPasajeroIdAndEstado(ObjectId pasajeroId, EstadoServicio estado);

    List<Servicio> findByConductorIdAndCalificacionPasajeroIsNotNull(ObjectId conductorId);
    List<Servicio> findByPasajeroIdAndCalificacionConductorIsNotNull(ObjectId pasajeroId);
}