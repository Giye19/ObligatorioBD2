package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.request.EstadioRequest;
import com.ucu.ticketing.dto.response.EstadioResponse;
import com.ucu.ticketing.dto.response.SectorResponse;
import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Estadio;
import com.ucu.ticketing.model.PaisSede;
import com.ucu.ticketing.model.Sector;
import com.ucu.ticketing.model.Usuario;
import com.ucu.ticketing.repository.AdmPaisSedeRepository;
import com.ucu.ticketing.repository.DireccionRepository;
import com.ucu.ticketing.repository.EstadioRepository;
import com.ucu.ticketing.repository.PaisSedeRepository;
import com.ucu.ticketing.repository.SectorRepository;
import com.ucu.ticketing.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstadioService {

    private static final String[] SECTORES_ESTANDAR = {"A", "B", "C", "D"};
    private static final int CAPACIDAD_DEFAULT = 10000;

    private final EstadioRepository estadioRepository;
    private final SectorRepository sectorRepository;
    private final PaisSedeRepository paisSedeRepository;
    private final AdmPaisSedeRepository admPaisSedeRepository;
    private final DireccionRepository direccionRepository;
    private final UsuarioRepository usuarioRepository;

    public EstadioService(EstadioRepository estadioRepository,
                           SectorRepository sectorRepository,
                           PaisSedeRepository paisSedeRepository,
                           AdmPaisSedeRepository admPaisSedeRepository,
                           DireccionRepository direccionRepository,
                           UsuarioRepository usuarioRepository) {
        this.estadioRepository = estadioRepository;
        this.sectorRepository = sectorRepository;
        this.paisSedeRepository = paisSedeRepository;
        this.admPaisSedeRepository = admPaisSedeRepository;
        this.direccionRepository = direccionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<EstadioResponse> findAll() {
        return estadioRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EstadioResponse findById(Long idEstadio) {
        Estadio estadio = estadioRepository.findById(idEstadio);
        if (estadio == null) {
            throw new BusinessException("El estadio no existe", HttpStatus.NOT_FOUND);
        }
        return toResponse(estadio);
    }

    @Transactional
    public EstadioResponse create(EstadioRequest request, String mailAdmin) {

        PaisSede paisSede = paisSedeRepository.findByNombrePais(request.getNombrePais());
        if (paisSede == null) {
            throw new BusinessException("El pais sede no existe", HttpStatus.BAD_REQUEST);
        }

        Usuario usuarioAdmin = usuarioRepository.findByMail(mailAdmin);
        if (usuarioAdmin == null) {
            throw new BusinessException("El administrador no existe", HttpStatus.UNAUTHORIZED);
        }

        boolean tieneJurisdiccion = admPaisSedeRepository.tieneJurisdiccion(
                usuarioAdmin.getIdUsuario(), paisSede.getIdPaisSede());
        if (!tieneJurisdiccion) {
            throw new BusinessException(
                    "No tiene jurisdiccion sobre el pais sede indicado", HttpStatus.FORBIDDEN);
        }

        Long idDireccion = direccionRepository.insert(
                request.getDirPais(), request.getDirLocalidad(), request.getDirCalle(),
                request.getDirNumero(), request.getDirCodPostal());

        Long idEstadio = estadioRepository.insert(
                request.getNombre(), paisSede.getIdPaisSede(), idDireccion);

        for (String letra : SECTORES_ESTANDAR) {
            sectorRepository.insert(idEstadio, letra, CAPACIDAD_DEFAULT);
        }

        Estadio estadio = estadioRepository.findById(idEstadio);
        return toResponse(estadio);
    }

    private EstadioResponse toResponse(Estadio estadio) {
        List<Sector> sectores = sectorRepository.findByEstadio(estadio.getIdEstadio());

        List<SectorResponse> sectoresResponse = sectores.stream()
                .map(s -> new SectorResponse(s.getLetra(), s.getCapacidad()))
                .collect(Collectors.toList());

        return new EstadioResponse(
                estadio.getIdEstadio(),
                estadio.getNombreEstadio(),
                estadio.getNombrePais(),
                sectoresResponse);
    }
}