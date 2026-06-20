package com.ucu.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// representa una fila del ranking de usuarios con mas entradas compradas
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingCompradorResponse {

    private String mailComprador;
    private Integer cantidadEntradasCompradas;
}