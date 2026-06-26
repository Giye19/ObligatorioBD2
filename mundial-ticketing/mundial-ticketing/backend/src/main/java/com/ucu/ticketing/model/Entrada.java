package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entrada {

    private Long idEntrada;
    private Long idEventoSector;
    private Long idVenta;
    private Long idPropietarioActual;
    private BigDecimal costoEntrada;
    private String estadoEntrada;
}