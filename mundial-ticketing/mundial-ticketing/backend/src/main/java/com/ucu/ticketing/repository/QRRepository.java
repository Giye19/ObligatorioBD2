package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.QR;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class QRRepository {

    private final JdbcTemplate jdbc;
    private final SimpleJdbcCall callAbrirSesion;
    private final SimpleJdbcCall callObtenerQrActual;
    private final SimpleJdbcCall callValidarQr;

    public QRRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;

        this.callAbrirSesion = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_abrir_sesion_qr");

        this.callObtenerQrActual = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_obtener_qr_actual");

        this.callValidarQr = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_validar_qr");
    }

    private final RowMapper<QR> rowMapper = (rs, rowNum) -> {
        QR qr = new QR();
        qr.setIdQr(rs.getLong("Id_QR"));
        qr.setIdEntrada(rs.getLong("Id_Entrada"));
        qr.setToken(rs.getString("Token"));
        qr.setFechaGeneracion(rs.getTimestamp("Fecha_Generacion").toLocalDateTime());
        qr.setFechaExpiracion(rs.getTimestamp("Fecha_Expiracion").toLocalDateTime());
        return qr;
    };

    /**
     * llama a sp_abrir_sesion_qr, que crea el qr si no existe
     * (el trigger trg_qr_generar_token_insert genera el token real)
     * y abre o reactiva la sesion qr activa. devuelve el qr vigente.
     */
    public QR abrirSesion(Long idEntrada) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_id_entrada", idEntrada);

        Map<String, Object> resultado = callAbrirSesion.execute(params);
        return extraerPrimerQr(resultado);
    }

    /**
     * llama a sp_obtener_qr_actual. si el token vigente expiro,
     * el procedimiento lo regenera antes de devolverlo.
     * se llama periodicamente desde el cliente cada pocos segundos.
     */
    public QR obtenerActual(Long idEntrada) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_id_entrada", idEntrada);

        Map<String, Object> resultado = callObtenerQrActual.execute(params);
        return extraerPrimerQr(resultado);
    }

    /**
     * llama a sp_cerrar_sesion_qr directamente con jdbc, ya que
     * no devuelve resultset (solo hace un update interno)
     */
    public void cerrarSesion(Long idQr) {
        jdbc.update("CALL sp_cerrar_sesion_qr(?)", idQr);
    }

    /**
     * llama a sp_validar_qr, que hace toda la validacion de acceso
     * (dispositivo autorizado, qr vigente, funcionario asignado al
     * sector) y registra la validacion en una sola operacion atomica
     */
    public void validarQr(String tokenLeido, Long idDispositivo, String puertaIngreso) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_token_leido", tokenLeido);
        params.put("p_id_dispositivo", idDispositivo);
        params.put("p_puerta_ingreso", puertaIngreso);

        callValidarQr.execute(params);
    }

    @SuppressWarnings("unchecked")
    private QR extraerPrimerQr(Map<String, Object> resultadoProcedure) {
        for (Object valor : resultadoProcedure.values()) {
            if (valor instanceof List<?> lista && !lista.isEmpty()) {
                Object primeraFila = lista.get(0);
                if (primeraFila instanceof Map<?, ?> fila) {
                    return mapearDesdeMap((Map<String, Object>) fila);
                }
            }
        }
        return null;
    }

    private QR mapearDesdeMap(Map<String, Object> fila) {
        QR qr = new QR();
        qr.setIdQr(((Number) fila.get("Id_QR")).longValue());
        qr.setIdEntrada(((Number) fila.get("Id_Entrada")).longValue());
        qr.setToken((String) fila.get("Token"));
        qr.setFechaGeneracion(((java.sql.Timestamp) fila.get("Fecha_Generacion")).toLocalDateTime());
        qr.setFechaExpiracion(((java.sql.Timestamp) fila.get("Fecha_Expiracion")).toLocalDateTime());
        return qr;
    }
}