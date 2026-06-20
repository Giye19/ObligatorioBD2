package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

//tabla Entrada
// cantTransferencias se usa para validar la regla de maximo 3 transferencias
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entrada {

    private Integer idEntrada;
    private Integer idVenta;
    private Integer idEvento;
    private Integer idEstadio;
    private String letraSector;
    private String mailPropietario; // propietario actual, cambia con transferencias
    private String estado;          // ACTIVA  TRANSFERIDA  CONSUMIDA
    private BigDecimal costoEntrada;
    private Integer cantTransferencias;
}