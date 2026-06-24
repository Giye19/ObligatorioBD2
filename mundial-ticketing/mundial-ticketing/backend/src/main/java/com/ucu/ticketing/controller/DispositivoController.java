package com.ucu.ticketing.controller;

import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Dispositivo;
import com.ucu.ticketing.repository.DispositivoRepository;
import com.ucu.ticketing.repository.FuncValidacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// expone los endpoints de registro de dispositivos de escaneo autorizados
// segun la consigna, cada dispositivo debe estar vinculado
// obligatoriamente a un funcionario de validacion
@RestController
@RequestMapping("/api/dispositivos")
public class DispositivoController {

    private final DispositivoRepository dispositivoRepository;
    private final FuncValidacionRepository funcValidacionRepository;

    public DispositivoController(DispositivoRepository dispositivoRepository,
                                  FuncValidacionRepository funcValidacionRepository) {
        this.dispositivoRepository = dispositivoRepository;
        this.funcValidacionRepository = funcValidacionRepository;
    }

    // registra un nuevo dispositivo autorizado, vinculado a un funcionario
    // solo accesible para administradores
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Dispositivo> create(@RequestBody Dispositivo dispositivo) {

        if (funcValidacionRepository.findByMail(dispositivo.getMailFuncionario()) == null) {
            throw new BusinessException(
                    "El mail indicado no corresponde a un funcionario de validacion",
                    HttpStatus.BAD_REQUEST);
        }

        if (dispositivoRepository.estaAutorizado(dispositivo.getIdDispositivo())) {
            throw new BusinessException("Ese dispositivo ya esta registrado", HttpStatus.CONFLICT);
        }

        dispositivoRepository.insert(dispositivo);
        return ResponseEntity.ok(dispositivo);
    }
    @GetMapping("/mi-dispositivo")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<Dispositivo> miDispositivo(
            @org.springframework.security.core.annotation.AuthenticationPrincipal String mail) {

        Dispositivo dispositivo = dispositivoRepository.findByFuncionario(mail);

        if (dispositivo == null) {
            throw new BusinessException(
                    "No tiene un dispositivo asignado", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(dispositivo);
    }
}