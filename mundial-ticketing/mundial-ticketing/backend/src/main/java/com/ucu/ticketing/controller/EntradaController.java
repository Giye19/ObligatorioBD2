package com.ucu.ticketing.controller;

import com.ucu.ticketing.dto.response.EntradaResponse;
import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Entrada;
import com.ucu.ticketing.model.Equipo;
import com.ucu.ticketing.model.Estadio;
import com.ucu.ticketing.model.Evento;
import com.ucu.ticketing.model.EventoSector;
import com.ucu.ticketing.model.Usuario;
import com.ucu.ticketing.model.UsuarioGeneral;
import com.ucu.ticketing.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/entradas")
public class EntradaController {

    private final EntradaRepository entradaRepository;
    private final EventoRepository eventoRepository;
    private final EventoSectorRepository eventoSectorRepository;
    private final EstadioRepository estadioRepository;
    private final EquipoRepository equipoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioGeneralRepository usuarioGeneralRepository;
    private final TransferenciaRepository transferenciaRepository;

    public EntradaController(EntradaRepository entradaRepository,
                              EventoRepository eventoRepository,
                              EventoSectorRepository eventoSectorRepository,
                              EstadioRepository estadioRepository,
                              EquipoRepository equipoRepository,
                              UsuarioRepository usuarioRepository,
                              UsuarioGeneralRepository usuarioGeneralRepository,
                              TransferenciaRepository transferenciaRepository) {
        this.entradaRepository = entradaRepository;
        this.eventoRepository = eventoRepository;
        this.eventoSectorRepository = eventoSectorRepository;
        this.estadioRepository = estadioRepository;
        this.equipoRepository = equipoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioGeneralRepository = usuarioGeneralRepository;
        this.transferenciaRepository = transferenciaRepository;
    }

    @GetMapping("/mis-entradas")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<List<EntradaResponse>> misEntradas(@AuthenticationPrincipal String mail) {

        Usuario usuario = usuarioRepository.findByMail(mail);
        if (usuario == null) {
            throw new BusinessException("El usuario no existe", HttpStatus.UNAUTHORIZED);
        }

        UsuarioGeneral usuarioGeneral = usuarioGeneralRepository.findByIdUsuario(usuario.getIdUsuario());
        if (usuarioGeneral == null) {
            throw new BusinessException("El usuario no es un usuario general", HttpStatus.FORBIDDEN);
        }

        List<Entrada> entradas = entradaRepository.findByPropietario(usuarioGeneral.getIdUsuarioGeneral());

        List<EntradaResponse> response = entradas.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private EntradaResponse toResponse(Entrada entrada) {
        EventoSector eventoSector = eventoSectorRepository.findById(entrada.getIdEventoSector());
        Evento evento = eventoSector != null ? eventoRepository.findById(eventoSector.getIdEvento()) : null;
        Estadio estadio = evento != null ? estadioRepository.findById(evento.getIdEstadio()) : null;

        String equipoLocal = null;
        String equipoVisitante = null;
        if (evento != null) {
            Equipo local = equipoRepository.findById(evento.getIdEquipoLocal());
            Equipo visitante = equipoRepository.findById(evento.getIdEquipoVisitante());
            equipoLocal = local != null ? local.getNombre() : null;
            equipoVisitante = visitante != null ? visitante.getNombre() : null;
        }

        int cantTransferencias = (int) transferenciaRepository.findByEntrada(entrada.getIdEntrada()).stream()
                .filter(t -> "ACEPTADA".equals(t.getEstadoTransferencia()))
                .count();

        return new EntradaResponse(
                entrada.getIdEntrada(),
                evento != null ? evento.getIdEvento() : null,
                estadio != null ? estadio.getNombreEstadio() : null,
                equipoLocal,
                equipoVisitante,
                eventoSector != null ? eventoSector.getLetraSector() : null,
                null,
                entrada.getEstadoEntrada(),
                entrada.getCostoEntrada(),
                cantTransferencias);
    }
}