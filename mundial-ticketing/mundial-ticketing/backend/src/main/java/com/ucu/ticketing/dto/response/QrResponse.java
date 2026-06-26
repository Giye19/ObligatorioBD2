package com.ucu.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


//el front debe volver a pedir este endpoint cada 30 segundos para mostrar siempre el codigo vigente
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QrResponse {

    private Long idQr;
    private Long idEntrada;
    private String token;
    private LocalDateTime fechaExpiracion;
}