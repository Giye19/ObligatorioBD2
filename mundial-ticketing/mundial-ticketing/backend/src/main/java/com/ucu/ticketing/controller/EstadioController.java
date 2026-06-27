package com.ucu.ticketing.controller;

import com.ucu.ticketing.dto.request.EstadioRequest;
import com.ucu.ticketing.dto.response.EstadioResponse;
import com.ucu.ticketing.model.PaisSede;
import com.ucu.ticketing.repository.PaisSedeRepository;
import com.ucu.ticketing.service.EstadioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estadios")
public class EstadioController {

    private final EstadioService estadioService;
    private final PaisSedeRepository paisSedeRepository;

    public EstadioController(EstadioService estadioService,
                              PaisSedeRepository paisSedeRepository) {
        this.estadioService = estadioService;
        this.paisSedeRepository = paisSedeRepository;
    }

    @GetMapping
    public ResponseEntity<List<EstadioResponse>> findAll() {
        return ResponseEntity.ok(estadioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstadioResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(estadioService.findById(id));
    }

    /**
     * devuelve los paises sede reales del mundial (USA, Mexico, Canada),
     * sin depender de si ya existen estadios creados en ellos. se usa
     * para poblar el selector de pais al crear un nuevo estadio.
     */
    @GetMapping("/paises-sede")
    public ResponseEntity<List<PaisSede>> findPaisesSede() {
        return ResponseEntity.ok(paisSedeRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EstadioResponse> create(@Valid @RequestBody EstadioRequest request,
                                                   @AuthenticationPrincipal String mailAdmin) {
        EstadioResponse response = estadioService.create(request, mailAdmin);
        return ResponseEntity.ok(response);
    }
}