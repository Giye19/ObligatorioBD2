package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.request.ValidacionRequest;
import com.ucu.ticketing.dto.response.ValidacionResponse;
import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Entrada;
import com.ucu.ticketing.model.Ingreso;
import com.ucu.ticketing.model.QR;
import com.ucu.ticketing.repository.DispositivoRepository;
import com.ucu.ticketing.repository.EntradaRepository;
import com.ucu.ticketing.repository.FuncValidacionRepository;
import com.ucu.ticketing.repository.IngresoRepository;
import com.ucu.ticketing.repository.QRRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// contiene la logica de validacion de acceso en puerta
// verifica dispositivo autorizado, qr activo y vigente,
// y marca la entrada como consumida de forma irreversible
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

    // valida el acceso de una entrada escaneada en la puerta del estadio
    // si todo es correcto, marca la entrada como consumida de forma
    // irreversible y registra el ingreso con el funcionario responsable
    @Transactional
    public ValidacionResponse validarAcceso(ValidacionRequest request) {

        // verifica que el dispositivo este autorizado
        var dispositivo = dispositivoRepository.findById(request.getIdDispositivo());
        if (dispositivo == null) {
            return new ValidacionResponse(false, "Dispositivo no autorizado", null, null, null, null);
        }

        // busca el qr por el token escaneado
        QR qr = qrRepository.findByToken(request.getTokenQr());
        if (qr == null) {
            return new ValidacionResponse(false, "Codigo qr invalido", null, null, null, null);
        }

        // verifica que el qr este activo (sea el ultimo generado)
        if (!qr.getActivo()) {
            return new ValidacionResponse(false, "El codigo qr ya no esta activo", null, null, null, null);
        }

        // verifica que el qr no haya expirado (vigencia de 30 segundos)
        if (qr.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return new ValidacionResponse(false, "El codigo qr expiro", null, null, null, null);
        }

        Entrada entrada = entradaRepository.findById(qr.getIdEntrada());
        if (entrada == null) {
            return new ValidacionResponse(false, "La entrada asociada no existe", null, null, null, null);
        }

        // verifica que la entrada no haya sido consumida ya
        // (control de reventa o doble ingreso)
        if ("CONSUMIDA".equals(entrada.getEstado())) {
            return new ValidacionResponse(
                    false, "Esta entrada ya fue consumida anteriormente",
                    entrada.getIdEntrada(), entrada.getLetraSector(),
                    entrada.getMailPropietario(), null);
        }

        // todo valido: registra el ingreso y marca la entrada como consumida
        Ingreso ingreso = new Ingreso();
        ingreso.setIdEntrada(entrada.getIdEntrada());
        ingreso.setIdQr(qr.getIdQr());
        ingreso.setIdDispositivo(request.getIdDispositivo());
        ingreso.setPuertaIngreso(request.getPuertaIngreso());

        ingresoRepository.insert(ingreso);
        entradaRepository.marcarConsumida(entrada.getIdEntrada());

        return new ValidacionResponse(
                true, "Acceso permitido",
                entrada.getIdEntrada(), entrada.getLetraSector(),
                entrada.getMailPropietario(), LocalDateTime.now());
    }

    // verifica si un funcionario valido entradas en todos los sectores
    // a los que fue asignado, dentro de un evento especifico
    // devuelve true si cumplio la regla, false si le falto algun sector
    public boolean validoTodosLosSectoresAsignados(String mailFuncionario, Integer idEstadio, Integer idEvento) {

        List<String> sectoresAsignados = funcValidacionRepository.findSectoresAsignados(
                mailFuncionario, idEstadio);

        if (sectoresAsignados.isEmpty()) {
            // si no tiene sectores asignados, no aplica la regla
            return true;
        }

        List<String> sectoresValidados = ingresoRepository.findSectoresValidadosPorFuncionario(
                mailFuncionario, idEvento);

        return sectoresAsignados.stream().allMatch(sectoresValidados::contains);
    }
}