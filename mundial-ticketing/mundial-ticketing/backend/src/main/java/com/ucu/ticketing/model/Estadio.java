package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

//tabla estadio

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estadio {

    private Integer idEstadio;
    private String nombre;
    private String nombrePais;     // FK a Pais_Sede

   
    private List<Sector> sectores;
}