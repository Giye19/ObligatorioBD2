package com.ucu.ticketing.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utilidad para trabajar con JSON Web Tokens.
 *
 * Genera tokens firmados con HS256 que incluyen:
 *   - subject: mail del usuario (identificador único)
 *   - rol: rol del usuario (ADMIN, FUNCIONARIO, USUARIO)
 *   - iat / exp: timestamps de emisión y expiración
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Genera un token JWT para el usuario dado.
     *
     * @param mail mail del usuario (subject del token)
     * @param rol  rol del usuario (ADMIN | FUNCIONARIO | USUARIO)
     * @return token JWT firmado como String
     */
    public String generateToken(String mail, String rol) {
        return Jwts.builder()
                .setSubject(mail)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae el mail (subject) de un token válido.
     */
    public String extractMail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extrae el rol del claim "rol" del token.
     */
    public String extractRol(String token) {
        return parseClaims(token).get("rol", String.class);
    }

    /**
     * Valida que el token esté bien formado, firmado y no expirado.
     *
     * @return true si el token es válido
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
