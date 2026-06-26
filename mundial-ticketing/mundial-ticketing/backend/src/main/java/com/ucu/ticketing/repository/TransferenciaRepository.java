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

@Repository
public class TransferenciaRepository {

    private final JdbcTemplate jdbc;

    public TransferenciaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Transferencia> rowMapper = (rs, rowNum) -> {
        Transferencia t = new Transferencia();
        t.setIdTransferencia(rs.getLong("Id_Transferencia"));
        t.setIdEntrada(rs.getLong("Id_Entrada"));
        t.setIdUsuarioOrigen(rs.getLong("Id_Usuario_Origen"));
        t.setIdUsuarioDestino(rs.getLong("Id_Usuario_Destino"));
        t.setFechaTransferencia(rs.getTimestamp("Fecha_Transferencia").toLocalDateTime());
        t.setEstadoTransferencia(rs.getString("Estado_Transferencia"));
        return t;
    };

    public Transferencia findById(Long idTransferencia) {
        String sql = "select * from Transferencia where Id_Transferencia = ?";
        List<Transferencia> resultado = jdbc.query(sql, rowMapper, idTransferencia);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Transferencia> findByEntrada(Long idEntrada) {
        String sql = "select * from Transferencia where Id_Entrada = ? order by Fecha_Transferencia asc";
        return jdbc.query(sql, rowMapper, idEntrada);
    }

    public List<Transferencia> findByUsuario(Long idUsuarioGeneral) {
        String sql = "select * from Transferencia " +
                     "where Id_Usuario_Origen = ? or Id_Usuario_Destino = ? " +
                     "order by Fecha_Transferencia desc";
        return jdbc.query(sql, rowMapper, idUsuarioGeneral, idUsuarioGeneral);
    }

    public Transferencia findPendientePorEntrada(Long idEntrada) {
        String sql = "select * from Transferencia where Id_Entrada = ? and Estado_Transferencia = 'PENDIENTE'";
        List<Transferencia> resultado = jdbc.query(sql, rowMapper, idEntrada);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public Long insert(Transferencia transferencia) {
        String sql = "insert into Transferencia (Id_Entrada, Id_Usuario_Origen, Id_Usuario_Destino, Estado_Transferencia) " +
                     "values (?, ?, ?, 'PENDIENTE')";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, transferencia.getIdEntrada());
            ps.setLong(2, transferencia.getIdUsuarioOrigen());
            ps.setLong(3, transferencia.getIdUsuarioDestino());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public void updateEstado(Long idTransferencia, String nuevoEstado) {
        String sql = "update Transferencia set Estado_Transferencia = ? where Id_Transferencia = ?";
        jdbc.update(sql, nuevoEstado, idTransferencia);
    }
}