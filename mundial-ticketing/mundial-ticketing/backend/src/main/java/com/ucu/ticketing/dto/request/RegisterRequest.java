package com.ucu.ticketing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;


@Data
public class RegisterRequest {

    @NotBlank(message = "El mail es obligatorio")
    @Email(message = "El mail debe tener formato válido")
    private String mail;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    // Documento
    @NotBlank(message = "El país del documento es obligatorio")
    private String docPais;

    @NotBlank(message = "El tipo de documento es obligatorio")
    private String docTipo;

    @NotBlank(message = "El número de documento es obligatorio")
    private String docNumero;

    // Dirección
    @NotBlank(message = "El país de la dirección es obligatorio")
    private String dirPais;

    @NotBlank(message = "La localidad es obligatoria")
    private String dirLocalidad;

    @NotBlank(message = "La calle es obligatoria")
    private String dirCalle;

    @NotBlank(message = "El número de dirección es obligatorio")
    private String dirNumero;

    @NotBlank(message = "El código postal es obligatorio")
    private String dirCodPostal;

    @NotEmpty(message = "Debe registrar al menos un teléfono")
    private List<String> telefonos;
}
