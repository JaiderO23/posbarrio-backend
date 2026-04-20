package com.pos.backend.security;

import com.pos.backend.enums.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Clave secreta - minimo 256 bits para HS256
    private static final String SECRET = "pos-offline-super-secret-key-2024-colombia-jwt";
    private static final long EXPIRATION_MS = 1000L * 60 * 60 * 8; // 8 horas

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // GENERAR TOKEN
    public String generarToken(String nombreUsuario, Rol rol) {
        return Jwts.builder()
                .subject(nombreUsuario)
                .claim("rol", rol.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getKey())
                .compact();
    }

    // EXTRAER NOMBRE DE USUARIO
    public String extraerNombreUsuario(String token) {
        return getClaims(token).getSubject();
    }

    // EXTRAER ROL
    public String extraerRol(String token) {
        return getClaims(token).get("rol", String.class);
    }

    // VALIDAR TOKEN
    public boolean validarToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // OBTENER CLAIMS
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}