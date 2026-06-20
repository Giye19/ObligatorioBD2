package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.request.TransferenciaRequest;
import com.ucu.ticketing.dto.response.TransferenciaResponse;
import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Entrada;
import com.ucu.ticketing.model.Transferencia;
import com.ucu.ticketing.repository.EntradaRepository;
import com.ucu.ticketing.repository.TransferenciaRepository;
import com.ucu.ticketing.repository.UsuarioGeneralRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// contiene la logica de transferencia de entradas entre usuarios
// valida limite de 3 transferencias, propietario actual, y que
// no haya una transferencia pendiente sin resolver
@Service
public class TransferenciaService {

    // cantidad maxima de transferencias permitidas para una entrada
    // antes de su validacion en puerta, segun la consigna: "una entrada
    // puede ser transferida como maximo 3 veces antes de su validacion"
    private static final int MAXIMO_TRANSFERENCIAS = 3;

    private final TransferenciaRepository transferenciaRepository;
    private final EntradaRepository entradaRepository;
    private final UsuarioGeneralRepository usuarioGeneralRepository;

    public TransferenciaService(TransferenciaRepository transferenciaRepository,
                                 EntradaRepository entradaRepository,
                                 UsuarioGeneralRepository usuarioGeneralRepository) {
        this.transferenciaRepository = transferenciaRepository;
        this.entradaRepository = entradaRepository;
        this.usuarioGeneralRepository = usuarioGeneralRepository;
    }

    // inicia una transferencia de una entrada hacia otro usuario
    // queda en estado pendiente hasta que el destinatario la acepte
    @Transactional
    public TransferenciaResponse iniciar(TransferenciaRequest request, String mailOrigen) {

        Entrada entrada = entradaRepository.findById(request.getIdEntrada());
        if (entrada == null) {
            throw new BusinessException("La entrada no existe", HttpStatus.NOT_FOUND);
        }

        // valida que quien transfiere sea el propietario actual
        if (!entrada.getMailPropietario().equals(mailOrigen)) {
            throw new BusinessException(
                    "Solo el propietario actual puede transferir esta entrada", HttpStatus.FORBIDDEN);
        }

        // valida que la entrada no haya sido consumida ya
        if ("CONSUMIDA".equals(entrada.getEstado())) {
            throw new BusinessException(
                    "No se puede transferir una entrada ya consumida", HttpStatus.CONFLICT);
        }

        // valida que no se haya alcanzado el limite de transferencias
        if (entrada.getCantTransferencias() >= MAXIMO_TRANSFERENCIAS) {
            throw new BusinessException(
                    "La entrada ya alcanzo el maximo de " + MAXIMO_TRANSFERENCIAS + " transferencias",
                    HttpStatus.CONFLICT);
        }

        // valida que no haya ya una transferencia pendiente para esta entrada
        Transferencia pendienteExistente = transferenciaRepository.findPendientePorEntrada(
                request.getIdEntrada());
        if (pendienteExistente != null) {
            throw new BusinessException(
                    "Ya existe una transferencia pendiente para esta entrada", HttpStatus.CONFLICT);
        }

        // valida que el destinatario sea un usuario general existente
        if (usuarioGeneralRepository.findByMail(request.getMailDestino()) == null) {
            throw new BusinessException(
                    "El usuario destino no existe o no es un usuario general", HttpStatus.BAD_REQUEST);
        }

        // valida que no se transfiera a si mismo
        if (mailOrigen.equals(request.getMailDestino())) {
            throw new BusinessException(
                    "No se puede transferir una entrada a si mismo", HttpStatus.BAD_REQUEST);
        }

        Transferencia transferencia = new Transferencia();
        transferencia.setIdEntrada(request.getIdEntrada());
        transferencia.setMailOrigen(mailOrigen);
        transferencia.setMailDestino(request.getMailDestino());

        Integer idTransferencia = transferenciaRepository.insert(transferencia);
        transferencia.setIdTransferencia(idTransferencia);
        transferencia.setEstado("PENDIENTE");

        return toResponse(transferencia);
    }

    // acepta una transferencia pendiente, lo cual recien ahi cambia
    // el propietario real de la entrada (desacoplamiento compra/tenencia)
    @Transactional
    public TransferenciaResponse aceptar(Integer idTransferencia, String mailDestino) {

        Transferencia transferencia = transferenciaRepository.findById(idTransferencia);
        if (transferencia == null) {
            throw new BusinessException("La transferencia no existe", HttpStatus.NOT_FOUND);
        }

        // valida que quien acepta sea el destinatario
        if (!transferencia.getMailDestino().equals(mailDestino)) {
            throw new BusinessException(
                    "Solo el destinatario puede aceptar esta transferencia", HttpStatus.FORBIDDEN);
        }

        // valida que siga pendiente (no fue aceptada ni rechazada antes)
        if (!"PENDIENTE".equals(transferencia.getEstado())) {
            throw new BusinessException(
                    "Esta transferencia ya fue resuelta anteriormente", HttpStatus.CONFLICT);
        }

        transferenciaRepository.updateEstado(idTransferencia, "ACEPTADA");
        entradaRepository.transferirPropietario(transferencia.getIdEntrada(), mailDestino);

        transferencia.setEstado("ACEPTADA");
        return toResponse(transferencia);
    }

    // rechaza una transferencia pendiente, la entrada queda disponible
    // para que el origen intente transferirla a otro destinatario
    @Transactional
    public TransferenciaResponse rechazar(Integer idTransferencia, String mailDestino) {

        Transferencia transferencia = transferenciaRepository.findById(idTransferencia);
        if (transferencia == null) {
            throw new BusinessException("La transferencia no existe", HttpStatus.NOT_FOUND);
        }

        if (!transferencia.getMailDestino().equals(mailDestino)) {
            throw new BusinessException(
                    "Solo el destinatario puede rechazar esta transferencia", HttpStatus.FORBIDDEN);
        }

        if (!"PENDIENTE".equals(transferencia.getEstado())) {
            throw new BusinessException(
                    "Esta transferencia ya fue resuelta anteriormente", HttpStatus.CONFLICT);
        }

        transferenciaRepository.updateEstado(idTransferencia, "RECHAZADA");

        transferencia.setEstado("RECHAZADA");
        return toResponse(transferencia);
    }

    // devuelve el historico de transferencias donde el usuario
    // participo como origen o como destino
    public List<TransferenciaResponse> findByUsuario(String mail) {
        return transferenciaRepository.findByUsuario(mail).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // devuelve la cadena de custodia completa de una entrada,
    // permitiendo reconstruir el camino desde su emision original
    public List<TransferenciaResponse> findHistorialByEntrada(Integer idEntrada) {
        return transferenciaRepository.findByEntrada(idEntrada).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TransferenciaResponse toResponse(Transferencia t) {
        return new TransferenciaResponse(
                t.getIdTransferencia(),
                t.getIdEntrada(),
                t.getMailOrigen(),
                t.getMailDestino(),
                t.getFechaTransferencia(),
                t.getEstado());
    }
}