package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.QR;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

// maneja el acceso a datos de la tabla QR
// cada entrada acumula un historico de codigos qr generados,
// pero solo uno esta activo a la vez (el ultimo)
@Repository
public class QRRepository {

    private final JdbcTemplate jdbc;

    public QRRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<QR> rowMapper = (rs, rowNum) -> {
        QR qr = new QR();
        qr.setIdQr(rs.getInt("id_qr"));
        qr.setIdEntrada(rs.getInt("id_entrada"));
        qr.setToken(rs.getString("token"));
        qr.setFechaGeneracion(rs.getTimestamp("fecha_generacion").toLocalDateTime());
        qr.setFechaExpiracion(rs.getTimestamp("fecha_expiracion").toLocalDateTime());
        qr.setActivo(rs.getBoolean("activo"));
        return qr;
    };

    // busca el qr activo de una entrada, devuelve null si no hay
    // ninguno activo (por ejemplo, si la entrada ya fue consumida)
    public QR findActivoByEntrada(Integer idEntrada) {
        String sql = "select * from QR where id_entrada = ? and activo = true " +
                     "order by fecha_generacion desc limit 1";
        List<QR> resultado = jdbc.query(sql, rowMapper, idEntrada);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // busca un qr por su token exacto, se usa al validar el escaneo
    // en la puerta del estadio
    public QR findByToken(String token) {
        String sql = "select * from QR where token = ?";
        List<QR> resultado = jdbc.query(sql, rowMapper, token);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // devuelve el historico completo de qr generados para una entrada,
    // para poder reconstruir la cadena de custodia si se necesita auditar
    public List<QR> findHistorialByEntrada(Integer idEntrada) {
        String sql = "select * from QR where id_entrada = ? order by fecha_generacion asc";
        return jdbc.query(sql, rowMapper, idEntrada);
    }

    // desactiva todos los qr previos de una entrada
    // se llama siempre antes de generar uno nuevo, para garantizar
    // que solo el ultimo generado sea valido
    public void desactivarTodosDeEntrada(Integer idEntrada) {
        String sql = "update QR set activo = false where id_entrada = ?";
        jdbc.update(sql, idEntrada);
    }

    // inserta un nuevo qr activo y devuelve el id autogenerado
    // el llamador es responsable de haber desactivado los anteriores
    public Integer insert(QR qr) {
        String sql = "insert into QR (id_entrada, token, fecha_expiracion, activo) " +
                     "values (?, ?, ?, true)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, qr.getIdEntrada());
            ps.setString(2, qr.getToken());
            ps.setTimestamp(3, Timestamp.valueOf(qr.getFechaExpiracion()));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }
}