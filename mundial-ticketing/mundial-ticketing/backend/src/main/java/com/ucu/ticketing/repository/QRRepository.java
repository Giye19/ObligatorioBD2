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

@Repository
public class QRRepository {

    private final JdbcTemplate jdbc;

    public QRRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
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
     * devuelve el ultimo qr generado para una entrada, sin importar
     * si esta vencido o no (esa validacion la hace el service).
     * como la tabla no tiene columna "activo", el ultimo generado
     * por fecha es, por definicion, el unico que tiene sentido usar
     */
    public QR findUltimoGeneradoPorEntrada(Long idEntrada) {
        String sql = "select * from QR where Id_Entrada = ? order by Fecha_Generacion desc limit 1";
        List<QR> resultado = jdbc.query(sql, rowMapper, idEntrada);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public QR findByToken(String token) {
        String sql = "select * from QR where Token = ?";
        List<QR> resultado = jdbc.query(sql, rowMapper, token);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<QR> findHistorialByEntrada(Long idEntrada) {
        String sql = "select * from QR where Id_Entrada = ? order by Fecha_Generacion asc";
        return jdbc.query(sql, rowMapper, idEntrada);
    }

    public Long insert(QR qr) {
        String sql = "insert into QR (Id_Entrada, Token, Fecha_Expiracion) values (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, qr.getIdEntrada());
            ps.setString(2, qr.getToken());
            ps.setTimestamp(3, Timestamp.valueOf(qr.getFechaExpiracion()));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}