package com.ucu.ticketing.controller;

import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Dispositivo;
import com.ucu.ticketing.model.EventoSector;
import com.ucu.ticketing.model.FuncValidacion;
import com.ucu.ticketing.model.Usuario;
import com.ucu.ticketing.repository.DispositivoRepository;
import com.ucu.ticketing.repository.EventoSectorRepository;
import com.ucu.ticketing.repository.FuncValidacionRepository;
import com.ucu.ticketing.repository.UsuarioRepository;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispositivos")
public class DispositivoController {

    private final DispositivoRepository dispositivoRepository;
    private final FuncValidacionRepository funcValidacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final EventoSectorRepository eventoSectorRepository;

    public DispositivoController(DispositivoRepository dispositivoRepository,
                                  FuncValidacionRepository funcValidacionRepository,
                                  UsuarioRepository usuarioRepository,
                                  EventoSectorRepository eventoSectorRepository) {
        this.dispositivoRepository = dispositivoRepository;
        this.funcValidacionRepository = funcValidacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventoSectorRepository = eventoSectorRepository;
    }

    @Data
    public static class DispositivoRequest {
        private String mailFuncionario;
        private String codigoDispositivo;
        private Long idEvento;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Dispositivo> create(@RequestBody DispositivoRequest request) {

        Usuario usuarioFuncionario = usuarioRepository.findByMail(request.getMailFuncionario());
        if (usuarioFuncionario == null) {
            throw new BusinessException(
                    "El mail indicado no corresponde a un usuario existente", HttpStatus.BAD_REQUEST);
        }

        FuncValidacion funcionario = funcValidacionRepository.findByIdUsuario(usuarioFuncionario.getIdUsuario());
        if (funcionario == null) {
            throw new BusinessException(
                    "El mail indicado no corresponde a un funcionario de validacion",
                    HttpStatus.BAD_REQUEST);
        }

        if (dispositivoRepository.findByFuncionario(funcionario.getIdFuncionario()) != null) {
            throw new BusinessException("Ese funcionario ya tiene un dispositivo asignado", HttpStatus.CONFLICT);
        }

        if (request.getIdEvento() == null) {
            throw new BusinessException("Debe indicar el evento a asignar", HttpStatus.BAD_REQUEST);
        }

        List<EventoSector> sectoresDelEvento = eventoSectorRepository.findByEvento(request.getIdEvento());
        if (sectoresDelEvento.isEmpty()) {
            throw new BusinessException(
                    "El evento indicado no tiene sectores habilitados", HttpStatus.BAD_REQUEST);
        }

        Long idDispositivo = dispositivoRepository.insert(
                funcionario.getIdFuncionario(), request.getCodigoDispositivo());

        for (EventoSector eventoSector : sectoresDelEvento) {
            funcValidacionRepository.asignarSector(
                    funcionario.getIdFuncionario(), eventoSector.getIdEventoSector());
        }

        Dispositivo dispositivo = new Dispositivo();
        dispositivo.setIdDispositivo(idDispositivo);
        dispositivo.setIdFuncionario(funcionario.getIdFuncionario());
        dispositivo.setCodigoDispositivo(request.getCodigoDispositivo());
        dispositivo.setActivo(true);

        return ResponseEntity.ok(dispositivo);
    }

    @GetMapping("/mi-dispositivo")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<Dispositivo> miDispositivo(@AuthenticationPrincipal String mail) {

        Usuario usuario = usuarioRepository.findByMail(mail);
        if (usuario == null) {
            throw new BusinessException("El usuario no existe", HttpStatus.UNAUTHORIZED);
        }

        FuncValidacion funcionario = funcValidacionRepository.findByIdUsuario(usuario.getIdUsuario());
        if (funcionario == null) {
            throw new BusinessException("El usuario no es un funcionario", HttpStatus.FORBIDDEN);
        }

        Dispositivo dispositivo = dispositivoRepository.findByFuncionario(funcionario.getIdFuncionario());
        if (dispositivo == null) {
            throw new BusinessException("No tiene un dispositivo asignado", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(dispositivo);
    }
    @Data
    public static class FuncionarioDisponibleResponse {
        private Long idFuncionario;
        private String mail;
        private String nroLegajo;
        private boolean tieneDispositivo;
    }

    @GetMapping("/funcionarios-disponibles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<FuncionarioDisponibleResponse>> funcionariosDisponibles() {

        java.util.List<com.ucu.ticketing.model.FuncValidacion> funcionarios = funcValidacionRepository.findAll();

        java.util.List<FuncionarioDisponibleResponse> response = funcionarios.stream()
                .map(f -> {
                    com.ucu.ticketing.model.Usuario usuario = usuarioRepository.findById(f.getIdUsuario());
                    Dispositivo dispositivoExistente = dispositivoRepository.findByFuncionario(f.getIdFuncionario());

                    FuncionarioDisponibleResponse dto = new FuncionarioDisponibleResponse();
                    dto.setIdFuncionario(f.getIdFuncionario());
                    dto.setMail(usuario != null ? usuario.getMail() : null);
                    dto.setNroLegajo(f.getNroLegajo());
                    dto.setTieneDispositivo(dispositivoExistente != null);
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }
}