package com.ucu.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// lo que se devuelve al front tras consultar o realizar una venta
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaResponse {

    private Integer idVenta;
    private String mailComprador;
    private LocalDateTime fechaVenta;
    private String estado;
    private BigDecimal montoTotal;
    private BigDecimal porcentajeComisionAplicado;
    private List<EntradaResponse> entradas;
}