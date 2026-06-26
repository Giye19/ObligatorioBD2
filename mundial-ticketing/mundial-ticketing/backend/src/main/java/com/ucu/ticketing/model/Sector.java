package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sector {

    private Long idSector;
    private Long idEstadio;
    private String letra;
    private Integer capacidad;
}