package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Sector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

// maneja el acceso a datos de la tabla Sector
@Repository
public class SectorRepository {

    private final JdbcTemplate jdbc;

    public SectorRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Sector> rowMapper = (rs, rowNum) -> {
        Sector s = new Sector();
        s.setIdEstadio(rs.getInt("id_estadio"));
        s.setLetra(rs.getString("letra"));
        s.setCapacidad(rs.getInt("capacidad"));
        return s;
    };

    // devuelve todos los sectores de un estadio, ordenados por letra
    public List<Sector> findByEstadio(Integer idEstadio) {
        String sql = "select * from Sector where id_estadio = ? order by letra";
        return jdbc.query(sql, rowMapper, idEstadio);
    }

    // busca un sector especifico por estadio y letra
    // devuelve null si no existe
    public Sector findByEstadioYLetra(Integer idEstadio, String letra) {
        String sql = "select * from Sector where id_estadio = ? and letra = ?";
        List<Sector> resultado = jdbc.query(sql, rowMapper, idEstadio, letra);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // verifica que un sector exista dentro de un estadio
    // se usa al habilitar sectores para un evento
    public boolean existsByEstadioYLetra(Integer idEstadio, String letra) {
        return findByEstadioYLetra(idEstadio, letra) != null;
    }

    // inserta un nuevo sector para un estadio
    public void insert(Sector sector) {
        String sql = "insert into Sector (id_estadio, letra, capacidad) values (?, ?, ?)";
        jdbc.update(sql, sector.getIdEstadio(), sector.getLetra(), sector.getCapacidad());
    }

    // actualiza la capacidad maxima de un sector
    public void updateCapacidad(Integer idEstadio, String letra, Integer nuevaCapacidad) {
        String sql = "update Sector set capacidad = ? where id_estadio = ? and letra = ?";
        jdbc.update(sql, nuevaCapacidad, idEstadio, letra);
    }
}