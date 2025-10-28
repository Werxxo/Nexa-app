package com.example.MotvirsWeb.Repositorios;

import com.example.MotvirsWeb.Models.Conductor;
import com.example.MotvirsWeb.Models.Vehiculo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface VehiculoRepository extends MongoRepository<Vehiculo, String> {
    Optional<Vehiculo> findByPlaca(String placa);
    List<Vehiculo> findByConductorAsignado(Conductor conductor);
    List<Vehiculo> findByActivo(boolean activo);
}