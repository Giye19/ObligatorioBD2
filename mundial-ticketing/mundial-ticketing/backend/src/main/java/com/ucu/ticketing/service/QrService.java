package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.response.QrResponse;
import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Entrada;
import com.ucu.ticketing.model.QR;
import com.ucu.ticketing.model.Usuario;
import com.ucu.ticketing.model.UsuarioGeneral;
import com.ucu.ticketing.repository.EntradaRepository;
import com.ucu.ticketing.repository.QRRepository;
import com.ucu.ticketing.repository.UsuarioGeneralRepository;
import com.ucu.ticketing.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class QrService {

    private static final int VIGENCIA_QR_SEGUNDOS = 30;

    private final QRRepository qrRepository;
    private final EntradaRepository entradaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioGeneralRepository usuarioGeneralRepository;

    public QrService(QRRepository qrRepository,
                      EntradaRepository entradaRepository,
                      UsuarioRepository usuarioRepository,
                      UsuarioGeneralRepository usuarioGeneralRepository) {
        this.qrRepository = qrRepository;
        this.entradaRepository = entradaRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioGeneralRepository = usuarioGeneralRepository;
    }

    @Transactional
    public QrResponse generarNuevo(Long idEntrada, String mailSolicitante) {

        Long idUsuarioGeneral = resolverIdUsuarioGeneral(mailSolicitante);

        Entrada entrada = entradaRepository.findById(idEntrada);
        if (entrada == null) {
            throw new BusinessException("La entrada no existe", HttpStatus.NOT_FOUND);
        }

        if (!entrada.getIdPropietarioActual().equals(idUsuarioGeneral)) {
            throw new BusinessException(
                    "Solo el propietario actual puede generar el qr de esta entrada",
                    HttpStatus.FORBIDDEN);
        }

        if ("CONSUMIDA".equals(entrada.getEstadoEntrada())) {
            throw new BusinessException(
                    "No se puede generar un qr para una entrada ya consumida", HttpStatus.CONFLICT);
        }

        String token = UUID.randomUUID().toString();

        QR qr = new QR();
        qr.setIdEntrada(idEntrada);
        qr.setToken(token);
        qr.setFechaExpiracion(LocalDateTime.now().plusSeconds(VIGENCIA_QR_SEGUNDOS));

        Long idQr = qrRepository.insert(qr);
        qr.setIdQr(idQr);

        return new QrResponse(idQr, idEntrada, token, qr.getFechaExpiracion());
    }

    public QrResponse obtenerActivo(Long idEntrada, String mailSolicitante) {

        Long idUsuarioGeneral = resolverIdUsuarioGeneral(mailSolicitante);

        Entrada entrada = entradaRepository.findById(idEntrada);
        if (entrada == null) {
            throw new BusinessException("La entrada no existe", HttpStatus.NOT_FOUND);
        }

        if (!entrada.getIdPropietarioActual().equals(idUsuarioGeneral)) {
            throw new BusinessException(
                    "Solo el propietario actual puede consultar el qr de esta entrada",
                    HttpStatus.FORBIDDEN);
        }

        QR qr = qrRepository.findUltimoGeneradoPorEntrada(idEntrada);
        if (qr == null || qr.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    "No hay un qr vigente, debe generar uno nuevo", HttpStatus.NOT_FOUND);
        }

        return new QrResponse(qr.getIdQr(), qr.getIdEntrada(), qr.getToken(), qr.getFechaExpiracion());
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
}