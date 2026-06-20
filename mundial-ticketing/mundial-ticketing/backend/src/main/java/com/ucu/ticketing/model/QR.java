package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

//tabla QR
//el codigo se regenera cada 30 segundos mientras la app esta activa
//solo el ultimo QR generado para una entrada esta activo=true
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QR {

    private Integer idQr;
    private Integer idEntrada;
    private String token;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaExpiracion;
    private Boolean activo;
}