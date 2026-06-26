package com.ucu.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaResponse {

    private Long idTransferencia;
    private Long idEntrada;
    private String mailOrigen;
    private String mailDestino;
    private LocalDateTime fechaTransferencia;
    private String estado;
}