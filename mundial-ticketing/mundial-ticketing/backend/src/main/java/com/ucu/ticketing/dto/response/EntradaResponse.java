package com.ucu.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

//lo que se devuelve al front al consultar una entrada
//incluye datos legibles del evento para no requerir otra llamada
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntradaResponse {

    private Integer idEntrada;
    private Integer idEvento;
    private String nombreEstadio;
    private String equipoLocal;
    private String equipoVisitante;
    private String letraSector;
    private String mailPropietario;
    private String estado;
    private BigDecimal costoEntrada;
    private Integer cantTransferencias;
}