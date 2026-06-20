package com.ucu.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadioResponse {

    private Integer idEstadio;
    private String nombre;
    private String nombrePais;
    private List<SectorResponse> sectores;
}