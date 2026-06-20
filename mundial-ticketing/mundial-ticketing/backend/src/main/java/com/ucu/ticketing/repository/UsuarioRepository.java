package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

// maneja el acceso a datos de la tabla Usuario y su tabla
// hija Usuario_Telefono (multivaluada)
@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // mapea una fila de Usuario a un objeto Usuario
    // no carga los telefonos aca, eso se hace aparte porque
    // viven en otra tabla (evita duplicar filas con un join)
    private final RowMapper<Usuario> usuarioRowMapper = (rs, rowNum) -> {
        Usuario u = new Usuario();
        u.setMail(rs.getString("mail"));
        u.setPassword(rs.getString("password"));
        u.setRol(rs.getString("rol"));
        u.setDocPais(rs.getString("doc_pais"));
        u.setDocTipo(rs.getString("doc_tipo"));
        u.setDocNumero(rs.getString("doc_numero"));
        u.setDirPais(rs.getString("dir_pais"));
        u.setDirLocalidad(rs.getString("dir_localidad"));
        u.setDirCalle(rs.getString("dir_calle"));
        u.setDirNumero(rs.getString("dir_numero"));
        u.setDirCodPostal(rs.getString("dir_cod_postal"));
        return u;
    };

    // busca un usuario por mail, sin cargar telefonos
    // devuelve null si no existe
    public Usuario findByMail(String mail) {
        String sql = "select * from Usuario where mail = ?";
        List<Usuario> resultado = jdbc.query(sql, usuarioRowMapper, mail);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // busca un usuario por mail y le carga la lista de telefonos
    // se usa cuando se necesita el objeto completo, ej: para
    // armar el UsuarioResponse del perfil
    public Usuario findByMailConTelefonos(String mail) {
        Usuario usuario = findByMail(mail);
        if (usuario != null) {
            usuario.setTelefonos(findTelefonosByMail(mail));
        }
        return usuario;
    }

    // verifica si ya existe un usuario con ese mail
    // se usa antes de registrar, para no violar la pk
    public boolean existsByMail(String mail) {
        String sql = "select count(*) from Usuario where mail = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, mail);
        return count != null && count > 0;
    }

    // verifica si ya existe un usuario con ese documento
    // (pais + tipo + numero), ya que el documento es unico
    public boolean existsByDocumento(String docPais, String docTipo, String docNumero) {
        String sql = "select count(*) from Usuario " +
                     "where doc_pais = ? and doc_tipo = ? and doc_numero = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, docPais, docTipo, docNumero);
        return count != null && count > 0;
    }

    // inserta un nuevo usuario base (sin telefonos todavia)
    // el password que llega aca ya debe venir hasheado con bcrypt,
    // el hash se hace en el service, no en el repository
    public void insert(Usuario usuario) {
        String sql = "insert into Usuario " +
                     "(mail, password, rol, doc_pais, doc_tipo, doc_numero, " +
                     " dir_pais, dir_localidad, dir_calle, dir_numero, dir_cod_postal) " +
                     "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbc.update(sql,
                usuario.getMail(),
                usuario.getPassword(),
                usuario.getRol(),
                usuario.getDocPais(),
                usuario.getDocTipo(),
                usuario.getDocNumero(),
                usuario.getDirPais(),
                usuario.getDirLocalidad(),
                usuario.getDirCalle(),
                usuario.getDirNumero(),
                usuario.getDirCodPostal());
    }

    // agrega un telefono a un usuario existente
    // se llama una vez por cada telefono de la lista al registrarse
    public void insertTelefono(String mail, String telefono) {
        String sql = "insert into Usuario_Telefono (mail, telefono) values (?, ?)";
        jdbc.update(sql, mail, telefono);
    }

    // devuelve la lista de telefonos asociados a un usuario
    public List<String> findTelefonosByMail(String mail) {
        String sql = "select telefono from Usuario_Telefono where mail = ?";
        return jdbc.queryForList(sql, String.class, mail);
    }

    // actualiza los datos basicos del usuario (no el password ni el rol,
    // esos tienen sus propios metodos especificos por seguridad)
    public void updateDatosBasicos(Usuario usuario) {
        String sql = "update Usuario set " +
                     "dir_pais = ?, dir_localidad = ?, dir_calle = ?, " +
                     "dir_numero = ?, dir_cod_postal = ? " +
                     "where mail = ?";
        jdbc.update(sql,
                usuario.getDirPais(),
                usuario.getDirLocalidad(),
                usuario.getDirCalle(),
                usuario.getDirNumero(),
                usuario.getDirCodPostal(),
                usuario.getMail());
    }
}