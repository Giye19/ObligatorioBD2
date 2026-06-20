package com.ucu.ticketing.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que se ejecuta una vez por request.
 *
 * Flujo:
 *  1. Busca el header "Authorization: Bearer <token>"
 *  2. Valida el token con JwtUtil
 *  3. Si es válido, carga la autenticación en el SecurityContext
 *     para que Spring Security permita el acceso al endpoint
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                String mail = jwtUtil.extractMail(token);
                String rol  = jwtUtil.extractRol(token);

                // Creamos la authority con prefijo ROLE_ que exige Spring Security
                var authority = new SimpleGrantedAuthority("ROLE_" + rol);
                var auth = new UsernamePasswordAuthenticationToken(
                        mail, null, List.of(authority));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
