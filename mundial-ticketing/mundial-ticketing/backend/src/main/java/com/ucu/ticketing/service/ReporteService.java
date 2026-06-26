package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.response.RankingCompradorResponse;
import com.ucu.ticketing.dto.response.RankingEventoResponse;
import com.ucu.ticketing.model.Equipo;
import com.ucu.ticketing.model.Estadio;
import com.ucu.ticketing.model.Evento;
import com.ucu.ticketing.model.Usuario;
import com.ucu.ticketing.repository.EntradaRepository;
import com.ucu.ticketing.repository.EquipoRepository;
import com.ucu.ticketing.repository.EstadioRepository;
import com.ucu.ticketing.repository.EventoRepository;
import com.ucu.ticketing.repository.UsuarioGeneralRepository;
import com.ucu.ticketing.repository.UsuarioRepository;
import com.ucu.ticketing.repository.VentaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private static final int LIMITE_DEFAULT = 10;

    private final EntradaRepository entradaRepository;
    private final VentaRepository ventaRepository;
    private final EventoRepository eventoRepository;
    private final EstadioRepository estadioRepository;
    private final EquipoRepository equipoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioGeneralRepository usuarioGeneralRepository;

    public ReporteService(EntradaRepository entradaRepository,
                           VentaRepository ventaRepository,
                           EventoRepository eventoRepository,
                           EstadioRepository estadioRepository,
                           EquipoRepository equipoRepository,
                           UsuarioRepository usuarioRepository,
                           UsuarioGeneralRepository usuarioGeneralRepository) {
        this.entradaRepository = entradaRepository;
        this.ventaRepository = ventaRepository;
        this.eventoRepository = eventoRepository;
        this.estadioRepository = estadioRepository;
        this.equipoRepository = equipoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioGeneralRepository = usuarioGeneralRepository;
    }

    public List<RankingEventoResponse> rankingEventos(int limite) {
        List<Object[]> filas = entradaRepository.findRankingEventos(limite);

        return filas.stream()
                .map(fila -> {
                    Long idEvento = (Long) fila[0];
                    Integer cantidad = (Integer) fila[1];

                    Evento evento = eventoRepository.findById(idEvento);
                    Estadio estadio = estadioRepository.findById(evento.getIdEstadio());
                    Equipo local = equipoRepository.findById(evento.getIdEquipoLocal());
                    Equipo visitante = equipoRepository.findById(evento.getIdEquipoVisitante());

                    return new RankingEventoResponse(
                            idEvento,
                            estadio != null ? estadio.getNombreEstadio() : null,
                            local != null ? local.getNombre() : null,
                            visitante != null ? visitante.getNombre() : null,
                            evento.getFechaEvento(),
                            cantidad);
                })
                .collect(Collectors.toList());
    }

    public List<RankingEventoResponse> rankingEventos() {
        return rankingEventos(LIMITE_DEFAULT);
    }

    public List<RankingCompradorResponse> rankingCompradores(int limite) {
        List<Object[]> filas = ventaRepository.findRankingCompradores(limite);

        return filas.stream()
                .map(fila -> {
                    Long idUsuarioGeneral = (Long) fila[0];
                    Integer cantidad = (Integer) fila[1];

                    Long idUsuario = usuarioGeneralRepository.findIdUsuarioById(idUsuarioGeneral);
                    Usuario usuario = idUsuario != null ? usuarioRepository.findById(idUsuario) : null;

                    return new RankingCompradorResponse(
                            usuario != null ? usuario.getMail() : null, cantidad);
                })
                .collect(Collectors.toList());
    }

    public List<RankingCompradorResponse> rankingCompradores() {
        return rankingCompradores(LIMITE_DEFAULT);
    }
}