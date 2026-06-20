package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

//tabla Comision
//fechaFin null significa que esta comision esta vigente actualmente
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comision {

    private Integer idComision;
    private BigDecimal porcentaje;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}