package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.FuncValidacion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

// maneja el acceso a datos de la tabla Func_Validacion
// (especializacion de Usuario)
@Repository
public class FuncValidacionRepository {

    private final JdbcTemplate jdbc;

    public FuncValidacionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<FuncValidacion> rowMapper = (rs, rowNum) -> {
        FuncValidacion fv = new FuncValidacion();
        fv.setMail(rs.getString("mail"));
        fv.setNroLegajo(rs.getString("nro_legajo"));
        return fv;
    };

    // busca un funcionario de validacion por mail
    // devuelve null si el usuario no tiene este rol
    public FuncValidacion findByMail(String mail) {
        String sql = "select * from Func_Validacion where mail = ?";
        List<FuncValidacion> resultado = jdbc.query(sql, rowMapper, mail);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // inserta la especializacion de funcionario de validacion
    // solo deberia poder ejecutarse desde un alta administrativa,
    // esa restriccion de quien puede llamarlo se controla en el service
    public void insert(FuncValidacion funcValidacion) {
        String sql = "insert into Func_Validacion (mail, nro_legajo) values (?, ?)";
        jdbc.update(sql, funcValidacion.getMail(), funcValidacion.getNroLegajo());
    }

    // lista todos los sectores a los que esta asignado un funcionario
    // dentro de un estadio especifico
    // se usa para validar la regla: "debe haber validado entradas
    // en todos los sectores a los que fue asignado durante un evento"
    public List<String> findSectoresAsignados(String mailFuncionario, Integer idEstadio) {
        String sql = "select letra_sector from Func_Sector_Asignado " +
                     "where mail_funcionario = ? and id_estadio = ?";
        return jdbc.queryForList(sql, String.class, mailFuncionario, idEstadio);
    }

    // asigna un funcionario a un sector especifico
    public void asignarSector(String mailFuncionario, Integer idEstadio, String letraSector) {
        String sql = "insert into Func_Sector_Asignado " +
                     "(mail_funcionario, id_estadio, letra_sector) values (?, ?, ?)";
        jdbc.update(sql, mailFuncionario, idEstadio, letraSector);
    }
}