package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoSector {

    private Long idEventoSector;
    private Long idEvento;
    private Long idSector;
    private String letraSector;
    private BigDecimal costoEntrada;
}