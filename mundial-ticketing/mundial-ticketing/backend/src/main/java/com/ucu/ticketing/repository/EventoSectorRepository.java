package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.EventoSector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class EventoSectorRepository {

    private final JdbcTemplate jdbc;

    public EventoSectorRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_BASE =
            "select es.Id_Evento_Sector, es.Id_Evento, es.Id_Sector, es.Costo_Entrada, s.Letra " +
            "from Evento_Sector es " +
            "join Sector s on s.Id_Sector = es.Id_Sector ";

    private final RowMapper<EventoSector> rowMapper = (rs, rowNum) -> {
        EventoSector es = new EventoSector();
        es.setIdEventoSector(rs.getLong("Id_Evento_Sector"));
        es.setIdEvento(rs.getLong("Id_Evento"));
        es.setIdSector(rs.getLong("Id_Sector"));
        es.setCostoEntrada(rs.getBigDecimal("Costo_Entrada"));
        es.setLetraSector(rs.getString("Letra"));
        return es;
    };

    public List<EventoSector> findByEvento(Long idEvento) {
        String sql = SELECT_BASE + "where es.Id_Evento = ?";
        return jdbc.query(sql, rowMapper, idEvento);
    }

    public EventoSector findById(Long idEventoSector) {
        String sql = SELECT_BASE + "where es.Id_Evento_Sector = ?";
        List<EventoSector> resultado = jdbc.query(sql, rowMapper, idEventoSector);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public EventoSector findByEventoYSector(Long idEvento, Long idSector) {
        String sql = SELECT_BASE + "where es.Id_Evento = ? and es.Id_Sector = ?";
        List<EventoSector> resultado = jdbc.query(sql, rowMapper, idEvento, idSector);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public boolean existePorEventoYSector(Long idEvento, Long idSector) {
        return findByEventoYSector(idEvento, idSector) != null;
    }

    public Long insert(Long idEvento, Long idSector, BigDecimal costoEntrada) {
        String sql = "insert into Evento_Sector (Id_Evento, Id_Sector, Costo_Entrada) values (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, idEvento);
            ps.setLong(2, idSector);
            ps.setBigDecimal(3, costoEntrada);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public int countEntradasVendidas(Long idEventoSector) {
        String sql = "select count(*) from Entrada where Id_Evento_Sector = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, idEventoSector);
        return count != null ? count : 0;
    }
}