package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.request.ValidacionRequest;
import com.ucu.ticketing.dto.response.ValidacionResponse;
import com.ucu.ticketing.model.Dispositivo;
import com.ucu.ticketing.model.Entrada;
import com.ucu.ticketing.model.Ingreso;
import com.ucu.ticketing.model.QR;
import com.ucu.ticketing.repository.DispositivoRepository;
import com.ucu.ticketing.repository.EntradaRepository;
import com.ucu.ticketing.repository.FuncValidacionRepository;
import com.ucu.ticketing.repository.IngresoRepository;
import com.ucu.ticketing.repository.QRRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ValidacionService {

    private final DispositivoRepository dispositivoRepository;
    private final QRRepository qrRepository;
    private final EntradaRepository entradaRepository;
    private final IngresoRepository ingresoRepository;
    private final FuncValidacionRepository funcValidacionRepository;

    public ValidacionService(DispositivoRepository dispositivoRepository,
                              QRRepository qrRepository,
                              EntradaRepository entradaRepository,
                              IngresoRepository ingresoRepository,
                              FuncValidacionRepository funcValidacionRepository) {
        this.dispositivoRepository = dispositivoRepository;
        this.qrRepository = qrRepository;
        this.entradaRepository = entradaRepository;
        this.ingresoRepository = ingresoRepository;
        this.funcValidacionRepository = funcValidacionRepository;
    }

    @Transactional
    public ValidacionResponse validarAcceso(ValidacionRequest request) {

        Dispositivo dispositivo = dispositivoRepository.findByCodigo(request.getIdDispositivo());
        if (dispositivo == null || !Boolean.TRUE.equals(dispositivo.getActivo())) {
            return new ValidacionResponse(false, "Dispositivo no autorizado", null, null, null, null);
        }

        QR qr = qrRepository.findByToken(request.getTokenQr());
        if (qr == null) {
            return new ValidacionResponse(false, "Codigo qr invalido", null, null, null, null);
        }

        if (qr.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return new ValidacionResponse(false, "El codigo qr expiro", null, null, null, null);
        }

        Entrada entrada = entradaRepository.findById(qr.getIdEntrada());
        if (entrada == null) {
            return new ValidacionResponse(false, "La entrada asociada no existe", null, null, null, null);
        }

        Ingreso ingreso = new Ingreso();
        ingreso.setIdQr(qr.getIdQr());
        ingreso.setIdDispositivo(dispositivo.getIdDispositivo());
        ingreso.setIdFuncionario(dispositivo.getIdFuncionario());
        ingreso.setPuertaIngreso(
                request.getPuertaIngreso() == null || request.getPuertaIngreso().isBlank()
                        ? "Sin especificar"
                        : request.getPuertaIngreso());

        ingresoRepository.insert(ingreso);

        return new ValidacionResponse(
                true, "Acceso permitido",
                entrada.getIdEntrada(), null,
                null, LocalDateTime.now());
    }

    public boolean validoTodosLosSectoresAsignados(Long idFuncionario, Long idEvento) {

        List<Long> sectoresAsignados = funcValidacionRepository.findSectoresAsignados(idFuncionario, idEvento);

        if (sectoresAsignados.isEmpty()) {
            return true;
        }

        List<Long> sectoresValidados = ingresoRepository.findSectoresValidadosPorFuncionario(
                idFuncionario, idEvento);

        return sectoresAsignados.stream().allMatch(sectoresValidados::contains);
    }
}