package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comision {

    private Long idComision;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal porcentaje;
}