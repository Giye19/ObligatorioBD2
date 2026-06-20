package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.request.EstadioRequest;
import com.ucu.ticketing.dto.response.EstadioResponse;
import com.ucu.ticketing.dto.response.SectorResponse;
import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Estadio;
import com.ucu.ticketing.model.Sector;
import com.ucu.ticketing.repository.AdmPaisSedeRepository;
import com.ucu.ticketing.repository.EstadioRepository;
import com.ucu.ticketing.repository.PaisSedeRepository;
import com.ucu.ticketing.repository.SectorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// contiene la logica de gestion de estadios y sus sectores
// solo un administrador puede crear estadios, y unicamente
// dentro de su propio pais sede (jurisdiccion exclusiva)
@Service
public class EstadioService {

    // letras de sector estandar que se crean automaticamente
    // al dar de alta un nuevo estadio (segun la consigna: A, B, C, D)
    private static final String[] SECTORES_ESTANDAR = {"A", "B", "C", "D"};

    // capacidad por defecto de cada sector al crearlo
    // se puede ajustar despues con un endpoint propio si se necesita
    private static final int CAPACIDAD_DEFAULT = 10000;

    private final EstadioRepository estadioRepository;
    private final SectorRepository sectorRepository;
    private final PaisSedeRepository paisSedeRepository;
    private final AdmPaisSedeRepository admPaisSedeRepository;

    public EstadioService(EstadioRepository estadioRepository,
                           SectorRepository sectorRepository,
                           PaisSedeRepository paisSedeRepository,
                           AdmPaisSedeRepository admPaisSedeRepository) {
        this.estadioRepository = estadioRepository;
        this.sectorRepository = sectorRepository;
        this.paisSedeRepository = paisSedeRepository;
        this.admPaisSedeRepository = admPaisSedeRepository;
    }

    // devuelve todos los estadios con sus sectores
    public List<EstadioResponse> findAll() {
        return estadioRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // devuelve un estadio especifico con sus sectores
    // lanza error si no existe
    public EstadioResponse findById(Integer idEstadio) {
        Estadio estadio = estadioRepository.findById(idEstadio);
        if (estadio == null) {
            throw new BusinessException("El estadio no existe", HttpStatus.NOT_FOUND);
        }
        return toResponse(estadio);
    }

    // crea un nuevo estadio con sus 4 sectores estandar
    // solo el admin con jurisdiccion sobre ese pais puede hacerlo
    public EstadioResponse create(EstadioRequest request, String mailAdmin) {

        // valida que el pais sede exista
        if (!paisSedeRepository.existsByNombre(request.getNombrePais())) {
            throw new BusinessException("El pais sede no existe", HttpStatus.BAD_REQUEST);
        }

        // valida que el admin tenga jurisdiccion sobre ese pais
        boolean tieneJurisdiccion = admPaisSedeRepository.tieneJurisdiccion(
                mailAdmin, request.getNombrePais());
        if (!tieneJurisdiccion) {
            throw new BusinessException(
                    "No tiene jurisdiccion sobre el pais sede indicado", HttpStatus.FORBIDDEN);
        }

        Estadio estadio = new Estadio();
        estadio.setNombre(request.getNombre());
        estadio.setNombrePais(request.getNombrePais());

        Integer idEstadio = estadioRepository.insert(estadio);
        estadio.setIdEstadio(idEstadio);

        // crea los 4 sectores estandar con capacidad default
        for (String letra : SECTORES_ESTANDAR) {
            Sector sector = new Sector();
            sector.setIdEstadio(idEstadio);
            sector.setLetra(letra);
            sector.setCapacidad(CAPACIDAD_DEFAULT);
            sectorRepository.insert(sector);
        }

        return toResponse(estadio);
    }

    // convierte el modelo interno a dto de salida, cargando sectores
    private EstadioResponse toResponse(Estadio estadio) {
        List<Sector> sectores = sectorRepository.findByEstadio(estadio.getIdEstadio());

        List<SectorResponse> sectoresResponse = sectores.stream()
                .map(s -> new SectorResponse(s.getLetra(), s.getCapacidad()))
                .collect(Collectors.toList());

        return new EstadioResponse(
                estadio.getIdEstadio(),
                estadio.getNombre(),
                estadio.getNombrePais(),
                sectoresResponse);
    }
}