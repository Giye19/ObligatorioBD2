package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Entrada;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class EntradaRepository {

    private final JdbcTemplate jdbc;

    public EntradaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Entrada> rowMapper = (rs, rowNum) -> {
        Entrada e = new Entrada();
        e.setIdEntrada(rs.getLong("Id_Entrada"));
        e.setIdEventoSector(rs.getLong("Id_Evento_Sector"));
        e.setIdVenta(rs.getLong("Id_Venta"));
        e.setIdPropietarioActual(rs.getLong("Id_Propietario_Actual"));
        e.setCostoEntrada(rs.getBigDecimal("Costo_Entrada"));
        e.setEstadoEntrada(rs.getString("Estado_Entrada"));
        return e;
    };

    public Entrada findById(Long idEntrada) {
        String sql = "select * from Entrada where Id_Entrada = ?";
        List<Entrada> resultado = jdbc.query(sql, rowMapper, idEntrada);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Entrada> findByVenta(Long idVenta) {
        String sql = "select * from Entrada where Id_Venta = ?";
        return jdbc.query(sql, rowMapper, idVenta);
    }

    public List<Entrada> findByPropietario(Long idUsuarioGeneral) {
        String sql = "select * from Entrada where Id_Propietario_Actual = ? order by Id_Entrada desc";
        return jdbc.query(sql, rowMapper, idUsuarioGeneral);
    }

    public int countEntradasVendidas(Long idEventoSector) {
        String sql = "select count(*) from Entrada where Id_Evento_Sector = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, idEventoSector);
        return count != null ? count : 0;
    }

    public Long insert(Entrada entrada) {
        String sql = "insert into Entrada " +
                     "(Id_Evento_Sector, Id_Venta, Id_Propietario_Actual, Costo_Entrada, Estado_Entrada) " +
                     "values (?, ?, ?, ?, 'NO_CONSUMIDA')";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, entrada.getIdEventoSector());
            ps.setLong(2, entrada.getIdVenta());
            ps.setLong(3, entrada.getIdPropietarioActual());
            ps.setBigDecimal(4, entrada.getCostoEntrada());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public void marcarConsumida(Long idEntrada) {
        String sql = "update Entrada set Estado_Entrada = 'CONSUMIDA' where Id_Entrada = ?";
        jdbc.update(sql, idEntrada);
    }

    public List<Object[]> findRankingEventos(int limite) {
        String sql = "select ev.Id_Evento, count(*) as cantidad " +
                     "from Entrada e " +
                     "join Evento_Sector es on es.Id_Evento_Sector = e.Id_Evento_Sector " +
                     "join Evento ev on ev.Id_Evento = es.Id_Evento " +
                     "group by ev.Id_Evento " +
                     "order by cantidad desc " +
                     "limit ?";
        return jdbc.query(sql, (rs, rowNum) ->
                new Object[]{rs.getLong("Id_Evento"), rs.getInt("cantidad")},
                limite);
    }

    public int countTransferenciasAceptadas(Long idEntrada) {
        String sql = "select count(*) from Transferencia where Id_Entrada = ? and Estado_Transferencia = 'ACEPTADA'";
        Integer count = jdbc.queryForObject(sql, Integer.class, idEntrada);
        return count != null ? count : 0;
    }
}