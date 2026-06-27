package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.request.ValidacionRequest;
import com.ucu.ticketing.dto.response.ValidacionResponse;
import com.ucu.ticketing.repository.FuncValidacionRepository;
import com.ucu.ticketing.repository.IngresoRepository;
import com.ucu.ticketing.repository.QRRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ValidacionService {

    private final QRRepository qrRepository;
    private final IngresoRepository ingresoRepository;
    private final FuncValidacionRepository funcValidacionRepository;

    public ValidacionService(QRRepository qrRepository,
                              IngresoRepository ingresoRepository,
                              FuncValidacionRepository funcValidacionRepository) {
        this.qrRepository = qrRepository;
        this.ingresoRepository = ingresoRepository;
        this.funcValidacionRepository = funcValidacionRepository;
    }

    /**
     * valida el acceso de una entrada escaneada en la puerta,
     * delegando toda la logica de validacion (dispositivo
     * autorizado, qr vigente, funcionario asignado al sector)
     * al procedimiento sp_validar_qr.
     *
     * si el procedimiento rechaza la operacion, lanza una excepcion
     * sql que el GlobalExceptionHandler traduce al mensaje definido
     * en el SIGNAL SQLSTATE correspondiente.
     */
    public ValidacionResponse validarAcceso(ValidacionRequest request) {

        Long idDispositivo = Long.parseLong(request.getIdDispositivo());

        qrRepository.validarQr(request.getTokenQr(), idDispositivo, request.getPuertaIngreso());

        return new ValidacionResponse(
                true, "Acceso permitido",
                null, null, null, LocalDateTime.now());
    }

    /**
     * verifica si un funcionario valido entradas en todos los
     * sectores a los que fue asignado, dentro de un evento especifico
     */
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