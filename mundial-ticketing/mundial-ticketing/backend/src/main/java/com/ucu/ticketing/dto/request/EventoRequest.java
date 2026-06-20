package com.ucu.ticketing.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


//datos para crear nuevo evento
//solo accesible por admin


@Data
public class EventoRequest {

    @NotNull(message = "El estadio es obligatorio")
    private Integer idEstadio;

    @NotBlank(message = "El equipo local es obligatorio")
    private String equipoLocal;

    @NotBlank(message = "El equipo visitante es obligatorio")
    private String equipoVisitante;

    @NotNull(message = "La fecha del evento es obligatoria")
    @Future(message = "La fecha del evento debe ser futura")
    private LocalDate fechaEvento;

    @NotNull(message = "La hora del evento es obligatoria")
    private LocalTime horaEvento;

    @NotEmpty(message = "Debe habilitar al menos un sector")
    private List<SectorHabilitadoRequest> sectoresHabilitados;
}