package com.ucu.ticketing.controller;

import com.ucu.ticketing.dto.request.EstadioRequest;
import com.ucu.ticketing.dto.response.EstadioResponse;
import com.ucu.ticketing.service.EstadioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// expone los endpoints de consulta y gestion de estadios
@RestController
@RequestMapping("/api/estadios")
public class EstadioController {

    private final EstadioService estadioService;

    public EstadioController(EstadioService estadioService) {
        this.estadioService = estadioService;
    }

    // devuelve todos los estadios, accesible para cualquier usuario autenticado
    @GetMapping
    public ResponseEntity<List<EstadioResponse>> findAll() {
        return ResponseEntity.ok(estadioService.findAll());
    }

    // devuelve el detalle de un estadio especifico con sus sectores
    @GetMapping("/{id}")
    public ResponseEntity<EstadioResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(estadioService.findById(id));
    }

    // crea un nuevo estadio, solo accesible para administradores
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EstadioResponse> create(@Valid @RequestBody EstadioRequest request,
                                                   @AuthenticationPrincipal String mailAdmin) {
        EstadioResponse response = estadioService.create(request, mailAdmin);
        return ResponseEntity.ok(response);
    }
}