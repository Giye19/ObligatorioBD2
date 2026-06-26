package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Dispositivo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class DispositivoRepository {

    private final JdbcTemplate jdbc;

    public DispositivoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Dispositivo> rowMapper = (rs, rowNum) -> {
        Dispositivo d = new Dispositivo();
        d.setIdDispositivo(rs.getLong("Id_Dispositivo"));
        d.setIdFuncionario(rs.getLong("Id_Funcionario"));
        d.setCodigoDispositivo(rs.getString("Codigo_Dispositivo"));
        d.setActivo(rs.getBoolean("Activo"));
        return d;
    };

    public Dispositivo findByCodigo(String codigoDispositivo) {
        String sql = "select * from Dispositivo where Codigo_Dispositivo = ?";
        List<Dispositivo> resultado = jdbc.query(sql, rowMapper, codigoDispositivo);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public boolean estaAutorizado(String codigoDispositivo) {
        Dispositivo d = findByCodigo(codigoDispositivo);
        return d != null && Boolean.TRUE.equals(d.getActivo());
    }

    public Dispositivo findByFuncionario(Long idFuncionario) {
        String sql = "select * from Dispositivo where Id_Funcionario = ?";
        List<Dispositivo> resultado = jdbc.query(sql, rowMapper, idFuncionario);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public Long insert(Long idFuncionario, String codigoDispositivo) {
        String sql = "insert into Dispositivo (Id_Funcionario, Codigo_Dispositivo) values (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, idFuncionario);
            ps.setString(2, codigoDispositivo);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}