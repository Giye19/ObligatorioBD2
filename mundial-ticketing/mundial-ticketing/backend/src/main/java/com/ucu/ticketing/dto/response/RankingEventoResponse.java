package com.ucu.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// representa una fila del ranking de eventos con mas entradas vendidas
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingEventoResponse {

    private Integer idEvento;
    private String nombreEstadio;
    private String equipoLocal;
    private String equipoVisitante;
    private LocalDate fechaEvento;
    private Integer cantidadEntradasVendidas;
}