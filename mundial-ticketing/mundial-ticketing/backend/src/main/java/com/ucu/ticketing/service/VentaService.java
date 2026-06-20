package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.request.ItemEntradaRequest;
import com.ucu.ticketing.dto.request.VentaRequest;
import com.ucu.ticketing.dto.response.EntradaResponse;
import com.ucu.ticketing.dto.response.VentaResponse;
import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Comision;
import com.ucu.ticketing.model.Entrada;
import com.ucu.ticketing.model.Estadio;
import com.ucu.ticketing.model.Evento;
import com.ucu.ticketing.model.Sector;
import com.ucu.ticketing.model.Venta;
import com.ucu.ticketing.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// contiene la logica de compra de entradas
// valida limite de 5 entradas por transaccion, disponibilidad
// de cada sector, y calcula el monto total con comision vigente
@Service
public class VentaService {

    // cantidad maxima de entradas permitidas en una sola transaccion,
    // segun la consigna: "un usuario no puede comprar mas de 5
    // entradas en la misma transaccion"
    private static final int MAXIMO_ENTRADAS_POR_VENTA = 5;

    private final VentaRepository ventaRepository;
    private final EntradaRepository entradaRepository;
    private final ComisionRepository comisionRepository;
    private final EventoRepository eventoRepository;
    private final SectorRepository sectorRepository;
    private final EstadioRepository estadioRepository;

    public VentaService(VentaRepository ventaRepository,
                         EntradaRepository entradaRepository,
                         ComisionRepository comisionRepository,
                         EventoRepository eventoRepository,
                         SectorRepository sectorRepository,
                         EstadioRepository estadioRepository) {
        this.ventaRepository = ventaRepository;
        this.entradaRepository = entradaRepository;
        this.comisionRepository = comisionRepository;
        this.eventoRepository = eventoRepository;
        this.sectorRepository = sectorRepository;
        this.estadioRepository = estadioRepository;
    }

