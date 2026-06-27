package com.ucu.ticketing.controller;

import com.ucu.ticketing.dto.request.ValidacionRequest;
import com.ucu.ticketing.dto.response.QrResponse;
import com.ucu.ticketing.dto.response.ValidacionResponse;
import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Dispositivo;
import com.ucu.ticketing.repository.DispositivoRepository;
import com.ucu.ticketing.service.QrService;
import com.ucu.ticketing.service.ValidacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/validacion")
public class ValidacionController {

    private final QrService qrService;
    private final ValidacionService validacionService;
    private final DispositivoRepository dispositivoRepository;

    public ValidacionController(QrService qrService,
                                 ValidacionService validacionService,
                                 DispositivoRepository dispositivoRepository) {
        this.qrService = qrService;
        this.validacionService = validacionService;
        this.dispositivoRepository = dispositivoRepository;
    }

    /**
     * abre o reactiva la sesion qr de una entrada. se llama una
     * vez al entrar a la pantalla de qr de una entrada.
     */
    @PostMapping("/qr/{idEntrada}/abrir")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<QrResponse> abrirSesionQr(@PathVariable Long idEntrada,
                                                     @AuthenticationPrincipal String mail) {
        return ResponseEntity.ok(qrService.abrirSesion(idEntrada, mail));
    }

    /**
     * obtiene el qr vigente de una entrada, regenerandolo si
     * corresponde. el cliente llama este endpoint periodicamente
     * (cada pocos segundos) mientras la pantalla de qr esta abierta.
     */
    @GetMapping("/qr/{idEntrada}")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<QrResponse> obtenerQrActual(@PathVariable Long idEntrada,
                                                       @AuthenticationPrincipal String mail) {
        return ResponseEntity.ok(qrService.obtenerActual(idEntrada, mail));
    }

    /**
     * cierra la sesion qr de una entrada, por ejemplo cuando el
     * usuario sale de la pantalla de qr
     */
    @PostMapping("/qr/{idQr}/cerrar")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<Void> cerrarSesionQr(@PathVariable Long idQr,
                                                @AuthenticationPrincipal String mail) {
        qrService.cerrarSesion(idQr, mail);
        return ResponseEntity.ok().build();
    }

    /**
     * valida el acceso de una entrada escaneada en la puerta.
     * resuelve el codigo de dispositivo (texto) a su id interno
     * antes de delegar la validacion al procedimiento sp_validar_qr.
     */
    @PostMapping("/escanear")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<ValidacionResponse> validarAcceso(@Valid @RequestBody ValidacionRequest request) {

        Dispositivo dispositivo = dispositivoRepository.findByCodigo(request.getIdDispositivo());
        if (dispositivo == null) {
            throw new BusinessException("Dispositivo no autorizado", HttpStatus.FORBIDDEN);
        }

        ValidacionRequest requestConIdNumerico = new ValidacionRequest();
        requestConIdNumerico.setTokenQr(request.getTokenQr());
        requestConIdNumerico.setIdDispositivo(String.valueOf(dispositivo.getIdDispositivo()));
        requestConIdNumerico.setPuertaIngreso(request.getPuertaIngreso());

        return ResponseEntity.ok(validacionService.validarAcceso(requestConIdNumerico));
    }
}