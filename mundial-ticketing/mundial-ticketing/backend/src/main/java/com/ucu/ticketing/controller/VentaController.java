package com.ucu.ticketing.controller;

import com.ucu.ticketing.dto.request.VentaRequest;
import com.ucu.ticketing.dto.response.VentaResponse;
import com.ucu.ticketing.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// expone los endpoints de compra y consulta de ventas
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // realiza una compra de entradas, solo accesible para usuarios generales
    @PostMapping
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<VentaResponse> comprar(@Valid @RequestBody VentaRequest request,
                                                  @AuthenticationPrincipal String mailComprador) {
        VentaResponse response = ventaService.comprar(request, mailComprador);
        return ResponseEntity.ok(response);
    }

    // devuelve el historico de compras del usuario autenticado
    @GetMapping("/mis-compras")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<List<VentaResponse>> misCompras(@AuthenticationPrincipal String mailComprador) {
        return ResponseEntity.ok(ventaService.findByComprador(mailComprador));
    }
}