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

// maneja el acceso a datos de la tabla Venta
@Repository
public class VentaRepository {

    private final JdbcTemplate jdbc;

    public VentaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Venta> rowMapper = (rs, rowNum) -> {
        Venta v = new Venta();
        v.setIdVenta(rs.getInt("id_venta"));
        v.setMailComprador(rs.getString("mail_comprador"));
        v.setIdComision(rs.getInt("id_comision"));
        v.setFechaVenta(rs.getTimestamp("fecha_venta").toLocalDateTime());
        v.setEstado(rs.getString("estado"));
        v.setMontoTotal(rs.getBigDecimal("monto_total"));
        return v;
    };

    // busca una venta por id, devuelve null si no existe
    public Venta findById(Integer idVenta) {
        String sql = "select * from Venta where id_venta = ?";
        List<Venta> resultado = jdbc.query(sql, rowMapper, idVenta);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // devuelve todas las ventas realizadas por un usuario,
    // ordenadas de la mas reciente a la mas antigua
    // se usa en "ver mis compras"
    public List<Venta> findByComprador(String mailComprador) {
        String sql = "select * from Venta where mail_comprador = ? order by fecha_venta desc";
        return jdbc.query(sql, rowMapper, mailComprador);
    }

    // inserta una nueva venta y devuelve el id autogenerado
    public Integer insert(Venta venta) {
        String sql = "insert into Venta (mail_comprador, id_comision, fecha_venta, estado, monto_total) " +
                     "values (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, venta.getMailComprador());
            ps.setInt(2, venta.getIdComision());
            ps.setTimestamp(3, Timestamp.valueOf(venta.getFechaVenta()));
            ps.setString(4, venta.getEstado());
            ps.setBigDecimal(5, venta.getMontoTotal());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    // actualiza el estado de una venta (pendiente -> confirmada -> paga)
    public void updateEstado(Integer idVenta, String nuevoEstado) {
        String sql = "update Venta set estado = ? where id_venta = ?";
        jdbc.update(sql, nuevoEstado, idVenta);
    }

    // devuelve el ranking de usuarios con mas entradas compradas
    // (mayores compradores), limitado a una cantidad maxima
    // se usa para la funcionalidad de reportes/estadisticas
    public List<Object[]> findRankingCompradores(int limite) {
        String sql = "select v.mail_comprador, count(e.id_entrada) as cantidad " +
                     "from Venta v " +
                     "join Entrada e on e.id_venta = v.id_venta " +
                     "group by v.mail_comprador " +
                     "order by cantidad desc " +
                     "limit ?";
        return jdbc.query(sql, (rs, rowNum) ->
                new Object[]{rs.getString("mail_comprador"), rs.getInt("cantidad")},
                limite);
    }
}