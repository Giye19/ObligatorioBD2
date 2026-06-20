package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;


//tabla Evento_Sector

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoSector {

    private Integer idEvento;
    private Integer idEstadio;
    private String letraSector;
    private BigDecimal costoEntrada;
}