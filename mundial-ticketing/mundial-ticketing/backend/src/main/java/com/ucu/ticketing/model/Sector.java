package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

//tabla sector - entidad debil de estadio
//PK compuesta (idEstadio, letra)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sector {

    private Integer idEstadio;  // FK + parte de la PK compuesta
    private String letra;       // A, B, C, D — parte de la PK compuesta
    private Integer capacidad;
}