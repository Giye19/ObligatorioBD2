package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdmPaisSede {

    private String mail;
    private LocalDate fechaAsignacion;
    private String nombrePais; // FK a Pais_Sede
}