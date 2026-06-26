package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.request.TransferenciaRequest;
import com.ucu.ticketing.dto.response.TransferenciaResponse;
import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Entrada;
import com.ucu.ticketing.model.Transferencia;
import com.ucu.ticketing.model.Usuario;
import com.ucu.ticketing.model.UsuarioGeneral;
import com.ucu.ticketing.repository.EntradaRepository;
import com.ucu.ticketing.repository.TransferenciaRepository;
import com.ucu.ticketing.repository.UsuarioGeneralRepository;
import com.ucu.ticketing.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransferenciaService {

    private final TransferenciaRepository transferenciaRepository;
    private final EntradaRepository entradaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioGeneralRepository usuarioGeneralRepository;

    public TransferenciaService(TransferenciaRepository transferenciaRepository,
                                 EntradaRepository entradaRepository,
                                 UsuarioRepository usuarioRepository,
                                 UsuarioGeneralRepository usuarioGeneralRepository) {
        this.transferenciaRepository = transferenciaRepository;
        this.entradaRepository = entradaRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioGeneralRepository = usuarioGeneralRepository;
    }

    @Transactional
    public TransferenciaResponse iniciar(TransferenciaRequest request, String mailOrigen) {

        Long idUsuarioGeneralOrigen = resolverIdUsuarioGeneral(mailOrigen);

        Entrada entrada = entradaRepository.findById(request.getIdEntrada());
        if (entrada == null) {
            throw new BusinessException("La entrada no existe", HttpStatus.NOT_FOUND);
        }

        if ("CONSUMIDA".equals(entrada.getEstadoEntrada())) {
            throw new BusinessException(
                    "No se puede transferir una entrada ya consumida", HttpStatus.CONFLICT);
        }

        Transferencia pendienteExistente = transferenciaRepository.findPendientePorEntrada(
                request.getIdEntrada());
        if (pendienteExistente != null) {
            throw new BusinessException(
                    "Ya existe una transferencia pendiente para esta entrada", HttpStatus.CONFLICT);
        }

        Usuario usuarioDestino = usuarioRepository.findByMail(request.getMailDestino());
        if (usuarioDestino == null) {
            throw new BusinessException("El usuario destino no existe", HttpStatus.BAD_REQUEST);
        }

        UsuarioGeneral usuarioGeneralDestino = usuarioGeneralRepository.findByIdUsuario(usuarioDestino.getIdUsuario());
        if (usuarioGeneralDestino == null) {
            throw new BusinessException(
                    "El usuario destino no es un usuario general", HttpStatus.BAD_REQUEST);
        }

        if (idUsuarioGeneralOrigen.equals(usuarioGeneralDestino.getIdUsuarioGeneral())) {
            throw new BusinessException(
                    "No se puede transferir una entrada a si mismo", HttpStatus.BAD_REQUEST);
        }

        Transferencia transferencia = new Transferencia();
        transferencia.setIdEntrada(request.getIdEntrada());
        transferencia.setIdUsuarioOrigen(idUsuarioGeneralOrigen);
        transferencia.setIdUsuarioDestino(usuarioGeneralDestino.getIdUsuarioGeneral());

        Long idTransferencia = transferenciaRepository.insert(transferencia);

        Transferencia creada = transferenciaRepository.findById(idTransferencia);
        return toResponse(creada);
    }

    @Transactional
    public TransferenciaResponse aceptar(Long idTransferencia, String mailDestino) {

        Long idUsuarioGeneralDestino = resolverIdUsuarioGeneral(mailDestino);

        Transferencia transferencia = transferenciaRepository.findById(idTransferencia);
        if (transferencia == null) {
            throw new BusinessException("La transferencia no existe", HttpStatus.NOT_FOUND);
        }

        if (!transferencia.getIdUsuarioDestino().equals(idUsuarioGeneralDestino)) {
            throw new BusinessException(
                    "Solo el destinatario puede aceptar esta transferencia", HttpStatus.FORBIDDEN);
        }

        if (!"PENDIENTE".equals(transferencia.getEstadoTransferencia())) {
            throw new BusinessException(
                    "Esta transferencia ya fue resuelta anteriormente", HttpStatus.CONFLICT);
        }

        transferenciaRepository.updateEstado(idTransferencia, "ACEPTADA");

        Transferencia actualizada = transferenciaRepository.findById(idTransferencia);
        return toResponse(actualizada);
    }

    @Transactional
    public TransferenciaResponse rechazar(Long idTransferencia, String mailDestino) {

        Long idUsuarioGeneralDestino = resolverIdUsuarioGeneral(mailDestino);

        Transferencia transferencia = transferenciaRepository.findById(idTransferencia);
        if (transferencia == null) {
            throw new BusinessException("La transferencia no existe", HttpStatus.NOT_FOUND);
        }

        if (!transferencia.getIdUsuarioDestino().equals(idUsuarioGeneralDestino)) {
            throw new BusinessException(
                    "Solo el destinatario puede rechazar esta transferencia", HttpStatus.FORBIDDEN);
        }

        if (!"PENDIENTE".equals(transferencia.getEstadoTransferencia())) {
            throw new BusinessException(
                    "Esta transferencia ya fue resuelta anteriormente", HttpStatus.CONFLICT);
        }

        transferenciaRepository.updateEstado(idTransferencia, "RECHAZADA");

        Transferencia actualizada = transferenciaRepository.findById(idTransferencia);
        return toResponse(actualizada);
    }

    public List<TransferenciaResponse> findByUsuario(String mail) {
        Long idUsuarioGeneral = resolverIdUsuarioGeneral(mail);

        return transferenciaRepository.findByUsuario(idUsuarioGeneral).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TransferenciaResponse> findHistorialByEntrada(Long idEntrada) {
        return transferenciaRepository.findByEntrada(idEntrada).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Long resolverIdUsuarioGeneral(String mail) {
        Usuario usuario = usuarioRepository.findByMail(mail);
        if (usuario == null) {
            throw new BusinessException("El usuario no existe", HttpStatus.UNAUTHORIZED);
        }

        UsuarioGeneral usuarioGeneral = usuarioGeneralRepository.findByIdUsuario(usuario.getIdUsuario());
        if (usuarioGeneral == null) {
            throw new BusinessException("El usuario no es un usuario general", HttpStatus.FORBIDDEN);
        }

        return usuarioGeneral.getIdUsuarioGeneral();
    }

    private TransferenciaResponse toResponse(Transferencia t) {
        String mailOrigen = resolverMail(t.getIdUsuarioOrigen());
        String mailDestino = resolverMail(t.getIdUsuarioDestino());

        return new TransferenciaResponse(
                t.getIdTransferencia(),
                t.getIdEntrada(),
                mailOrigen,
                mailDestino,
                t.getFechaTransferencia(),
                t.getEstadoTransferencia());
    }

    private String resolverMail(Long idUsuarioGeneral) {
        Long idUsuario = usuarioGeneralRepository.findIdUsuarioById(idUsuarioGeneral);
        if (idUsuario == null) {
            return null;
        }
        Usuario usuario = usuarioRepository.findById(idUsuario);
        return usuario != null ? usuario.getMail() : null;
    }
}