package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

//tabla Venta

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    private Integer idVenta;
    private String mailComprador;
    private Integer idComision;
    private LocalDateTime fechaVenta;
    private String estado; // PENDIENTE CONFIRMADA PAGA
    private BigDecimal montoTotal;

    private List<Entrada> entradas;
}