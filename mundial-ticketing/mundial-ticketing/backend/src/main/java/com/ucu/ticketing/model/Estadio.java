package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estadio {

    private Long idEstadio;
    private String nombreEstadio;
    private Long idPaisSede;
    private String nombrePais;
    private Long idDireccion;

    private List<Sector> sectores;
}