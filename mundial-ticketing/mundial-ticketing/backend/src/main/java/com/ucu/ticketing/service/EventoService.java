package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.request.EventoRequest;
import com.ucu.ticketing.dto.request.SectorHabilitadoRequest;
import com.ucu.ticketing.dto.response.EventoResponse;
import com.ucu.ticketing.dto.response.SectorHabilitadoResponse;
import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Estadio;
import com.ucu.ticketing.model.Evento;
import com.ucu.ticketing.model.Sector;
import com.ucu.ticketing.repository.AdmPaisSedeRepository;
import com.ucu.ticketing.repository.EntradaRepository;
import com.ucu.ticketing.repository.EquipoRepository;
import com.ucu.ticketing.repository.EstadioRepository;
import com.ucu.ticketing.repository.EventoRepository;
import com.ucu.ticketing.repository.SectorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// contiene la logica de programacion de eventos (encuentros deportivos)
// valida no superposicion de horarios, jurisdiccion del admin,
// existencia de equipos y sectores antes de crear el evento
@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final EstadioRepository estadioRepository;
    private final SectorRepository sectorRepository;
    private final EquipoRepository equipoRepository;
    private final EntradaRepository entradaRepository;
    private final AdmPaisSedeRepository admPaisSedeRepository;

    public EventoService(EventoRepository eventoRepository,
                          EstadioRepository estadioRepository,
                          SectorRepository sectorRepository,
                          EquipoRepository equipoRepository,
                          EntradaRepository entradaRepository,
                          AdmPaisSedeRepository admPaisSedeRepository) {
        this.eventoRepository = eventoRepository;
        this.estadioRepository = estadioRepository;
        this.sectorRepository = sectorRepository;
        this.equipoRepository = equipoRepository;
        this.entradaRepository = entradaRepository;
        this.admPaisSedeRepository = admPaisSedeRepository;
    }

    // devuelve todos los eventos programados
    public List<EventoResponse> findAll() {
        return eventoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // devuelve un evento especifico, lanza error si no existe
    public EventoResponse findById(Integer idEvento) {
        Evento evento = eventoRepository.findById(idEvento);
        if (evento == null) {
            throw new BusinessException("El evento no existe", HttpStatus.NOT_FOUND);
        }
        return toResponse(evento);
    }

    // crea un nuevo evento, validando todas las reglas de negocio
    // antes de insertar nada en la base de datos
    public EventoResponse create(EventoRequest request, String mailAdmin) {

        // valida que el estadio exista
        Estadio estadio = estadioRepository.findById(request.getIdEstadio());
        if (estadio == null) {
            throw new BusinessException("El estadio no existe", HttpStatus.BAD_REQUEST);
        }

        // valida que el admin tenga jurisdiccion sobre el pais del estadio
        boolean tieneJurisdiccion = admPaisSedeRepository.tieneJurisdiccion(
                mailAdmin, estadio.getNombrePais());
        if (!tieneJurisdiccion) {
            throw new BusinessException(
                    "No tiene jurisdiccion sobre el estadio indicado", HttpStatus.FORBIDDEN);
        }

        // valida que ambos equipos existan
        if (!equipoRepository.existsByNombre(request.getEquipoLocal())) {
            throw new BusinessException("El equipo local no existe", HttpStatus.BAD_REQUEST);
        }
        if (!equipoRepository.existsByNombre(request.getEquipoVisitante())) {
            throw new BusinessException("El equipo visitante no existe", HttpStatus.BAD_REQUEST);
        }
        if (request.getEquipoLocal().equals(request.getEquipoVisitante())) {
            throw new BusinessException(
                    "El equipo local y visitante no pueden ser el mismo", HttpStatus.BAD_REQUEST);
        }

        // valida que no haya superposicion de horarios en el estadio
        boolean superpuesto = eventoRepository.existeSuperposicion(
                request.getIdEstadio(), request.getFechaEvento(), request.getHoraEvento());
        if (superpuesto) {
            throw new BusinessException(
                    "Ya existe un evento en ese estadio con un horario superpuesto",
                    HttpStatus.CONFLICT);
        }

        // valida que todos los sectores habilitados existan en el estadio
        for (SectorHabilitadoRequest sh : request.getSectoresHabilitados()) {
            boolean existeSector = sectorRepository.existsByEstadioYLetra(
                    request.getIdEstadio(), sh.getLetraSector());
            if (!existeSector) {
                throw new BusinessException(
                        "El sector " + sh.getLetraSector() + " no existe en el estadio",
                        HttpStatus.BAD_REQUEST);
            }
        }

        // todas las validaciones pasaron, se procede a insertar
        Evento evento = new Evento();
        evento.setIdEstadio(request.getIdEstadio());
        evento.setMailAdmin(mailAdmin);
        evento.setFechaEvento(request.getFechaEvento());
        evento.setHoraEvento(request.getHoraEvento());

        Integer idEvento = eventoRepository.insert(evento);
        evento.setIdEvento(idEvento);

        // registra los equipos participantes
        eventoRepository.insertEquipo(idEvento, request.getEquipoLocal(), "LOCAL");
        eventoRepository.insertEquipo(idEvento, request.getEquipoVisitante(), "VISITANTE");

        // habilita los sectores con su costo de entrada
        for (SectorHabilitadoRequest sh : request.getSectoresHabilitados()) {
            eventoRepository.insertSectorHabilitado(
                    idEvento, request.getIdEstadio(), sh.getLetraSector(), sh.getCostoEntrada());
        }

        return toResponse(evento);
    }

    // convierte el modelo interno a dto de salida, cargando equipos,
    // estadio y sectores habilitados con su disponibilidad actual
    private EventoResponse toResponse(Evento evento) {
        Estadio estadio = estadioRepository.findById(evento.getIdEstadio());

        List<Object[]> equiposRaw = eventoRepository.findEquiposByEvento(evento.getIdEvento());
        String equipoLocal = null;
        String equipoVisitante = null;
        for (Object[] fila : equiposRaw) {
            String nombreEquipo = (String) fila[0];
            String condicion = (String) fila[1];
            if ("LOCAL".equals(condicion)) {
                equipoLocal = nombreEquipo;
            } else {
                equipoVisitante = nombreEquipo;
            }
        }

        List<Object[]> sectoresRaw = eventoRepository.findSectoresHabilitadosByEvento(evento.getIdEvento());
        List<SectorHabilitadoResponse> sectoresResponse = sectoresRaw.stream()
                .map(fila -> {
                    String letraSector = (String) fila[0];
                    java.math.BigDecimal costo = (java.math.BigDecimal) fila[1];

                    Sector sector = sectorRepository.findByEstadioYLetra(evento.getIdEstadio(), letraSector);
                    int capacidadTotal = sector != null ? sector.getCapacidad() : 0;

                    int vendidas = entradaRepository.countEntradasVendidas(
                            evento.getIdEvento(), evento.getIdEstadio(), letraSector);

                    int disponibles = capacidadTotal - vendidas;

                    return new SectorHabilitadoResponse(letraSector, costo, capacidadTotal, disponibles);
                })
                .collect(Collectors.toList());

        return new EventoResponse(
                evento.getIdEvento(),
                evento.getIdEstadio(),
                estadio != null ? estadio.getNombre() : null,
                estadio != null ? estadio.getNombrePais() : null,
                equipoLocal,
                equipoVisitante,
                evento.getFechaEvento(),
                evento.getHoraEvento(),
                sectoresResponse);
    }
}