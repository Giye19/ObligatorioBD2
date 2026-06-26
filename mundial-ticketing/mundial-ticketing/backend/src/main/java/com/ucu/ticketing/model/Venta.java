package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    private Long idVenta;
    private Long idUsuarioGeneral;
    private Long idComision;
    private LocalDateTime fechaVenta;
    private String estadoVenta;
    private BigDecimal montoTotal;

    private List<Entrada> entradas;
}