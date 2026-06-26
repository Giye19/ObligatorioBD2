package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Venta;
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
public class VentaRepository {

    private final JdbcTemplate jdbc;

    public VentaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Venta> rowMapper = (rs, rowNum) -> {
        Venta v = new Venta();
        v.setIdVenta(rs.getLong("Id_Venta"));
        v.setIdUsuarioGeneral(rs.getLong("Id_Usuario_General"));
        v.setIdComision(rs.getLong("Id_Comision"));
        v.setFechaVenta(rs.getTimestamp("Fecha_Venta").toLocalDateTime());
        v.setEstadoVenta(rs.getString("Estado_Venta"));
        v.setMontoTotal(rs.getBigDecimal("Monto_Total"));
        return v;
    };

    public Venta findById(Long idVenta) {
        String sql = "select * from Venta where Id_Venta = ?";
        List<Venta> resultado = jdbc.query(sql, rowMapper, idVenta);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Venta> findByUsuarioGeneral(Long idUsuarioGeneral) {
        String sql = "select * from Venta where Id_Usuario_General = ? order by Fecha_Venta desc";
        return jdbc.query(sql, rowMapper, idUsuarioGeneral);
    }

    public Long insert(Venta venta) {
        String sql = "insert into Venta (Id_Usuario_General, Id_Comision, Fecha_Venta, Estado_Venta, Monto_Total) " +
                     "values (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, venta.getIdUsuarioGeneral());
            ps.setLong(2, venta.getIdComision());
            ps.setTimestamp(3, Timestamp.valueOf(venta.getFechaVenta()));
            ps.setString(4, venta.getEstadoVenta());
            ps.setBigDecimal(5, venta.getMontoTotal());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public List<Object[]> findRankingCompradores(int limite) {
        String sql = "select v.Id_Usuario_General, count(e.Id_Entrada) as cantidad " +
                     "from Venta v " +
                     "join Entrada e on e.Id_Venta = v.Id_Venta " +
                     "group by v.Id_Usuario_General " +
                     "order by cantidad desc " +
                     "limit ?";
        return jdbc.query(sql, (rs, rowNum) ->
                new Object[]{rs.getLong("Id_Usuario_General"), rs.getInt("cantidad")},
                limite);
    }
}