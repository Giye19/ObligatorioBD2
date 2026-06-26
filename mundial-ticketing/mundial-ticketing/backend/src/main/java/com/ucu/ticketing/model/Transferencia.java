package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transferencia {

    private Long idTransferencia;
    private Long idEntrada;
    private Long idUsuarioOrigen;
    private Long idUsuarioDestino;
    private LocalDateTime fechaTransferencia;
    private String estadoTransferencia;
}