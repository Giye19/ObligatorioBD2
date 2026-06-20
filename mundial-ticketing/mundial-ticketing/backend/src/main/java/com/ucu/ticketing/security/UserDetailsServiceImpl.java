package com.ucu.ticketing.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de UserDetailsService de Spring Security.
 *
 * Busca en la tabla Usuario por mail y devuelve un UserDetails
 * con el rol correspondiente. Spring Security usa este servicio
 * durante el proceso de autenticación (login).
 *
 * NOTA: Los nombres de tabla/columna aquí deben coincidir
 * exactamente con el script SQL del compañero de BD.
 * Ajustar si difieren.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final JdbcTemplate jdbc;

    public UserDetailsServiceImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        String sql = "SELECT u.mail, u.password, u.rol " +
                     "FROM Usuario u " +
                     "WHERE u.mail = ?";

        return jdbc.query(sql, rs -> {
            if (!rs.next()) {
                throw new UsernameNotFoundException("Usuario no encontrado: " + mail);
            }
            String password = rs.getString("password");
            String rol      = rs.getString("rol");

            return User.builder()
                    .username(mail)
                    .password(password)
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + rol)))
                    .build();
        }, mail);
    }
}
