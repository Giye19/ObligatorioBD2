package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Estadio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

// maneja el acceso a datos de la tabla Estadio
@Repository
public class EstadioRepository {

    private final JdbcTemplate jdbc;

    public EstadioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Estadio> rowMapper = (rs, rowNum) -> {
        Estadio e = new Estadio();
        e.setIdEstadio(rs.getInt("id_estadio"));
        e.setNombre(rs.getString("nombre"));
        e.setNombrePais(rs.getString("nombre_pais"));
        return e;
    };

    // devuelve todos los estadios registrados
    public List<Estadio> findAll() {
        String sql = "select * from Estadio";
        return jdbc.query(sql, rowMapper);
    }

    // devuelve todos los estadios de un pais sede especifico
    // se usa para que un admin solo vea los estadios de su jurisdiccion
    public List<Estadio> findByPais(String nombrePais) {
        String sql = "select * from Estadio where nombre_pais = ?";
        return jdbc.query(sql, rowMapper, nombrePais);
    }

    // busca un estadio por id, sin sectores cargados
    // devuelve null si no existe
    public Estadio findById(Integer idEstadio) {
        String sql = "select * from Estadio where id_estadio = ?";
        List<Estadio> resultado = jdbc.query(sql, rowMapper, idEstadio);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // inserta un nuevo estadio y devuelve el id autogenerado
    public Integer insert(Estadio estadio) {
        String sql = "insert into Estadio (nombre, nombre_pais) values (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, estadio.getNombre());
            ps.setString(2, estadio.getNombrePais());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }
}