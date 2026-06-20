package com.ucu.ticketing.controller;

import com.ucu.ticketing.dto.request.TransferenciaRequest;
import com.ucu.ticketing.dto.response.TransferenciaResponse;
import com.ucu.ticketing.service.TransferenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// expone los endpoints de transferencia de entradas entre usuarios
@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    // inicia una transferencia de una entrada hacia otro usuario
    @PostMapping
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<TransferenciaResponse> iniciar(@Valid @RequestBody TransferenciaRequest request,
                                                           @AuthenticationPrincipal String mailOrigen) {
        TransferenciaResponse response = transferenciaService.iniciar(request, mailOrigen);
        return ResponseEntity.ok(response);
    }

    // acepta una transferencia pendiente dirigida al usuario autenticado
    @PostMapping("/{id}/aceptar")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<TransferenciaResponse> aceptar(@PathVariable Integer id,
                                                          @AuthenticationPrincipal String mailDestino) {
        TransferenciaResponse response = transferenciaService.aceptar(id, mailDestino);
        return ResponseEntity.ok(response);
    }

    // rechaza una transferencia pendiente dirigida al usuario autenticado
    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<TransferenciaResponse> rechazar(@PathVariable Integer id,
                                                           @AuthenticationPrincipal String mailDestino) {
        TransferenciaResponse response = transferenciaService.rechazar(id, mailDestino);
        return ResponseEntity.ok(response);
    }

    // devuelve el historico de transferencias del usuario autenticado
    @GetMapping("/mis-transferencias")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<List<TransferenciaResponse>> misTransferencias(
            @AuthenticationPrincipal String mail) {
        return ResponseEntity.ok(transferenciaService.findByUsuario(mail));
    }

    // devuelve el historico completo de transferencias de una entrada
    // (cadena de custodia), accesible para cualquier usuario autenticado
    @GetMapping("/entrada/{idEntrada}")
    public ResponseEntity<List<TransferenciaResponse>> historialEntrada(@PathVariable Integer idEntrada) {
        return ResponseEntity.ok(transferenciaService.findHistorialByEntrada(idEntrada));
    }
}