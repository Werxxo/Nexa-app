package com.example.MotvirsWeb.Controladores;

import com.example.MotvirsWeb.Models.EstadoServicio;
import com.example.MotvirsWeb.Models.Servicio;
import com.example.MotvirsWeb.Repositorios.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/rating")
public class ControllerRating {

    @Autowired
    private ServicioRepository servicioRepository;

    @GetMapping
    @PreAuthorize("hasRole(#rol ? #rol.toUpperCase() : 'USER')")
    public String mostrarRating(@RequestParam("servicioId") String servicioId,
                                @RequestParam(value = "rol", required = false) String rol,
                                Model model, Principal principal) {

        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        model.addAttribute("servicioId", servicioId);
        model.addAttribute("rol", rol); // "conductor" o "pasajero"


        String nombreOtro = "";
        if ("conductor".equals(rol)) {
            // El conductor está calificando, muestra el nombre del pasajero
            if (servicio.getPasajero() != null) {
                nombreOtro = servicio.getPasajero().getNombre();
            }
        } else if ("pasajero".equals(rol)) {

            if (servicio.getConductor() != null) {
                nombreOtro = servicio.getConductor().getNombre();
            }
        }
        model.addAttribute("nombreOtro", nombreOtro);


        return "rating";
    }

    @PostMapping("/calificar")
    @PreAuthorize("hasRole(#rol ? #rol.toUpperCase() : 'USER')")  // Valida en POST también
    public String calificarServicio(
            @RequestParam String servicioId,
            @RequestParam Double calificacion,
            @RequestParam(required = false) String comentarios,
            @RequestParam String rol,
            Authentication authentication) {

        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        // Validación adicional: rol param debe coincidir con auth (seguridad)
        String authRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", "").toLowerCase())
                .orElse("user");
        if (!rol.equals(authRole)) {
            throw new RuntimeException("Rol no autorizado para esta calificación");
        }

        if ("pasajero".equals(rol)) {
            servicio.setCalificacionConductor(calificacion);
            servicio.setComentariosConductor(comentarios);
        } else if ("conductor".equals(rol)) {
            servicio.setCalificacionPasajero(calificacion);
            servicio.setComentariosPasajero(comentarios);
        } else {
            throw new RuntimeException("Rol inválido");
        }

        // Marcar el servicio como completado
        servicio.setEstado(EstadoServicio.FINALIZADO);
        servicioRepository.save(servicio);

        // Redirigir basado en el rol del parámetro (no en auth, pero validado arriba)
        if ("pasajero".equals(rol)) {
            return "redirect:/pasajero/panel";
        } else {
            return "redirect:/conductor/panel";
        }
    }
}