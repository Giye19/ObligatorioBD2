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

// maneja el acceso a datos de la tabla Evento y sus tablas
// relacionadas Evento_Equipo y Evento_Sector
@Repository
public class EventoRepository {

    // ventana de bloqueo en horas alrededor de un evento existente
    // dentro de la cual no se permite programar otro evento en el
    // mismo estadio (duracion de partido + margen de entrada/salida)
    private static final int VENTANA_BLOQUEO_HORAS = 3;

    private final JdbcTemplate jdbc;

    public EventoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Evento> rowMapper = (rs, rowNum) -> {
        Evento e = new Evento();
        e.setIdEvento(rs.getInt("id_evento"));
        e.setIdEstadio(rs.getInt("id_estadio"));
        e.setMailAdmin(rs.getString("mail_admin"));
        e.setFechaEvento(rs.getDate("fecha_evento").toLocalDate());
        e.setHoraEvento(rs.getTime("hora_evento").toLocalTime());
        return e;
    };

    // devuelve todos los eventos, ordenados por fecha y hora
    public List<Evento> findAll() {
        String sql = "select * from Evento order by fecha_evento, hora_evento";
        return jdbc.query(sql, rowMapper);
    }

    // busca un evento por id, devuelve null si no existe
    public Evento findById(Integer idEvento) {
        String sql = "select * from Evento where id_evento = ?";
        List<Evento> resultado = jdbc.query(sql, rowMapper, idEvento);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // devuelve todos los eventos de un estadio especifico
    public List<Evento> findByEstadio(Integer idEstadio) {
        String sql = "select * from Evento where id_estadio = ? order by fecha_evento, hora_evento";
        return jdbc.query(sql, rowMapper, idEstadio);
    }

    // verifica si existe superposicion de horarios para un nuevo evento
    // en el mismo estadio y fecha, dentro de la ventana de bloqueo
    // se usa antes de insertar un evento, para impedir el solapamiento
    public boolean existeSuperposicion(Integer idEstadio, LocalDate fecha, LocalTime hora) {
        String sql = "select count(*) from Evento " +
                     "where id_estadio = ? and fecha_evento = ? " +
                     "and abs(time_to_sec(timediff(hora_evento, ?))) < ?";

        Integer count = jdbc.queryForObject(sql, Integer.class,
                idEstadio,
                Date.valueOf(fecha),
                Time.valueOf(hora),
                VENTANA_BLOQUEO_HORAS * 3600);

        return count != null && count > 0;
    }

    // inserta un nuevo evento y devuelve el id autogenerado
    // no valida superposicion aca, eso se hace antes desde el service
    // llamando a existeSuperposicion
    public Integer insert(Evento evento) {
        String sql = "insert into Evento (id_estadio, mail_admin, fecha_evento, hora_evento) " +
                     "values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, evento.getIdEstadio());
            ps.setString(2, evento.getMailAdmin());
            ps.setDate(3, Date.valueOf(evento.getFechaEvento()));
            ps.setTime(4, Time.valueOf(evento.getHoraEvento()));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    // agrega un equipo participante a un evento, con su condicion
    public void insertEquipo(Integer idEvento, String nombreEquipo, String condicion) {
        String sql = "insert into Evento_Equipo (id_evento, nombre_equipo, condicion) " +
                     "values (?, ?, ?)";
        jdbc.update(sql, idEvento, nombreEquipo, condicion);
    }

    // devuelve los equipos participantes de un evento, con su condicion
    // se devuelve como lista de arreglos [nombreEquipo, condicion]
    // para mantener el repository simple sin crear otro rowmapper
    public List<Object[]> findEquiposByEvento(Integer idEvento) {
        String sql = "select nombre_equipo, condicion from Evento_Equipo where id_evento = ?";
        return jdbc.query(sql, (rs, rowNum) ->
                new Object[]{rs.getString("nombre_equipo"), rs.getString("condicion")},
                idEvento);
    }

    // habilita un sector para un evento, con su costo de entrada
    public void insertSectorHabilitado(Integer idEvento, Integer idEstadio,
                                        String letraSector, java.math.BigDecimal costoEntrada) {
        String sql = "insert into Evento_Sector (id_evento, id_estadio, letra_sector, costo_entrada) " +
                     "values (?, ?, ?, ?)";
        jdbc.update(sql, idEvento, idEstadio, letraSector, costoEntrada);
    }

    // devuelve los sectores habilitados para un evento, con su costo
    public List<Object[]> findSectoresHabilitadosByEvento(Integer idEvento) {
        String sql = "select letra_sector, costo_entrada from Evento_Sector where id_evento = ?";
        return jdbc.query(sql, (rs, rowNum) ->
                new Object[]{rs.getString("letra_sector"), rs.getBigDecimal("costo_entrada")},
                idEvento);
    }

    // busca el costo de entrada de un sector especifico dentro de un evento
    // devuelve null si ese sector no esta habilitado para ese evento
    public java.math.BigDecimal findCostoEntrada(Integer idEvento, Integer idEstadio, String letraSector) {
        String sql = "select costo_entrada from Evento_Sector " +
                     "where id_evento = ? and id_estadio = ? and letra_sector = ?";
        List<java.math.BigDecimal> resultado = jdbc.queryForList(sql, java.math.BigDecimal.class,
                idEvento, idEstadio, letraSector);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // verifica si un sector esta habilitado para un evento
    public boolean sectorHabilitado(Integer idEvento, Integer idEstadio, String letraSector) {
        return findCostoEntrada(idEvento, idEstadio, letraSector) != null;
    }
}