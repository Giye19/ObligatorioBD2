package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Usuario> rowMapper = (rs, rowNum) -> {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getLong("Id_Usuario"));
        u.setIdDocumento(rs.getLong("Id_Documento"));
        u.setIdDireccion(rs.getLong("Id_Direccion"));
        u.setMail(rs.getString("Mail"));
        u.setPassword(rs.getString("Contrasena"));
        u.setNombre(rs.getString("Nombre"));
        u.setApellido(rs.getString("Apellido"));
        u.setFechaCreacion(rs.getTimestamp("Fecha_Creacion").toLocalDateTime());
        return u;
    };

    public Usuario findByMail(String mail) {
        String sql = "select * from Usuario where Mail = ?";
        List<Usuario> resultado = jdbc.query(sql, rowMapper, mail);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public Usuario findById(Long idUsuario) {
        String sql = "select * from Usuario where Id_Usuario = ?";
        List<Usuario> resultado = jdbc.query(sql, rowMapper, idUsuario);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public boolean existsByMail(String mail) {
        String sql = "select count(*) from Usuario where Mail = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, mail);
        return count != null && count > 0;
    }

    public Long insert(Usuario usuario) {
        String sql = "insert into Usuario (Id_Documento, Id_Direccion, Mail, Contrasena, Nombre, Apellido) " +
                     "values (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, usuario.getIdDocumento());
            ps.setLong(2, usuario.getIdDireccion());
            ps.setString(3, usuario.getMail());
            ps.setString(4, usuario.getPassword());
            ps.setString(5, usuario.getNombre());
            ps.setString(6, usuario.getApellido());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public void insertTelefono(Long idUsuario, String telefono) {
        String sql = "insert into Usuario_Telefono (Id_Usuario, Telefono) values (?, ?)";
        jdbc.update(sql, idUsuario, telefono);
    }

    public List<String> findTelefonosByIdUsuario(Long idUsuario) {
        String sql = "select Telefono from Usuario_Telefono where Id_Usuario = ?";
        return jdbc.queryForList(sql, String.class, idUsuario);
    }


    //determina el rol de un usuario consultando las 3 tablas de especializacion de roles
    public String findRolByIdUsuario(Long idUsuario) {
        String sqlAdmin = "select count(*) from Adm_Pais_Sede where Id_Usuario = ?";
        Integer esAdmin = jdbc.queryForObject(sqlAdmin, Integer.class, idUsuario);
        if (esAdmin != null && esAdmin > 0) {
            return "ADMIN";
        }

        String sqlFuncionario = "select count(*) from Func_Validacion where Id_Usuario = ?";
        Integer esFuncionario = jdbc.queryForObject(sqlFuncionario, Integer.class, idUsuario);
        if (esFuncionario != null && esFuncionario > 0) {
            return "FUNCIONARIO";
        }

        String sqlUsuarioGeneral = "select count(*) from Usuario_General where Id_Usuario = ?";
        Integer esUsuarioGeneral = jdbc.queryForObject(sqlUsuarioGeneral, Integer.class, idUsuario);
        if (esUsuarioGeneral != null && esUsuarioGeneral > 0) {
            return "USUARIO";
        }

        return null;
    }
}