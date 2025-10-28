package com.example.MotvirsWeb.Controladores;

import com.example.MotvirsWeb.Models.*;
import com.example.MotvirsWeb.Repositorios.ServicioRepository;
import com.example.MotvirsWeb.Repositorios.UsuarioRepository;
import com.example.MotvirsWeb.socket.WebSocketService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/conductor")
public class ControllerConductor {

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private WebSocketService webSocketService;

    @GetMapping("/panel")
    public String mapearpanelconductor(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!(usuario instanceof Conductor)) {
            throw new RuntimeException("El usuario no es un conductor");
        }

        Conductor conductor = (Conductor) usuario;

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setConductor(new Vehiculo.ConductorEmbedded(
                conductor.getId(),
                conductor.getNombre_y_Apellido(),
                conductor.getEmail()
        ));

        List<Servicio> serviciosDisponibles = servicioRepository.findByEstado(EstadoServicio.SOLICITADO);
        List<Servicio> serviciosEnCurso = servicioRepository.findByConductorIdAndEstado(conductor.getId(), EstadoServicio.EN_CURSO);
        List<Servicio> historialServicios = servicioRepository.findByConductorIdAndEstado(conductor.getId(), EstadoServicio.FINALIZADO);
        List<Servicio> serviciosCalificados = servicioRepository.findByConductorIdAndCalificacionPasajeroIsNotNull(conductor.getId());
        model.addAttribute("conductor", conductor);
        model.addAttribute("serviciosDisponibles", serviciosDisponibles);
        model.addAttribute("serviciosEnCurso", serviciosEnCurso);
        model.addAttribute("historialServicios", historialServicios);
        model.addAttribute("serviciosCalificados", serviciosCalificados);

