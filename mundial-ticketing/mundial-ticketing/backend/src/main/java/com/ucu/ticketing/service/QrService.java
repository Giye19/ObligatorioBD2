package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.response.QrResponse;
import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Entrada;
import com.ucu.ticketing.model.QR;
import com.ucu.ticketing.repository.EntradaRepository;
import com.ucu.ticketing.repository.QRRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

// contiene la logica de generacion del qr dinamico de cada entrada
// el codigo se regenera cada 30 segundos mientras la app esta activa,
// para evitar fraude por captura de pantalla
@Service
public class QrService {

    // duracion en segundos de vigencia de cada codigo qr generado
    // pasado ese tiempo, el qr deja de ser valido para escanear
    private static final int VIGENCIA_QR_SEGUNDOS = 30;

    private final QRRepository qrRepository;
    private final EntradaRepository entradaRepository;

    public QrService(QRRepository qrRepository, EntradaRepository entradaRepository) {
        this.qrRepository = qrRepository;
        this.entradaRepository = entradaRepository;
    }

    // genera un nuevo qr activo para una entrada, desactivando
    // cualquier qr anterior que tuviera
    // solo el propietario actual de la entrada puede generarlo
    @Transactional
    public QrResponse generarNuevo(Integer idEntrada, String mailSolicitante) {

        Entrada entrada = entradaRepository.findById(idEntrada);
        if (entrada == null) {
            throw new BusinessException("La entrada no existe", HttpStatus.NOT_FOUND);
        }

        if (!entrada.getMailPropietario().equals(mailSolicitante)) {
            throw new BusinessException(
                    "Solo el propietario actual puede generar el qr de esta entrada",
                    HttpStatus.FORBIDDEN);
        }

        if ("CONSUMIDA".equals(entrada.getEstado())) {
            throw new BusinessException(
                    "No se puede generar un qr para una entrada ya consumida", HttpStatus.CONFLICT);
        }

        // desactiva cualquier qr previo, para que solo el nuevo sea valido
        qrRepository.desactivarTodosDeEntrada(idEntrada);

        // genera un token unico e impredecible
        String token = UUID.randomUUID().toString();

        QR qr = new QR();
        qr.setIdEntrada(idEntrada);
        qr.setToken(token);
        qr.setFechaExpiracion(LocalDateTime.now().plusSeconds(VIGENCIA_QR_SEGUNDOS));

        Integer idQr = qrRepository.insert(qr);
        qr.setIdQr(idQr);

        return new QrResponse(idQr, idEntrada, token, qr.getFechaExpiracion());
    }

    // devuelve el qr activo actual de una entrada, sin generar uno nuevo
    // se usa si el front quiere mostrar el qr ya generado sin regenerarlo
    public QrResponse obtenerActivo(Integer idEntrada, String mailSolicitante) {

        Entrada entrada = entradaRepository.findById(idEntrada);
        if (entrada == null) {
            throw new BusinessException("La entrada no existe", HttpStatus.NOT_FOUND);
        }

        if (!entrada.getMailPropietario().equals(mailSolicitante)) {
            throw new BusinessException(
                    "Solo el propietario actual puede consultar el qr de esta entrada",
                    HttpStatus.FORBIDDEN);
        }

        QR qr = qrRepository.findActivoByEntrada(idEntrada);
        if (qr == null) {
            throw new BusinessException(
                    "No hay un qr activo, debe generar uno nuevo primero", HttpStatus.NOT_FOUND);
        }

        return new QrResponse(qr.getIdQr(), qr.getIdEntrada(), qr.getToken(), qr.getFechaExpiracion());
    }
}