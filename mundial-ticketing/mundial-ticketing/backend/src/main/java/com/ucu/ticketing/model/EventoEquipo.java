package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


//tabla Evento_Equipo

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoEquipo {

    private Integer idEvento;
    private String nombreEquipo;
    private String condicion; // LOCAL  VISITANTE
}