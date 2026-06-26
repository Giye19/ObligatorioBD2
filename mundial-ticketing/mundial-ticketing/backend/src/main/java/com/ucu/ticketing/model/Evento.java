package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evento {

    private Long idEvento;
    private Long idEstadio;
    private Long idAdmin;
    private Long idEquipoLocal;
    private Long idEquipoVisitante;
    private LocalDate fechaEvento;
    private LocalTime horaEvento;

    private List<EventoSector> sectoresHabilitados;
}