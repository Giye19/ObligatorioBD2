package com.ucu.ticketing.controller;

import com.ucu.ticketing.dto.request.ValidacionRequest;
import com.ucu.ticketing.dto.response.QrResponse;
import com.ucu.ticketing.dto.response.ValidacionResponse;
import com.ucu.ticketing.service.QrService;
import com.ucu.ticketing.service.ValidacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// expone los endpoints de generacion de qr y validacion de acceso
@RestController
@RequestMapping("/api/validacion")
public class ValidacionController {

    private final QrService qrService;
    private final ValidacionService validacionService;

    public ValidacionController(QrService qrService, ValidacionService validacionService) {
        this.qrService = qrService;
        this.validacionService = validacionService;
    }

    // genera un nuevo qr activo para una entrada
    // el front debe llamar esto cada 30 segundos mientras la app
    // este en primer plano, para mantener el qr siempre vigente
    @PostMapping("/qr/{idEntrada}/generar")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<QrResponse> generarQr(@PathVariable Long idEntrada,
                                                 @AuthenticationPrincipal String mail) {
        return ResponseEntity.ok(qrService.generarNuevo(idEntrada, mail));
    }

    // consulta el qr activo actual sin regenerarlo
    @GetMapping("/qr/{idEntrada}")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<QrResponse> obtenerQr(@PathVariable Long idEntrada,
                                                 @AuthenticationPrincipal String mail) {
        return ResponseEntity.ok(qrService.obtenerActivo(idEntrada, mail));
    }

    // valida el acceso de una entrada escaneada en la puerta
    // solo accesible para funcionarios de validacion
    @PostMapping("/escanear")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<ValidacionResponse> validarAcceso(@Valid @RequestBody ValidacionRequest request) {
        return ResponseEntity.ok(validacionService.validarAcceso(request));
    }
}



