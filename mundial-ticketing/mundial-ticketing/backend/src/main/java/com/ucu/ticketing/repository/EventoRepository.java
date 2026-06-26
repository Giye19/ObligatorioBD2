package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Evento;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public class EventoRepository {

    private static final int VENTANA_BLOQUEO_HORAS = 3;

    private final JdbcTemplate jdbc;

    public EventoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Evento> rowMapper = (rs, rowNum) -> {
        Evento e = new Evento();
        e.setIdEvento(rs.getLong("Id_Evento"));
        e.setIdEstadio(rs.getLong("Id_Estadio"));
        e.setIdAdmin(rs.getLong("Id_Admin"));
        e.setIdEquipoLocal(rs.getLong("Id_Equipo_Local"));
        e.setIdEquipoVisitante(rs.getLong("Id_Equipo_Visitante"));
        e.setFechaEvento(rs.getDate("Fecha_Evento").toLocalDate());
        e.setHoraEvento(rs.getTime("Hora_Evento").toLocalTime());
        return e;
    };

    public List<Evento> findAll() {
        String sql = "select * from Evento order by Fecha_Evento, Hora_Evento";
        return jdbc.query(sql, rowMapper);
    }

    public Evento findById(Long idEvento) {
        String sql = "select * from Evento where Id_Evento = ?";
        List<Evento> resultado = jdbc.query(sql, rowMapper, idEvento);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public boolean existeSuperposicion(Long idEstadio, LocalDate fecha, LocalTime hora) {
        String sql = "select count(*) from Evento " +
                     "where Id_Estadio = ? and Fecha_Evento = ? " +
                     "and abs(time_to_sec(timediff(Hora_Evento, ?))) < ?";

        Integer count = jdbc.queryForObject(sql, Integer.class,
                idEstadio,
                Date.valueOf(fecha),
                Time.valueOf(hora),
                VENTANA_BLOQUEO_HORAS * 3600);

        return count != null && count > 0;
    }

    public Long insert(Evento evento) {
        String sql = "insert into Evento " +
                     "(Id_Estadio, Id_Admin, Id_Equipo_Local, Id_Equipo_Visitante, Fecha_Evento, Hora_Evento) " +
                     "values (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, evento.getIdEstadio());
            ps.setLong(2, evento.getIdAdmin());
            ps.setLong(3, evento.getIdEquipoLocal());
            ps.setLong(4, evento.getIdEquipoVisitante());
            ps.setDate(5, Date.valueOf(evento.getFechaEvento()));
            ps.setTime(6, Time.valueOf(evento.getHoraEvento()));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}