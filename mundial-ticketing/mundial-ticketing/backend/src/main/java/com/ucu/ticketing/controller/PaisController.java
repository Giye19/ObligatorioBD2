package com.ucu.ticketing.controller;

import com.ucu.ticketing.model.Pais;
import com.ucu.ticketing.repository.PaisRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/paises")
public class PaisController {

    private final PaisRepository paisRepository;

    public PaisController(PaisRepository paisRepository) {
        this.paisRepository = paisRepository;
    }

    @GetMapping
    public ResponseEntity<List<Pais>> findAll() {
        return ResponseEntity.ok(paisRepository.findAll());
    }
}