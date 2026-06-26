package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.request.ItemEntradaRequest;
import com.ucu.ticketing.dto.request.VentaRequest;
import com.ucu.ticketing.dto.response.EntradaResponse;
import com.ucu.ticketing.dto.response.VentaResponse;
import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Comision;
import com.ucu.ticketing.model.Entrada;
import com.ucu.ticketing.model.Equipo;
import com.ucu.ticketing.model.Estadio;
import com.ucu.ticketing.model.Evento;
import com.ucu.ticketing.model.EventoSector;
import com.ucu.ticketing.model.Sector;
import com.ucu.ticketing.model.Usuario;
import com.ucu.ticketing.model.UsuarioGeneral;
import com.ucu.ticketing.model.Venta;
import com.ucu.ticketing.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final EntradaRepository entradaRepository;
    private final ComisionRepository comisionRepository;
    private final EventoRepository eventoRepository;
    private final EventoSectorRepository eventoSectorRepository;
    private final SectorRepository sectorRepository;
    private final EstadioRepository estadioRepository;
    private final EquipoRepository equipoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioGeneralRepository usuarioGeneralRepository;
    private final TransferenciaRepository transferenciaRepository;

    public VentaService(VentaRepository ventaRepository,
                         EntradaRepository entradaRepository,
                         ComisionRepository comisionRepository,
                         EventoRepository eventoRepository,
                         EventoSectorRepository eventoSectorRepository,
                         SectorRepository sectorRepository,
                         EstadioRepository estadioRepository,
                         EquipoRepository equipoRepository,
                         UsuarioRepository usuarioRepository,
                         UsuarioGeneralRepository usuarioGeneralRepository,
                         TransferenciaRepository transferenciaRepository) {
        this.ventaRepository = ventaRepository;
        this.entradaRepository = entradaRepository;
        this.comisionRepository = comisionRepository;
        this.eventoRepository = eventoRepository;
        this.eventoSectorRepository = eventoSectorRepository;
        this.sectorRepository = sectorRepository;
        this.estadioRepository = estadioRepository;
        this.equipoRepository = equipoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioGeneralRepository = usuarioGeneralRepository;
        this.transferenciaRepository = transferenciaRepository;
    }

    @Transactional
    public VentaResponse comprar(VentaRequest request, String mailComprador) {

        Usuario usuario = usuarioRepository.findByMail(mailComprador);
        if (usuario == null) {
            throw new BusinessException("El usuario no existe", HttpStatus.UNAUTHORIZED);
        }

        UsuarioGeneral usuarioGeneral = usuarioGeneralRepository.findByIdUsuario(usuario.getIdUsuario());
        if (usuarioGeneral == null) {
            throw new BusinessException("El usuario no es un usuario general", HttpStatus.FORBIDDEN);
        }

        Evento evento = eventoRepository.findById(request.getIdEvento());
        if (evento == null) {
            throw new BusinessException("El evento no existe", HttpStatus.BAD_REQUEST);
        }

        int cantidadTotal = request.getItemsEntrada().stream()
                .mapToInt(ItemEntradaRequest::getCantidad)
                .sum();

        if (cantidadTotal == 0) {
            throw new BusinessException("Debe solicitar al menos una entrada", HttpStatus.BAD_REQUEST);
        }

        List<EventoSector> eventoSectores = new ArrayList<>();
        for (ItemEntradaRequest item : request.getItemsEntrada()) {
            EventoSector es = validarDisponibilidad(evento, item);
            eventoSectores.add(es);
        }

        BigDecimal costoEntradas = BigDecimal.ZERO;
        for (int i = 0; i < request.getItemsEntrada().size(); i++) {
            ItemEntradaRequest item = request.getItemsEntrada().get(i);
            EventoSector es = eventoSectores.get(i);
            costoEntradas = costoEntradas.add(es.getCostoEntrada().multiply(BigDecimal.valueOf(item.getCantidad())));
        }

        Comision comisionVigente = comisionRepository.findVigente();
        if (comisionVigente == null) {
            throw new BusinessException("No hay una comision vigente configurada", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        BigDecimal montoComision = costoEntradas
                .multiply(comisionVigente.getPorcentaje())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal montoTotal = costoEntradas.add(montoComision);

        Venta venta = new Venta();
        venta.setIdUsuarioGeneral(usuarioGeneral.getIdUsuarioGeneral());
        venta.setIdComision(comisionVigente.getIdComision());
        venta.setFechaVenta(LocalDateTime.now());
        venta.setEstadoVenta("PAGA");
        venta.setMontoTotal(montoTotal);

        Long idVenta = ventaRepository.insert(venta);

        List<Entrada> entradasCreadas = new ArrayList<>();

        for (int i = 0; i < request.getItemsEntrada().size(); i++) {
            ItemEntradaRequest item = request.getItemsEntrada().get(i);
            EventoSector es = eventoSectores.get(i);

            for (int j = 0; j < item.getCantidad(); j++) {
                Entrada entrada = new Entrada();
                entrada.setIdEventoSector(es.getIdEventoSector());
                entrada.setIdVenta(idVenta);
                entrada.setIdPropietarioActual(usuarioGeneral.getIdUsuarioGeneral());
                entrada.setCostoEntrada(es.getCostoEntrada());

                Long idEntrada = entradaRepository.insert(entrada);
                entrada.setIdEntrada(idEntrada);
                entrada.setEstadoEntrada("NO_CONSUMIDA");

                entradasCreadas.add(entrada);
            }
        }

        Venta ventaCreada = ventaRepository.findById(idVenta);
        return toResponse(ventaCreada, comisionVigente.getPorcentaje(), entradasCreadas, evento);
    }

    private EventoSector validarDisponibilidad(Evento evento, ItemEntradaRequest item) {

        Sector sector = sectorRepository.findByEstadioYLetra(evento.getIdEstadio(), item.getLetraSector());
        if (sector == null) {
            throw new BusinessException(
                    "El sector " + item.getLetraSector() + " no existe en el estadio", HttpStatus.BAD_REQUEST);
        }

        EventoSector eventoSector = eventoSectorRepository.findByEventoYSector(
                evento.getIdEvento(), sector.getIdSector());

        if (eventoSector == null) {
            throw new BusinessException(
                    "El sector " + item.getLetraSector() + " no esta habilitado para este evento",
                    HttpStatus.BAD_REQUEST);
        }

        return eventoSector;
    }

    public List<VentaResponse> findByComprador(String mailComprador) {
        Usuario usuario = usuarioRepository.findByMail(mailComprador);
        if (usuario == null) {
            throw new BusinessException("El usuario no existe", HttpStatus.UNAUTHORIZED);
        }

        UsuarioGeneral usuarioGeneral = usuarioGeneralRepository.findByIdUsuario(usuario.getIdUsuario());
        if (usuarioGeneral == null) {
            return List.of();
        }

        return ventaRepository.findByUsuarioGeneral(usuarioGeneral.getIdUsuarioGeneral()).stream()
                .map(venta -> {
                    Comision comision = comisionRepository.findById(venta.getIdComision());
                    List<Entrada> entradas = entradaRepository.findByVenta(venta.getIdVenta());

                    Evento evento = null;
                    if (!entradas.isEmpty()) {
                        EventoSector es = eventoSectorRepository.findById(entradas.get(0).getIdEventoSector());
                        evento = eventoRepository.findById(es.getIdEvento());
                    }

                    return toResponse(venta, comision.getPorcentaje(), entradas, evento);
                })
                .collect(Collectors.toList());
    }

    private VentaResponse toResponse(Venta venta, BigDecimal porcentajeComision,
                                      List<Entrada> entradas, Evento evento) {

        List<EntradaResponse> entradasResponse = entradas.stream()
                .map(e -> toEntradaResponse(e, evento))
                .collect(Collectors.toList());

        String mailComprador = resolverMailComprador(venta.getIdUsuarioGeneral());

        return new VentaResponse(
                venta.getIdVenta(),
                mailComprador,
                venta.getFechaVenta(),
                venta.getEstadoVenta(),
                venta.getMontoTotal(),
                porcentajeComision,
                entradasResponse);
    }

    private String resolverMailComprador(Long idUsuarioGeneral) {
        Long idUsuario = usuarioGeneralRepository.findIdUsuarioById(idUsuarioGeneral);
        if (idUsuario == null) {
            return null;
        }
        Usuario usuario = usuarioRepository.findById(idUsuario);
        return usuario != null ? usuario.getMail() : null;
    }

    private EntradaResponse toEntradaResponse(Entrada entrada, Evento evento) {
        EventoSector eventoSector = eventoSectorRepository.findById(entrada.getIdEventoSector());

        Estadio estadio = evento != null ? estadioRepository.findById(evento.getIdEstadio()) : null;

        String equipoLocal = null;
        String equipoVisitante = null;
        if (evento != null) {
            Equipo local = equipoRepository.findById(evento.getIdEquipoLocal());
            Equipo visitante = equipoRepository.findById(evento.getIdEquipoVisitante());
            equipoLocal = local != null ? local.getNombre() : null;
            equipoVisitante = visitante != null ? visitante.getNombre() : null;
        }

        int cantTransferencias = transferenciaRepository.findByEntrada(entrada.getIdEntrada()).stream()
                .filter(t -> "ACEPTADA".equals(t.getEstadoTransferencia()))
                .toList()
                .size();

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