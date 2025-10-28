package com.example.MotvirsWeb.Controladores;

import com.example.MotvirsWeb.Models.Conductor;
import com.example.MotvirsWeb.Models.EstadoValidacion;
import com.example.MotvirsWeb.Models.Rol;
import com.example.MotvirsWeb.Models.Usuario;
import com.example.MotvirsWeb.Repositorios.UsuarioRepository;
import com.example.MotvirsWeb.Service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AuthControllerDashboard {


    private final UsuarioRepository usuarioRepository;
    private final DashboardService dashboardService;

    // Método principal para mostrar el dashboard
    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model) {
        // 1. Conteos optimizados para los gráficos
        model.addAttribute("totalPasajeros", usuarioRepository.countByRol(Rol.PASAJERO));
        model.addAttribute("totalConductores",
                usuarioRepository.countByRolAndEstadoValidacion(Rol.CONDUCTOR, EstadoValidacion.APROBADO));

        // 2. Datos completos para la tabla
        model.addAttribute("conductoresPendientes", obtenerConductores(EstadoValidacion.PENDIENTE));
        model.addAttribute("conductoresAprobados", obtenerConductores(EstadoValidacion.APROBADO));
        model.addAttribute("conductoresRechazados", obtenerConductores(EstadoValidacion.RECHAZADO));

        model.addAttribute("EstadoValidacion", EstadoValidacion.class);
        return "panel_admin";
    }

    // Método para obtener conductores por estado (reutilizable)
    private List<Conductor> obtenerConductores(EstadoValidacion estado) {
        return usuarioRepository.findByRolAndEstadoValidacion(Rol.CONDUCTOR, estado)
                .stream()
                .map(u -> (Conductor) u)
                .collect(Collectors.toList());
    }

    // Método para aprobar conductores (similar al tuyo)
    @PostMapping("/aprobar-conductor")
    public String aprobarConductor(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        try {
            Conductor conductor = usuarioRepository.findConductorByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

            if (conductor.getEstadoValidacion() != EstadoValidacion.PENDIENTE) {
                throw new RuntimeException("El conductor no está pendiente de aprobación");
            }

            conductor.setEstadoValidacion(EstadoValidacion.APROBADO);
            usuarioRepository.save(conductor);

            redirectAttributes.addFlashAttribute("success",
                    "Conductor " + conductor.getNombre_y_Apellido() + " aprobado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    // Método para rechazar conductores (similar al tuyo)
    @PostMapping("/rechazar-conductor")
    public String rechazarConductor(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        try {
            Conductor conductor = usuarioRepository.findConductorByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

            if (conductor.getEstadoValidacion() != EstadoValidacion.PENDIENTE) {
                throw new RuntimeException("El conductor no está pendiente de aprobación");
            }

            conductor.setEstadoValidacion(EstadoValidacion.RECHAZADO);
            usuarioRepository.save(conductor);

            redirectAttributes.addFlashAttribute("success",
                    "Conductor " + conductor.getNombre_y_Apellido() + " rechazado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard/registros-mensuales")
    @ResponseBody
    public Map<String, Object> obtenerRegistrosMensuales() {
        return dashboardService.obtenerEstadisticasMensuales();
    }

    // Endpoint para AJAX - Actualización parcial de datos
    @GetMapping("/dashboard/actualizar")
    @ResponseBody
    public Map<String, Object> actualizarDatosDashboard() {
        Map<String, Object> datos = new HashMap<>();
        datos.put("pasajeros", usuarioRepository.countByRol(Rol.PASAJERO));
        datos.put("conductoresAprobados",
                usuarioRepository.countByRolAndEstadoValidacion(Rol.CONDUCTOR, EstadoValidacion.APROBADO));
        datos.put("conductoresPendientes",
                usuarioRepository.countByRolAndEstadoValidacion(Rol.CONDUCTOR, EstadoValidacion.PENDIENTE));

        return datos;
    }
}