package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Dispositivo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

// maneja el acceso a datos de la tabla Dispositivo
// cada dispositivo de escaneo debe estar vinculado a un
// funcionario de validacion (relacion 1:1)
@Repository
public class DispositivoRepository {

    private final JdbcTemplate jdbc;

    public DispositivoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Dispositivo> rowMapper = (rs, rowNum) -> {
        Dispositivo d = new Dispositivo();
        d.setIdDispositivo(rs.getString("id_dispositivo"));
        d.setMailFuncionario(rs.getString("mail_funcionario"));
        return d;
    };

    // busca un dispositivo por id, devuelve null si no existe
    // o no esta autorizado (no esta en la tabla)
    public Dispositivo findById(String idDispositivo) {
        String sql = "select * from Dispositivo where id_dispositivo = ?";
        List<Dispositivo> resultado = jdbc.query(sql, rowMapper, idDispositivo);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // verifica si un dispositivo esta autorizado (registrado)
    // se usa como primer chequeo antes de cualquier validacion de acceso
    public boolean estaAutorizado(String idDispositivo) {
        return findById(idDispositivo) != null;
    }

    // busca el dispositivo vinculado a un funcionario especifico
    public Dispositivo findByFuncionario(String mailFuncionario) {
        String sql = "select * from Dispositivo where mail_funcionario = ?";
        List<Dispositivo> resultado = jdbc.query(sql, rowMapper, mailFuncionario);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // registra un nuevo dispositivo autorizado, vinculado a un funcionario
    public void insert(Dispositivo dispositivo) {
        String sql = "insert into Dispositivo (id_dispositivo, mail_funcionario) values (?, ?)";
        jdbc.update(sql, dispositivo.getIdDispositivo(), dispositivo.getMailFuncionario());
    }
}