package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

//tabla Transferencia
//registra cada cambio de titularidad de una entrada
//el cambio de propietario real (en Entrada.mailPropietario)
//solo ocurre cuando estado pasa a ACEPTADA
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transferencia {

    private Integer idTransferencia;
    private Integer idEntrada;
    private String mailOrigen;
    private String mailDestino;
    private LocalDateTime fechaTransferencia;
    private String estado; // PENDIENTE  ACEPTADA  RECHAZADA
}