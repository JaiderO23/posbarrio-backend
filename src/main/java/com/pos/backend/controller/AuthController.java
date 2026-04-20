package com.pos.backend.controller;

import com.pos.backend.model.Usuario;
import com.pos.backend.security.JwtUtil;
import com.pos.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String nombreUsuario = credenciales.get("nombreUsuario");
        String contraseña = credenciales.get("contraseña");

        // Validar que vengan los campos
        if (nombreUsuario == null || contraseña == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "nombreUsuario y contraseña son requeridos"));
        }

        try {
            // Verificar credenciales
            Usuario usuario = usuarioService.login(nombreUsuario, contraseña);

            // Generar token JWT
            String token = jwtUtil.generarToken(usuario.getNombreUsuario(), usuario.getRol());

            // Responder con token e info básica
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "id", usuario.getId(),          // ← agregar esto
                    "nombreUsuario", usuario.getNombreUsuario(),
                    "nombreCompleto", usuario.getNombreCompleto(),
                    "rol", usuario.getRol().name()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}