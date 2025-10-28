package com.example.MotvirsWeb.Service;

import com.example.MotvirsWeb.Models.EstadoValidacion;
import com.example.MotvirsWeb.Models.Rol;
import com.example.MotvirsWeb.Models.Usuario;
import com.example.MotvirsWeb.Repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UsuarioRepository usuarioRepository;

    public Map<String, Object> obtenerEstadisticasMensuales() {
        Map<String, Object> datos = new HashMap<>();

        // Obtener todos los usuarios necesarios
        List<Usuario> pasajeros = usuarioRepository.findByRol(Rol.PASAJERO);
        List<Usuario> conductoresAprobados = usuarioRepository.findByRolAndEstadoValidacion(
                Rol.CONDUCTOR, EstadoValidacion.APROBADO);

        // Procesar los datos para el gráfico
        int[] pasajerosData = contarRegistrosPorMes(pasajeros);
        int[] conductoresData = contarRegistrosPorMes(conductoresAprobados);

        datos.put("pasajeros", pasajerosData);
        datos.put("conductores", conductoresData);

        return datos;
    }

    private int[] contarRegistrosPorMes(List<Usuario> usuarios) {
        int[] conteoMensual = new int[12];

        for (Usuario usuario : usuarios) {
            if (usuario.getFechaRegistro() != null) {
                int mes = usuario.getFechaRegistro().getMonthValue() - 1; // Convertir a 0-11
                if (mes >= 0 && mes < 12) {
                    conteoMensual[mes]++;
                }
            }
        }

        return conteoMensual;
    }
}