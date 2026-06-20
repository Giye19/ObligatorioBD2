package com.ucu.ticketing.controller;

import com.ucu.ticketing.model.Equipo;
import com.ucu.ticketing.repository.EquipoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// expone los endpoints de consulta y alta de equipos participantes
@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    private final EquipoRepository equipoRepository;

    public EquipoController(EquipoRepository equipoRepository) {
        this.equipoRepository = equipoRepository;
    }

    // devuelve todos los equipos registrados
    @GetMapping
    public ResponseEntity<List<Equipo>> findAll() {
        return ResponseEntity.ok(equipoRepository.findAll());
    }

    // da de alta un nuevo equipo, solo accesible para administradores
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> create(@RequestBody String nombre) {
        equipoRepository.insert(nombre);
        return ResponseEntity.ok().build();
    }
}