package com.ucu.ticketing.controller;

import com.ucu.ticketing.dto.response.RankingCompradorResponse;
import com.ucu.ticketing.dto.response.RankingEventoResponse;
import com.ucu.ticketing.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// expone los endpoints de reportes estadisticos
// solo accesibles para administradores, segun pide la consigna
// ("reportes estadisticos del lado del administrador")
@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // devuelve los eventos con mas entradas vendidas
    @GetMapping("/eventos-mas-vendidos")
    public ResponseEntity<List<RankingEventoResponse>> rankingEventos(
            @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(reporteService.rankingEventos(limite));
    }

    // devuelve el ranking de usuarios mayores compradores
    @GetMapping("/mayores-compradores")
    public ResponseEntity<List<RankingCompradorResponse>> rankingCompradores(
            @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(reporteService.rankingCompradores(limite));
    }
}