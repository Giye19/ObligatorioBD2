package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.response.RankingCompradorResponse;
import com.ucu.ticketing.dto.response.RankingEventoResponse;
import com.ucu.ticketing.model.Estadio;
import com.ucu.ticketing.model.Evento;
import com.ucu.ticketing.repository.EntradaRepository;
import com.ucu.ticketing.repository.EstadioRepository;
import com.ucu.ticketing.repository.EventoRepository;
import com.ucu.ticketing.repository.VentaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// contiene la logica de los reportes estadisticos pedidos por la
// consigna: eventos con mas entradas vendidas, y ranking de
// usuarios mayores compradores
@Service
public class ReporteService {

    // cantidad de filas a devolver por defecto en cada ranking
    private static final int LIMITE_DEFAULT = 10;

    private final EntradaRepository entradaRepository;
    private final VentaRepository ventaRepository;
    private final EventoRepository eventoRepository;
    private final EstadioRepository estadioRepository;

    public ReporteService(EntradaRepository entradaRepository,
                           VentaRepository ventaRepository,
                           EventoRepository eventoRepository,
                           EstadioRepository estadioRepository) {
        this.entradaRepository = entradaRepository;
        this.ventaRepository = ventaRepository;
        this.eventoRepository = eventoRepository;
        this.estadioRepository = estadioRepository;
    }

    // devuelve el ranking de eventos con mas entradas vendidas,
    // con datos legibles de estadio y equipos participantes
    public List<RankingEventoResponse> rankingEventos(int limite) {
        List<Object[]> filas = entradaRepository.findRankingEventos(limite);

        return filas.stream()
                .map(fila -> {
                    Integer idEvento = (Integer) fila[0];
                    Integer cantidad = (Integer) fila[1];

                    Evento evento = eventoRepository.findById(idEvento);
                    Estadio estadio = estadioRepository.findById(evento.getIdEstadio());

                    String equipoLocal = null;
                    String equipoVisitante = null;
                    for (Object[] eq : eventoRepository.findEquiposByEvento(idEvento)) {
                        if ("LOCAL".equals(eq[1])) {
                            equipoLocal = (String) eq[0];
                        } else {
                            equipoVisitante = (String) eq[0];
                        }
                    }

                    return new RankingEventoResponse(
                            idEvento,
                            estadio != null ? estadio.getNombre() : null,
                            equipoLocal,
                            equipoVisitante,
                            evento.getFechaEvento(),
                            cantidad);
                })
                .collect(Collectors.toList());
    }

    // usa el limite default de filas
    public List<RankingEventoResponse> rankingEventos() {
        return rankingEventos(LIMITE_DEFAULT);
    }

    // devuelve el ranking de usuarios con mas entradas compradas
    public List<RankingCompradorResponse> rankingCompradores(int limite) {
        List<Object[]> filas = ventaRepository.findRankingCompradores(limite);

        return filas.stream()
                .map(fila -> new RankingCompradorResponse((String) fila[0], (Integer) fila[1]))
                .collect(Collectors.toList());
    }

    // usa el limite default de filas
    public List<RankingCompradorResponse> rankingCompradores() {
        return rankingCompradores(LIMITE_DEFAULT);
    }
}