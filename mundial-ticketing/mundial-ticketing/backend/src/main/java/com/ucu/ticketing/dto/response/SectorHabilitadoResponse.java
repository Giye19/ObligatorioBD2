package com.ucu.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectorHabilitadoResponse {

    private String letraSector;
    private BigDecimal costoEntrada;
    private Integer capacidadTotal;
    private Integer entradasDisponibles;
}