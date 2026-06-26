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

@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<TransferenciaResponse> iniciar(@Valid @RequestBody TransferenciaRequest request,
                                                           @AuthenticationPrincipal String mailOrigen) {
        TransferenciaResponse response = transferenciaService.iniciar(request, mailOrigen);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/aceptar")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<TransferenciaResponse> aceptar(@PathVariable Long id,
                                                          @AuthenticationPrincipal String mailDestino) {
        TransferenciaResponse response = transferenciaService.aceptar(id, mailDestino);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<TransferenciaResponse> rechazar(@PathVariable Long id,
                                                           @AuthenticationPrincipal String mailDestino) {
        TransferenciaResponse response = transferenciaService.rechazar(id, mailDestino);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mis-transferencias")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<List<TransferenciaResponse>> misTransferencias(
            @AuthenticationPrincipal String mail) {
        return ResponseEntity.ok(transferenciaService.findByUsuario(mail));
    }

    @GetMapping("/entrada/{idEntrada}")
    public ResponseEntity<List<TransferenciaResponse>> historialEntrada(@PathVariable Long idEntrada) {
        return ResponseEntity.ok(transferenciaService.findHistorialByEntrada(idEntrada));
    }
}