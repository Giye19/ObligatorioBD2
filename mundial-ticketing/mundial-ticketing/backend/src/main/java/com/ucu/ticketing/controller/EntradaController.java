package com.ucu.ticketing.controller;

import com.ucu.ticketing.dto.response.EntradaResponse;
import com.ucu.ticketing.model.Entrada;
import com.ucu.ticketing.model.Estadio;
import com.ucu.ticketing.model.Evento;
import com.ucu.ticketing.repository.EntradaRepository;
import com.ucu.ticketing.repository.EstadioRepository;
import com.ucu.ticketing.repository.EventoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

// expone el endpoint de consulta de entradas asignadas a un usuario
// la logica es simple (una consulta), por eso no se creo un service
// aparte y se resuelve directo en el controller con los repositories
@RestController
@RequestMapping("/api/entradas")
public class EntradaController {

    private final EntradaRepository entradaRepository;
    private final EventoRepository eventoRepository;
    private final EstadioRepository estadioRepository;

    public EntradaController(EntradaRepository entradaRepository,
                              EventoRepository eventoRepository,
                              EstadioRepository estadioRepository) {
        this.entradaRepository = entradaRepository;
        this.eventoRepository = eventoRepository;
        this.estadioRepository = estadioRepository;
    }

    // devuelve todas las entradas que el usuario autenticado tiene
    // asignadas actualmente, sin importar de que venta vinieron
    @GetMapping("/mis-entradas")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<List<EntradaResponse>> misEntradas(@AuthenticationPrincipal String mail) {

        List<Entrada> entradas = entradaRepository.findByPropietario(mail);

        List<EntradaResponse> response = entradas.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private EntradaResponse toResponse(Entrada entrada) {
        Evento evento = eventoRepository.findById(entrada.getIdEvento());
        Estadio estadio = estadioRepository.findById(entrada.getIdEstadio());

        String equipoLocal = null;
        String equipoVisitante = null;
        for (Object[] fila : eventoRepository.findEquiposByEvento(entrada.getIdEvento())) {
            if ("LOCAL".equals(fila[1])) {
                equipoLocal = (String) fila[0];
            } else {
                equipoVisitante = (String) fila[0];
            }
        }

        return new EntradaResponse(
                entrada.getIdEntrada(),
                entrada.getIdEvento(),
                estadio != null ? estadio.getNombre() : null,
                equipoLocal,
                equipoVisitante,
                entrada.getLetraSector(),
                entrada.getMailPropietario(),
                entrada.getEstado(),
                entrada.getCostoEntrada(),
                entrada.getCantTransferencias());
    }
}