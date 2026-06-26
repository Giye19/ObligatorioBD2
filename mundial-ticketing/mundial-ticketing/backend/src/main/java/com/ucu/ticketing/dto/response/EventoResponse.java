package com.ucu.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoResponse {

    private Long idEvento;
    private Long idEstadio;
    private String nombreEstadio;
    private String nombrePaisSede;
    private String equipoLocal;
    private String equipoVisitante;
    private LocalDate fechaEvento;
    private LocalTime horaEvento;
    private List<SectorHabilitadoResponse> sectoresHabilitados;
}