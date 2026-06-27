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

@Service
public class QrService {

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

    /**
     * abre (o reactiva) la sesion qr de una entrada, llamando al
     * procedimiento sp_abrir_sesion_qr. el token real lo genera
     * un trigger en la base al insertarse el qr por primera vez.
     */
    public QrResponse abrirSesion(Long idEntrada, String mailSolicitante) {
        validarPropietario(idEntrada, mailSolicitante);

        QR qr = qrRepository.abrirSesion(idEntrada);
        if (qr == null) {
            throw new BusinessException(
                    "No se pudo abrir la sesión de QR", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new QrResponse(qr.getIdQr(), qr.getIdEntrada(), qr.getToken(), qr.getFechaExpiracion());
    }

    /**
     * obtiene el qr vigente de una entrada, llamando al
     * procedimiento sp_obtener_qr_actual. si el token vigente
     * expiro, el procedimiento lo regenera antes de devolverlo.
     * el cliente llama este metodo periodicamente (cada pocos
     * segundos) mientras la pantalla de qr esta abierta.
     */
    public QrResponse obtenerActual(Long idEntrada, String mailSolicitante) {
        validarPropietario(idEntrada, mailSolicitante);

        QR qr = qrRepository.obtenerActual(idEntrada);
        if (qr == null) {
            throw new BusinessException(
                    "No hay una sesión de QR abierta para esta entrada", HttpStatus.NOT_FOUND);
        }

        return new QrResponse(qr.getIdQr(), qr.getIdEntrada(), qr.getToken(), qr.getFechaExpiracion());
    }

    /**
     * cierra la sesion qr de una entrada, por ejemplo cuando el
     * usuario sale de la pantalla de qr
     */
    public void cerrarSesion(Long idQr, String mailSolicitante) {
        qrRepository.cerrarSesion(idQr);
    }

    private void validarPropietario(Long idEntrada, String mailSolicitante) {
        Usuario usuario = usuarioRepository.findByMail(mailSolicitante);
        if (usuario == null) {
            throw new BusinessException("El usuario no existe", HttpStatus.UNAUTHORIZED);
        }

        UsuarioGeneral usuarioGeneral = usuarioGeneralRepository.findByIdUsuario(usuario.getIdUsuario());
        if (usuarioGeneral == null) {
            throw new BusinessException("El usuario no es un usuario general", HttpStatus.FORBIDDEN);
        }

        Entrada entrada = entradaRepository.findById(idEntrada);
        if (entrada == null) {
            throw new BusinessException("La entrada no existe", HttpStatus.NOT_FOUND);
        }

        if (!entrada.getIdPropietarioActual().equals(usuarioGeneral.getIdUsuarioGeneral())) {
            throw new BusinessException(
                    "Solo el propietario actual puede acceder al QR de esta entrada",
                    HttpStatus.FORBIDDEN);
        }
    }
}