package com.example.MotvirsWeb.Controladores;

import com.example.MotvirsWeb.Models.*;
import com.example.MotvirsWeb.Repositorios.ServicioRepository;
import com.example.MotvirsWeb.Repositorios.UsuarioRepository;
import com.example.MotvirsWeb.socket.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pasajero")
public class ControllerPasajero {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private WebSocketService webSocketService;

    @GetMapping("/panel")
    public String mapeopanelpasajero(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!(usuario instanceof Pasajero)) {
            throw new RuntimeException("El usuario no es un pasajero");
        }

        Pasajero pasajero = (Pasajero) usuario;
        model.addAttribute("pasajero", pasajero);

        return "/pasajero/panel_pasajero";
    }

    @PostMapping("/solicitar-viaje")
    @ResponseBody
    public ResponseEntity<?> solicitarViaje(@RequestBody Map<String, Object> solicitud) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!(usuario instanceof Pasajero)) {
                return ResponseEntity.badRequest().body("El usuario no es un pasajero");
            }

            Pasajero pasajero = (Pasajero) usuario;

            // Validar campos requeridos
            if (!solicitud.containsKey("origen") || !solicitud.containsKey("destino") ||
                    !solicitud.containsKey("ubicacionOrigen") ||
                    !solicitud.containsKey("ubicacionDestino") ||
                    !solicitud.containsKey("tarifa") ||
                    !solicitud.containsKey("distanciaKM")    ) {
                return ResponseEntity.badRequest().body("Faltan campos requeridos en la solicitud");
            }

            Servicio servicio = new Servicio();
            servicio.setPasajero(new Servicio.PasajeroEmbedded(
                    pasajero.getId(),
                    pasajero.getNombreCompleto(),
                    pasajero.getEmail()
            ));

            servicio.setOrigen((String) solicitud.get("origen"));
            servicio.setDestino((String) solicitud.get("destino"));

            @SuppressWarnings("unchecked")
            Map<String, Double> ubicacionOrigen = (Map<String, Double>) solicitud.get("ubicacionOrigen");
            servicio.setUbicacionOrigen(new Servicio.Coordenada(
                    ubicacionOrigen.get("latitud"),
                    ubicacionOrigen.get("longitud")
            ));

            @SuppressWarnings("unchecked")
            Map<String, Double> ubicacionDestino = (Map<String, Double>) solicitud.get("ubicacionDestino");
            servicio.setUbicacionDestino(new Servicio.Coordenada(
                    ubicacionDestino.get("latitud"),
                    ubicacionDestino.get("longitud")
            ));

            Object tarifaObj = solicitud.get("tarifa");
            double tarifa = tarifaObj instanceof Number ? ((Number) tarifaObj).doubleValue() : 0.0;
            if (tarifa <= 0) {
                return ResponseEntity.badRequest().body("La tarifa debe ser mayor a 0");
            }
            servicio.setTarifa(tarifa);

            Object distObj = solicitud.get("distanciaKM");
            double distancia = distObj instanceof Number ? ((Number) distObj).doubleValue() : 0.0;
            if (distancia <= 0) {
                return ResponseEntity.badRequest().body("La distancia debe ser mayor a 0");
            }
            servicio.setDistanciaKM(distancia);

            String fechaHoraStr = (String) solicitud.get("fechaHora");
            servicio.setFechaHora(fechaHoraStr != null ?
                    LocalDateTime.parse(fechaHoraStr, DateTimeFormatter.ISO_DATE_TIME) :
                    LocalDateTime.now());

            if (solicitud.containsKey("metodoPago")) {
                servicio.setMetodoPago((String) solicitud.get("metodoPago"));
            }

            servicio.setEstado(EstadoServicio.SOLICITADO);

            servicioRepository.save(servicio);

            // Notificar solo a los conductores sobre el nuevo servicio disponible
            webSocketService.notificarNuevoServicioDisponible(servicio);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Viaje solicitado con éxito. Esperando conductor...",
                    "servicioId", servicio.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    @PostMapping("/actualizar-estado/{servicioId}")
    @ResponseBody
    public ResponseEntity<?> actualizarEstadoServicio(
            @PathVariable String servicioId,
            @RequestBody Map<String, String> request) {

        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!(usuario instanceof Pasajero)) {
                return ResponseEntity.badRequest().body("El usuario no es un pasajero");
            }

            Servicio servicio = servicioRepository.findById(servicioId)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

            if (!servicio.getPasajero().getId().equals(((Pasajero) usuario).getId())) {
                return ResponseEntity.badRequest().body("No puedes modificar este servicio");
            }

            if (!request.containsKey("estado")) {
                return ResponseEntity.badRequest().body("El campo 'estado' es requerido");
            }

            String nuevoEstado = request.get("estado");
            try {
                servicio.setEstado(EstadoServicio.valueOf(nuevoEstado));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Estado no válido: " + nuevoEstado);
            }

            if (nuevoEstado.equals("EN_CURSO")) {
                servicio.setFechaInicio(LocalDateTime.now());
            } else if (nuevoEstado.equals("FINALIZADO")) {
                servicio.setFechaFinalizacion(LocalDateTime.now());
            }

            servicioRepository.save(servicio);
            webSocketService.notificarCambioEstadoServicio(servicio);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Estado del servicio actualizado",
                    "servicioId", servicioId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar el estado: " + e.getMessage());
        }
    }

    @PostMapping("/calificar-servicio/{servicioId}")
    @ResponseBody
    public ResponseEntity<?> calificarServicio(@PathVariable String servicioId,
                                               @RequestBody Map<String, Object> datosCalificacion) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!(usuario instanceof Pasajero)) {
                return ResponseEntity.badRequest().body("El usuario no es un pasajero");
            }

            Servicio servicio = servicioRepository.findById(servicioId)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

            if (servicio.getEstado() != EstadoServicio.FINALIZADO ||
                    !servicio.getPasajero().getId().equals(((Pasajero) usuario).getId())) {
                return ResponseEntity.badRequest().body("No puedes calificar este servicio");
            }

            if (datosCalificacion.containsKey("calificacion")) {
                try {
                    double calificacion = Double.parseDouble(datosCalificacion.get("calificacion").toString());
                    if (calificacion < 0 || calificacion > 5) {
                        return ResponseEntity.badRequest().body("La calificación debe estar entre 0 y 5");
                    }
                    servicio.setCalificacionConductor(calificacion); // Cambiado a calificacionConductor
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body("Formato de calificación no válido");
                }
            }

            if (datosCalificacion.containsKey("comentarios")) {
                servicio.setComentariosConductor(datosCalificacion.get("comentarios").toString()); // Cambiado a comentariosConductor
            }

            servicioRepository.save(servicio);
            webSocketService.notificarCambioEstadoServicio(servicio);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Calificación enviada con éxito",
                    "servicioId", servicioId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al enviar la calificación: " + e.getMessage());
        }
    }

    @GetMapping("/historial")
    @ResponseBody
    public ResponseEntity<List<Servicio>> obtenerHistorialPasajero(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String conductor) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!(usuario instanceof Pasajero)) {
            throw new RuntimeException("El usuario no es un pasajero");
        }

        Pasajero pasajero = (Pasajero) usuario;
        List<Servicio> historial = servicioRepository.findByPasajeroIdAndEstado(pasajero.getId(), EstadoServicio.FINALIZADO);

        if (fecha != null || conductor != null) {
            historial = historial.stream()
                    .filter(servicio -> {
                        boolean matchFecha = fecha != null ?
                                servicio.getFechaHora().toLocalDate().toString().equals(fecha) : true;
                        boolean matchConductor = conductor == null || conductor.isEmpty() ||
                                (servicio.getConductor() != null &&
                                        servicio.getConductor().getNombre() != null &&
                                        servicio.getConductor().getNombre().toLowerCase().contains(conductor.toLowerCase()));
                        return matchFecha && matchConductor;
                    })
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(historial);
    }

    @GetMapping("/servicios/{servicioId}")
    @ResponseBody
    public ResponseEntity<Servicio> obtenerServicioPasajero(@PathVariable String servicioId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!(usuario instanceof Pasajero)) {
            throw new RuntimeException("El usuario no es un pasajero");
        }

        Pasajero pasajero = (Pasajero) usuario;
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        if (!servicio.getPasajero().getId().equals(pasajero.getId())) {
            throw new RuntimeException("No tienes permiso para ver este servicio");
        }

        return ResponseEntity.ok(servicio);
    }

    @PostMapping("/cancelar-viaje/{servicioId}")
    @ResponseBody
    public ResponseEntity<?> cancelarViajePasajero(@PathVariable String servicioId) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no autenticado encontrado"));

            if (!(usuario instanceof Pasajero)) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acción no permitida para este tipo de usuario"));
            }

            Pasajero pasajero = (Pasajero) usuario;
            Servicio servicio = servicioRepository.findById(servicioId)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + servicioId));

            // Verificar que el pasajero autenticado es el dueño del servicio
            // Compara usando getId() de Pasajero (que es ObjectId) y el ID almacenado en PasajeroEmbedded
            if (servicio.getPasajero() == null || servicio.getPasajero().getId() == null ||
                    !servicio.getPasajero().getId().equals(pasajero.getId())) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No tienes permiso para cancelar este servicio"));
            }


            if (servicio.getEstado() != EstadoServicio.SOLICITADO && servicio.getEstado() != EstadoServicio.ACEPTADO) {

                return ResponseEntity.badRequest().body(Map.of("error", "No puedes cancelar un servicio que ya está " + servicio.getEstado()));
            }

            servicio.setEstado(EstadoServicio.CANCELADO);
            servicio.setFechaCancelacion(LocalDateTime.now());
            Servicio servicioCancelado = servicioRepository.save(servicio);


            webSocketService.notificarCambioEstadoServicio(servicioCancelado);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Viaje cancelado con éxito",
                    "servicioId", servicioId
            ));
        } catch (RuntimeException e) {
            // Log específico para "No encontrado"
            if (e.getMessage().contains("Servicio no encontrado")) {
                System.err.println("Intento de cancelar servicio no existente: " + servicioId + " - " + e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }

            System.err.println("Error RuntimeException al cancelar viaje por pasajero: " + e.getMessage());
            e.printStackTrace(); // Imprime stack trace para más detalles
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Error al procesar la cancelación: " + e.getMessage()));
        } catch (Exception e) {

            System.err.println("Error inesperado al cancelar viaje por pasajero: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno del servidor al procesar la cancelación"));
        }
    }

}