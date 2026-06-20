package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioGeneral {

    private String mail;
    private LocalDate fechaRegistro;
    private String estadoVerificacion; // PENDIENTE  VERIFICADO  RECHAZADO
}