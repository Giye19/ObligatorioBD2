package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QR {

    private Long idQr;
    private Long idEntrada;
    private String token;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaExpiracion;
}