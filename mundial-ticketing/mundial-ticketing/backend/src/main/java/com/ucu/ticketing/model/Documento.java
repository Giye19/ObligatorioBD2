package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Documento {

    private Long idDocumento;
    private Long idPais;
    private String nombrePais;
    private String tipoDocumento;
    private String numeroDocumento;
}