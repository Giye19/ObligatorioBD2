package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdmPaisSede {

    private Long idAdmin;
    private Long idUsuario;
    private Long idPaisSede;
    private LocalDate fechaAsignacion;
    private Boolean activo;
}