        return "/conductor/panel_conductor";
    }

    @PostMapping("/aceptar-servicio/{servicioId}")
    @ResponseBody
    public ResponseEntity<?> aceptarServicio(@PathVariable String servicioId) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!(usuario instanceof Conductor)) {
                return ResponseEntity.badRequest().body("El usuario no es un conductor");
            }

            Conductor conductor = (Conductor) usuario;
            Servicio servicio = servicioRepository.findById(servicioId)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

            if (servicio.getEstado() != EstadoServicio.SOLICITADO) {
                return ResponseEntity.badRequest().body("El servicio no está disponible para aceptar");
            }


            servicio.setConductor(new Servicio.ConductorEmbedded(conductor));
            servicio.setEstado(EstadoServicio.ACEPTADO);
            servicio.setFechaAceptacion(LocalDateTime.now());
            servicioRepository.save(servicio);

            webSocketService.notificarCambioEstadoServicio(servicio);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Servicio aceptado con éxito",
                    "servicioId", servicioId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al aceptar el servicio: " + e.getMessage());
        }
    }

    @PostMapping("/iniciar-servicio/{servicioId}")
    @ResponseBody
    public ResponseEntity<?> iniciarServicio(@PathVariable String servicioId) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!(usuario instanceof Conductor)) {
                return ResponseEntity.badRequest().body("El usuario no es un conductor");
            }

            Conductor conductor = (Conductor) usuario;
            Servicio servicio = servicioRepository.findById(servicioId)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

            // Cambiado de SOLICITADO a ACEPTADO como estado previo requerido
            if (servicio.getEstado() != EstadoServicio.ACEPTADO) {
                return ResponseEntity.badRequest().body("El servicio no está en estado ACEPTADO");
            }

            servicio.setEstado(EstadoServicio.EN_CURSO);
            servicio.setFechaInicio(LocalDateTime.now());
            servicioRepository.save(servicio);
            webSocketService.notificarCambioEstadoServicio(servicio);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Viaje iniciado con éxito",
                    "servicioId", servicioId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al iniciar el viaje: " + e.getMessage());
        }
    }


    @PostMapping("/finalizar-servicio/{servicioId}")
    @ResponseBody
    public ResponseEntity<?> finalizarServicio(@PathVariable String servicioId,
                                               @RequestBody(required = false) Map<String, Object> datosFinalizacion) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!(usuario instanceof Conductor)) {
                return ResponseEntity.badRequest().body("El usuario no es un conductor");
            }

            Conductor conductor = (Conductor) usuario;
            Servicio servicio = servicioRepository.findById(servicioId)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

            if (servicio.getEstado() != EstadoServicio.EN_CURSO ||
                    !servicio.getConductor().getId().equals(conductor.getId())) {
                return ResponseEntity.badRequest().body("No puedes finalizar este servicio");
            }

            servicio.setEstado(EstadoServicio.FINALIZADO);
            servicio.setFechaFinalizacion(LocalDateTime.now());

            // Eliminamos la calificación aquí ya que ahora se maneja en el controlador de Rating
            servicioRepository.save(servicio);
            webSocketService.notificarCambioEstadoServicio(servicio);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Servicio finalizado con éxito. Redirigiendo a calificación...",
                    "servicioId", servicioId,
                    "redirectUrl", "/rating?servicioId=" + servicioId + "&rol=conductor"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al finalizar el servicio: " + e.getMessage());
        }
    }

    @PostMapping("/cancelar-servicio/{servicioId}")
    @ResponseBody
    public ResponseEntity<?> cancelarServicio(@PathVariable String servicioId) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!(usuario instanceof Conductor)) {
                return ResponseEntity.badRequest().body("El usuario no es un conductor");
            }

            Conductor conductor = (Conductor) usuario;
            Servicio servicio = servicioRepository.findById(servicioId)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

            if (!servicio.getConductor().getId().equals(conductor.getId())) {
                return ResponseEntity.badRequest().body("No puedes cancelar este servicio");
            }

            if (servicio.getEstado() == EstadoServicio.FINALIZADO ||
                    servicio.getEstado() == EstadoServicio.CANCELADO) {
                return ResponseEntity.badRequest().body("El servicio ya está completado o cancelado");
            }

            servicio.setEstado(EstadoServicio.CANCELADO);
            servicio.setFechaCancelacion(LocalDateTime.now());
            servicioRepository.save(servicio);
            webSocketService.notificarCambioEstadoServicio(servicio); // Notificar cambio de estado

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Servicio cancelado con éxito",
                    "servicioId", servicioId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al cancelar el servicio: " + e.getMessage());
        }
    }

    @GetMapping("/servicios-disponibles")
    @ResponseBody
    public ResponseEntity<List<Servicio>> obtenerServiciosDisponibles() {
        List<Servicio> servicios = servicioRepository.findByEstado(EstadoServicio.SOLICITADO);
        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/servicios-en-curso")
    @ResponseBody
    public ResponseEntity<List<Servicio>> obtenerServiciosEnCurso() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!(usuario instanceof Conductor)) {
            throw new RuntimeException("El usuario no es un conductor");
        }

        Conductor conductor = (Conductor) usuario;
        List<Servicio> servicios = servicioRepository.findByConductorIdAndEstado(conductor.getId(), EstadoServicio.EN_CURSO);
        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/servicios/{servicioId}")
    @ResponseBody
    public ResponseEntity<Servicio> obtenerServicio(@PathVariable String servicioId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        return ResponseEntity.ok(servicio);
    }

    @GetMapping("/historial")
    @ResponseBody
    public ResponseEntity<List<Servicio>> obtenerHistorial(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String pasajero) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!(usuario instanceof Conductor)) {
            throw new RuntimeException("El usuario no es un conductor");
        }

        Conductor conductor = (Conductor) usuario;
        List<Servicio> historial = servicioRepository.findByConductorIdAndEstado(conductor.getId(), EstadoServicio.FINALIZADO);

        if (fecha != null || pasajero != null) {
            historial = historial.stream()
                    .filter(servicio -> {
                        boolean matchFecha = fecha != null ? servicio.getFechaHora().toLocalDate().toString().equals(fecha) : true;
                        boolean matchPasajero = pasajero != null ? servicio.getPasajero().getNombre().toLowerCase().contains(pasajero.toLowerCase()) : true;
                        return matchFecha && matchPasajero;
                    })
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(historial);
    }

    @GetMapping("/ingresos")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerIngresos(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) String fecha) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!(usuario instanceof Conductor)) {
            throw new RuntimeException("El usuario no es un conductor");
        }

        Conductor conductor = (Conductor) usuario;
        List<Servicio> servicios = servicioRepository.findByConductorIdAndEstado(conductor.getId(), EstadoServicio.FINALIZADO);

        LocalDate today = LocalDate.now();
        List<Servicio> filteredServicios = servicios;

        if (fecha != null) {
            filteredServicios = servicios.stream()
                    .filter(s -> s.getFechaHora().toLocalDate().toString().equals(fecha))
                    .collect(Collectors.toList());
        } else if (periodo != null) {
            switch (periodo) {
                case "año":
                    filteredServicios = servicios.stream()
                            .filter(s -> s.getFechaHora().toLocalDate().getYear() == today.getYear())
                            .collect(Collectors.toList());
                    break;
                case "mes":
                    filteredServicios = servicios.stream()
                            .filter(s -> s.getFechaHora().toLocalDate().getYear() == today.getYear() &&
                                    s.getFechaHora().toLocalDate().getMonth() == today.getMonth())
                            .collect(Collectors.toList());
                    break;
                case "semana":
                    LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                    LocalDate endOfWeek = startOfWeek.plusDays(6);
                    filteredServicios = servicios.stream()
                            .filter(s -> !s.getFechaHora().toLocalDate().isBefore(startOfWeek) &&
                                    !s.getFechaHora().toLocalDate().isAfter(endOfWeek))
                            .collect(Collectors.toList());
                    break;
                case "día":
                    filteredServicios = servicios.stream()
                            .filter(s -> s.getFechaHora().toLocalDate().equals(today))
                            .collect(Collectors.toList());
                    break;
                default:
                    break;
            }
        }

        List<Map<String, Object>> ingresos = filteredServicios.stream()
                .map(servicio -> {
                    Map<String, Object> ingreso = new HashMap<>();
                    ingreso.put("fecha", servicio.getFechaHora().toLocalDate().toString());
                    ingreso.put("monto", servicio.getTarifa());
                    ingreso.put("descripcion", "Viaje con " + servicio.getPasajero().getNombre());
                    return ingreso;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ingresos);
    }


}