    // realiza una compra de entradas para un evento
    // toda la operacion es transaccional: si falla cualquier paso,
    // se deshacen todos los inserts hechos hasta ese momento
    @Transactional
    public VentaResponse comprar(VentaRequest request, String mailComprador) {

        // valida que el evento exista
        Evento evento = eventoRepository.findById(request.getIdEvento());
        if (evento == null) {
            throw new BusinessException("El evento no existe", HttpStatus.BAD_REQUEST);
        }

        // suma la cantidad total de entradas pedidas en todos los items
        int cantidadTotal = request.getItemsEntrada().stream()
                .mapToInt(ItemEntradaRequest::getCantidad)
                .sum();

        if (cantidadTotal > MAXIMO_ENTRADAS_POR_VENTA) {
            throw new BusinessException(
                    "No se pueden comprar mas de " + MAXIMO_ENTRADAS_POR_VENTA +
                            " entradas en la misma transaccion",
                    HttpStatus.BAD_REQUEST);
        }

        if (cantidadTotal == 0) {
            throw new BusinessException("Debe solicitar al menos una entrada", HttpStatus.BAD_REQUEST);
        }

        // valida cada item: que el sector este habilitado para el evento
        // y que haya disponibilidad suficiente
        for (ItemEntradaRequest item : request.getItemsEntrada()) {
            validarDisponibilidad(evento, item);
        }

        // calcula el costo total de las entradas (sin comision todavia)
        BigDecimal costoEntradas = BigDecimal.ZERO;
        for (ItemEntradaRequest item : request.getItemsEntrada()) {
            BigDecimal costoSector = eventoRepository.findCostoEntrada(
                    evento.getIdEvento(), evento.getIdEstadio(), item.getLetraSector());
            costoEntradas = costoEntradas.add(costoSector.multiply(BigDecimal.valueOf(item.getCantidad())));
        }

        // busca la comision vigente y calcula el monto total
        Comision comisionVigente = comisionRepository.findVigente();
        if (comisionVigente == null) {
            throw new BusinessException("No hay una comision vigente configurada", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        BigDecimal montoComision = costoEntradas
                .multiply(comisionVigente.getPorcentaje())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal montoTotal = costoEntradas.add(montoComision);

        // crea la venta
        Venta venta = new Venta();
        venta.setMailComprador(mailComprador);
        venta.setIdComision(comisionVigente.getIdComision());
        venta.setEstado("PAGA"); // simplificacion para el demo: se confirma directo
        venta.setMontoTotal(montoTotal);
        venta.setFechaVenta(java.time.LocalDateTime.now());
        
        Integer idVenta = ventaRepository.insert(venta);
        venta.setIdVenta(idVenta);

        // crea una entrada individual por cada unidad solicitada en cada item
        // cada entrada tiene su propio id, aunque pertenezcan a la misma venta
        List<Entrada> entradasCreadas = new ArrayList<>();

        for (ItemEntradaRequest item : request.getItemsEntrada()) {
            BigDecimal costoSector = eventoRepository.findCostoEntrada(
                    evento.getIdEvento(), evento.getIdEstadio(), item.getLetraSector());

            for (int i = 0; i < item.getCantidad(); i++) {
                Entrada entrada = new Entrada();
                entrada.setIdVenta(idVenta);
                entrada.setIdEvento(evento.getIdEvento());
                entrada.setIdEstadio(evento.getIdEstadio());
                entrada.setLetraSector(item.getLetraSector());
                entrada.setMailPropietario(mailComprador);
                entrada.setCostoEntrada(costoSector);

                Integer idEntrada = entradaRepository.insert(entrada);
                entrada.setIdEntrada(idEntrada);
                entrada.setEstado("ACTIVA");
                entrada.setCantTransferencias(0);

                entradasCreadas.add(entrada);
            }
        }

        return toResponse(venta, comisionVigente.getPorcentaje(), entradasCreadas, evento);
    }

    // valida que el sector este habilitado para el evento y que
    // haya disponibilidad suficiente para la cantidad solicitada
    // actua como limite duro de capacidad, segun la consigna
    private void validarDisponibilidad(Evento evento, ItemEntradaRequest item) {

        boolean sectorHabilitado = eventoRepository.sectorHabilitado(
                evento.getIdEvento(), evento.getIdEstadio(), item.getLetraSector());

        if (!sectorHabilitado) {
            throw new BusinessException(
                    "El sector " + item.getLetraSector() + " no esta habilitado para este evento",
                    HttpStatus.BAD_REQUEST);
        }

        Sector sector = sectorRepository.findByEstadioYLetra(evento.getIdEstadio(), item.getLetraSector());
        int vendidas = entradaRepository.countEntradasVendidas(
                evento.getIdEvento(), evento.getIdEstadio(), item.getLetraSector());

        int disponibles = sector.getCapacidad() - vendidas;

        if (item.getCantidad() > disponibles) {
            throw new BusinessException(
                    "No hay suficiente disponibilidad en el sector " + item.getLetraSector() +
                            ". Disponibles: " + disponibles,
                    HttpStatus.CONFLICT);
        }
    }

    // devuelve el historico de ventas de un usuario
    public List<VentaResponse> findByComprador(String mailComprador) {
        return ventaRepository.findByComprador(mailComprador).stream()
                .map(venta -> {
                    Comision comision = comisionRepository.findById(venta.getIdComision());
                    List<Entrada> entradas = entradaRepository.findByVenta(venta.getIdVenta());
                    Evento evento = entradas.isEmpty() ? null :
                            eventoRepository.findById(entradas.get(0).getIdEvento());

                    return toResponse(venta, comision.getPorcentaje(), entradas, evento);
                })
                .collect(Collectors.toList());
    }

    // convierte el modelo interno a dto de salida
    private VentaResponse toResponse(Venta venta, BigDecimal porcentajeComision,
                                      List<Entrada> entradas, Evento evento) {

        List<EntradaResponse> entradasResponse = entradas.stream()
                .map(e -> toEntradaResponse(e, evento))
                .collect(Collectors.toList());

        return new VentaResponse(
                venta.getIdVenta(),
                venta.getMailComprador(),
                venta.getFechaVenta(),
                venta.getEstado(),
                venta.getMontoTotal(),
                porcentajeComision,
                entradasResponse);
    }

    // convierte una entrada a su dto de salida, agregando datos
    // legibles del evento y estadio
    private EntradaResponse toEntradaResponse(Entrada entrada, Evento evento) {
        Estadio estadio = estadioRepository.findById(entrada.getIdEstadio());

        String equipoLocal = null;
        String equipoVisitante = null;
        if (evento != null) {
            List<Object[]> equiposRaw = eventoRepository.findEquiposByEvento(evento.getIdEvento());
            for (Object[] fila : equiposRaw) {
                if ("LOCAL".equals(fila[1])) {
                    equipoLocal = (String) fila[0];
                } else {
                    equipoVisitante = (String) fila[0];
                }
            }
        }

        return new EntradaResponse(
                entrada.getIdEntrada(),
                entrada.getIdEvento(),
                estadio != null ? estadio.getNombre() : null,
                equipoLocal,
                equipoVisitante,
                entrada.getLetraSector(),
                entrada.getMailPropietario(),
                entrada.getEstado(),
                entrada.getCostoEntrada(),
                entrada.getCantTransferencias());
    }
}