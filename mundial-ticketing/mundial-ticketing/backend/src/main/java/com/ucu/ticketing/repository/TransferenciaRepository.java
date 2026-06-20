package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Transferencia;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

// maneja el acceso a datos de la tabla Transferencia
@Repository
public class TransferenciaRepository {

    private final JdbcTemplate jdbc;

    public TransferenciaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Transferencia> rowMapper = (rs, rowNum) -> {
        Transferencia t = new Transferencia();
        t.setIdTransferencia(rs.getInt("id_transferencia"));
        t.setIdEntrada(rs.getInt("id_entrada"));
        t.setMailOrigen(rs.getString("mail_origen"));
        t.setMailDestino(rs.getString("mail_destino"));
        t.setFechaTransferencia(rs.getTimestamp("fecha_transferencia").toLocalDateTime());
        t.setEstado(rs.getString("estado"));
        return t;
    };

    // busca una transferencia por id, devuelve null si no existe
    public Transferencia findById(Integer idTransferencia) {
        String sql = "select * from Transferencia where id_transferencia = ?";
        List<Transferencia> resultado = jdbc.query(sql, rowMapper, idTransferencia);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // devuelve el historico completo de transferencias de una entrada,
    // ordenado cronologicamente, para reconstruir la cadena de custodia
    public List<Transferencia> findByEntrada(Integer idEntrada) {
        String sql = "select * from Transferencia where id_entrada = ? " +
                     "order by fecha_transferencia asc";
        return jdbc.query(sql, rowMapper, idEntrada);
    }

    // devuelve las transferencias donde el usuario fue origen o destino
    // se usa en "ver mis transferencias"
    public List<Transferencia> findByUsuario(String mail) {
        String sql = "select * from Transferencia " +
                     "where mail_origen = ? or mail_destino = ? " +
                     "order by fecha_transferencia desc";
        return jdbc.query(sql, rowMapper, mail, mail);
    }

    // busca si existe una transferencia pendiente para una entrada
    // se usa para no permitir iniciar una segunda transferencia
    // mientras la primera no fue aceptada o rechazada
    public Transferencia findPendientePorEntrada(Integer idEntrada) {
        String sql = "select * from Transferencia " +
                     "where id_entrada = ? and estado = 'PENDIENTE'";
        List<Transferencia> resultado = jdbc.query(sql, rowMapper, idEntrada);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // inserta una nueva transferencia en estado pendiente
    // y devuelve el id autogenerado
    public Integer insert(Transferencia transferencia) {
        String sql = "insert into Transferencia (id_entrada, mail_origen, mail_destino, estado) " +
                     "values (?, ?, ?, 'PENDIENTE')";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, transferencia.getIdEntrada());
            ps.setString(2, transferencia.getMailOrigen());
            ps.setString(3, transferencia.getMailDestino());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    // actualiza el estado de una transferencia (aceptada o rechazada)
    public void updateEstado(Integer idTransferencia, String nuevoEstado) {
        String sql = "update Transferencia set estado = ? where id_transferencia = ?";
        jdbc.update(sql, nuevoEstado, idTransferencia);
    }
}