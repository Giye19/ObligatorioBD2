package com.ucu.ticketing.controller;

import com.ucu.ticketing.dto.request.EventoRequest;
import com.ucu.ticketing.dto.response.EventoResponse;
import com.ucu.ticketing.service.EventoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// expone los endpoints de consulta y gestion de eventos
@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    // devuelve todos los eventos programados
    @GetMapping
    public ResponseEntity<List<EventoResponse>> findAll() {
        return ResponseEntity.ok(eventoService.findAll());
    }

    // devuelve el detalle de un evento especifico
    @GetMapping("/{id}")
    public ResponseEntity<EventoResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(eventoService.findById(id));
    }

    // crea un nuevo evento, solo accesible para administradores
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventoResponse> create(@Valid @RequestBody EventoRequest request,
                                                  @AuthenticationPrincipal String mailAdmin) {
        EventoResponse response = eventoService.create(request, mailAdmin);
        return ResponseEntity.ok(response);
    }
}