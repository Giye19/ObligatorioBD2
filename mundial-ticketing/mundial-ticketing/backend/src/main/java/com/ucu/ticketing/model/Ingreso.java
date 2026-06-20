package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

//tabla Ingreso
//al crearse un ingreso la entrada pasa a CONSUMIDA
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingreso {

    private Integer idIngreso;
    private Integer idEntrada;
    private Integer idQr;
    private String idDispositivo;
    private LocalDateTime horaIngreso;
    private String puertaIngreso;
}