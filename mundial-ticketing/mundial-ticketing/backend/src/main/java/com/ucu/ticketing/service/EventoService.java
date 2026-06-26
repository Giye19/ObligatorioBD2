package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.request.EventoRequest;
import com.ucu.ticketing.dto.request.SectorHabilitadoRequest;
import com.ucu.ticketing.dto.response.EventoResponse;
import com.ucu.ticketing.dto.response.SectorHabilitadoResponse;
import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.AdmPaisSede;
import com.ucu.ticketing.model.Equipo;
import com.ucu.ticketing.model.Estadio;
import com.ucu.ticketing.model.Evento;
import com.ucu.ticketing.model.EventoSector;
import com.ucu.ticketing.model.Sector;
import com.ucu.ticketing.model.Usuario;
import com.ucu.ticketing.repository.AdmPaisSedeRepository;
import com.ucu.ticketing.repository.EntradaRepository;
import com.ucu.ticketing.repository.EquipoRepository;
import com.ucu.ticketing.repository.EstadioRepository;
import com.ucu.ticketing.repository.EventoRepository;
import com.ucu.ticketing.repository.EventoSectorRepository;
import com.ucu.ticketing.repository.SectorRepository;
import com.ucu.ticketing.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final EventoSectorRepository eventoSectorRepository;
    private final EstadioRepository estadioRepository;
    private final SectorRepository sectorRepository;
    private final EquipoRepository equipoRepository;
    private final AdmPaisSedeRepository admPaisSedeRepository;
    private final UsuarioRepository usuarioRepository;

    public EventoService(EventoRepository eventoRepository,
                          EventoSectorRepository eventoSectorRepository,
                          EstadioRepository estadioRepository,
                          SectorRepository sectorRepository,
                          EquipoRepository equipoRepository,
                          AdmPaisSedeRepository admPaisSedeRepository,
                          UsuarioRepository usuarioRepository) {
        this.eventoRepository = eventoRepository;
        this.eventoSectorRepository = eventoSectorRepository;
        this.estadioRepository = estadioRepository;
        this.sectorRepository = sectorRepository;
        this.equipoRepository = equipoRepository;
        this.admPaisSedeRepository = admPaisSedeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<EventoResponse> findAll() {
        return eventoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EventoResponse findById(Long idEvento) {
        Evento evento = eventoRepository.findById(idEvento);
        if (evento == null) {
            throw new BusinessException("El evento no existe", HttpStatus.NOT_FOUND);
        }
        return toResponse(evento);
    }

    @Transactional
    public EventoResponse create(EventoRequest request, String mailAdmin) {

        Estadio estadio = estadioRepository.findById(request.getIdEstadio());
        if (estadio == null) {
            throw new BusinessException("El estadio no existe", HttpStatus.BAD_REQUEST);
        }

        Usuario usuarioAdmin = usuarioRepository.findByMail(mailAdmin);
        if (usuarioAdmin == null) {
            throw new BusinessException("El administrador no existe", HttpStatus.UNAUTHORIZED);
        }

        AdmPaisSede admin = admPaisSedeRepository.findByIdUsuario(usuarioAdmin.getIdUsuario());
        if (admin == null) {
            throw new BusinessException("El usuario no es administrador", HttpStatus.FORBIDDEN);
        }

        Equipo equipoLocal = equipoRepository.findByNombre(request.getEquipoLocal());
        if (equipoLocal == null) {
            throw new BusinessException("El equipo local no existe", HttpStatus.BAD_REQUEST);
        }

        Equipo equipoVisitante = equipoRepository.findByNombre(request.getEquipoVisitante());
        if (equipoVisitante == null) {
            throw new BusinessException("El equipo visitante no existe", HttpStatus.BAD_REQUEST);
        }

        if (equipoLocal.getIdEquipo().equals(equipoVisitante.getIdEquipo())) {
            throw new BusinessException(
                    "El equipo local y visitante no pueden ser el mismo", HttpStatus.BAD_REQUEST);
        }

        for (SectorHabilitadoRequest sh : request.getSectoresHabilitados()) {
            boolean existeSector = sectorRepository.existsByEstadioYLetra(
                    request.getIdEstadio(), sh.getLetraSector());
            if (!existeSector) {
                throw new BusinessException(
                        "El sector " + sh.getLetraSector() + " no existe en el estadio",
                        HttpStatus.BAD_REQUEST);
            }
        }

        Evento evento = new Evento();
        evento.setIdEstadio(request.getIdEstadio());
        evento.setIdAdmin(admin.getIdAdmin());
        evento.setIdEquipoLocal(equipoLocal.getIdEquipo());
        evento.setIdEquipoVisitante(equipoVisitante.getIdEquipo());
        evento.setFechaEvento(request.getFechaEvento());
        evento.setHoraEvento(request.getHoraEvento());

        Long idEvento = eventoRepository.insert(evento);

        for (SectorHabilitadoRequest sh : request.getSectoresHabilitados()) {
            Sector sector = sectorRepository.findByEstadioYLetra(request.getIdEstadio(), sh.getLetraSector());
            eventoSectorRepository.insert(idEvento, sector.getIdSector(), sh.getCostoEntrada());
        }

        Evento eventoCreado = eventoRepository.findById(idEvento);
        return toResponse(eventoCreado);
    }

    private EventoResponse toResponse(Evento evento) {
        Estadio estadio = estadioRepository.findById(evento.getIdEstadio());
        Equipo equipoLocal = equipoRepository.findById(evento.getIdEquipoLocal());
        Equipo equipoVisitante = equipoRepository.findById(evento.getIdEquipoVisitante());

        List<EventoSector> sectores = eventoSectorRepository.findByEvento(evento.getIdEvento());

        List<SectorHabilitadoResponse> sectoresResponse = sectores.stream()
                .map(es -> {
                    Sector sector = sectorRepository.findById(es.getIdSector());
                    int vendidas = eventoSectorRepository.countEntradasVendidas(es.getIdEventoSector());
                    int disponibles = sector.getCapacidad() - vendidas;

                    return new SectorHabilitadoResponse(
                            es.getLetraSector(), es.getCostoEntrada(), sector.getCapacidad(), disponibles);
                })
                .collect(Collectors.toList());

        return new EventoResponse(
                evento.getIdEvento(),
                evento.getIdEstadio(),
                estadio != null ? estadio.getNombreEstadio() : null,
                estadio != null ? estadio.getNombrePais() : null,
                equipoLocal != null ? equipoLocal.getNombre() : null,
                equipoVisitante != null ? equipoVisitante.getNombre() : null,
                evento.getFechaEvento(),
                evento.getHoraEvento(),
                sectoresResponse);
    }
}