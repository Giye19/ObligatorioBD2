package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.FuncValidacion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FuncValidacionRepository {

    private final JdbcTemplate jdbc;

    public FuncValidacionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<FuncValidacion> rowMapper = (rs, rowNum) -> {
        FuncValidacion fv = new FuncValidacion();
        fv.setIdFuncionario(rs.getLong("Id_Funcionario"));
        fv.setIdUsuario(rs.getLong("Id_Usuario"));
        fv.setNroLegajo(rs.getString("Nro_Legajo"));
        fv.setActivo(rs.getBoolean("Activo"));
        return fv;
    };

    public FuncValidacion findByIdUsuario(Long idUsuario) {
        String sql = "select * from Func_Validacion where Id_Usuario = ?";
        List<FuncValidacion> resultado = jdbc.query(sql, rowMapper, idUsuario);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public FuncValidacion findById(Long idFuncionario) {
        String sql = "select * from Func_Validacion where Id_Funcionario = ?";
        List<FuncValidacion> resultado = jdbc.query(sql, rowMapper, idFuncionario);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Long> findSectoresAsignados(Long idFuncionario, Long idEvento) {
        String sql = "select fes.Id_Evento_Sector from Funcionario_Evento_Sector fes " +
                     "join Evento_Sector es on es.Id_Evento_Sector = fes.Id_Evento_Sector " +
                     "where fes.Id_Funcionario = ? and es.Id_Evento = ?";
        return jdbc.query(sql, (rs, rowNum) -> rs.getLong("Id_Evento_Sector"), idFuncionario, idEvento);
    }

    public void asignarSector(Long idFuncionario, Long idEventoSector) {
        String sql = "insert into Funcionario_Evento_Sector (Id_Funcionario, Id_Evento_Sector) values (?, ?)";
        jdbc.update(sql, idFuncionario, idEventoSector);
    }
    public List<FuncValidacion> findAll() {
        String sql = "select * from Func_Validacion";
        return jdbc.query(sql, rowMapper);
    }
}