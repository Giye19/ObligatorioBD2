package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


//tabla Evento

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evento {

    private Integer idEvento;
    private Integer idEstadio;
    private String mailAdmin;       // quien dio de alta el evento
    private LocalDate fechaEvento;
    private LocalTime horaEvento;

    // Cargados aparte cuando se necesita el detalle completo
    private List<EventoEquipo> equipos;
    private List<EventoSector> sectoresHabilitados;
}