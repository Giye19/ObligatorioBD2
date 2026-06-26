package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioGeneral {

    private Long idUsuarioGeneral;
    private Long idUsuario;
    private LocalDateTime fechaRegistro;
    private String estadoVerificacion;
}