package com.example.MotvirsWeb.socket;

import com.example.MotvirsWeb.Models.EstadoServicio;
import com.example.MotvirsWeb.Models.Servicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notificarCambioEstadoServicio(Servicio servicio) {
        // Notificar al pasajero
        messagingTemplate.convertAndSend("/topic/pasajero/" + servicio.getPasajero().getId(), servicio);

        // Notificar al conductor (si existe)
        if (servicio.getConductor() != null) {
            messagingTemplate.convertAndSend("/topic/conductor/" + servicio.getConductor().getId(), servicio);
        }
    }

    public void notificarNuevoServicioDisponible(Servicio servicio) {
        // Notificar a todos los conductores sobre nuevo servicio disponible
        messagingTemplate.convertAndSend("/topic/servicios-disponibles", servicio);
    }
}