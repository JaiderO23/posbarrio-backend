package com.pos.backend.controller;

import com.pos.backend.dto.request.LoginRequest;
import com.pos.backend.dto.response.LoginResponse;
import com.pos.backend.enums.Rol;
import com.pos.backend.model.Usuario;
import com.pos.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;


    // POST - Crear usuario
    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(@Valid @RequestBody Usuario usuario) {
        Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }


    // POST - Login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Usuario usuario = usuarioService.login(
                loginRequest.getNombreUsuario(),
                loginRequest.getContraseña()
        );

        // Crear response sin contraseña
        LoginResponse response = new LoginResponse(usuario);

        return ResponseEntity.ok(response);
    }


    // GET - Obtener todos los usuarios
    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodos() {
        List<Usuario> usuarios = usuarioService.obtenerTodos();
        return ResponseEntity.ok(usuarios);
    }


    // GET - Obtener usuarios activos
    @GetMapping("/activos")
    public ResponseEntity<List<Usuario>> obtenerActivos() {
        List<Usuario> usuarios = usuarioService.obtenerActivos();
        return ResponseEntity.ok(usuarios);
    }


    // GET - Obtener usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(usuario);
    }


    // GET - Obtener usuario por nombre de usuario
    @GetMapping("/nombre/{nombreUsuario}")
    public ResponseEntity<Usuario> obtenerPorNombreUsuario(@PathVariable String nombreUsuario) {
        Usuario usuario = usuarioService.obtenerPorNombreUsuario(nombreUsuario);
        return ResponseEntity.ok(usuario);
    }


    // GET - Obtener usuarios por rol
    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<Usuario>> obtenerPorRol(@PathVariable Rol rol) {
        List<Usuario> usuarios = usuarioService.obtenerPorRol(rol);
        return ResponseEntity.ok(usuarios);
    }


    // GET - Obtener usuarios por rol (solo activos)
    @GetMapping("/rol/{rol}/activos")
    public ResponseEntity<List<Usuario>> obtenerPorRolActivos(@PathVariable Rol rol) {
        List<Usuario> usuarios = usuarioService.obtenerPorRolActivos(rol);
        return ResponseEntity.ok(usuarios);
    }


    // PUT - Actualizar usuario
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody Usuario usuario) {
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuario);
        return ResponseEntity.ok(usuarioActualizado);
    }


    // PATCH - Cambiar contraseña
    @PatchMapping("/{id}/cambiar-contraseña")
    public ResponseEntity<Usuario> cambiarContraseña(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwords) {

        String contraseñaActual = passwords.get("contraseñaActual");
        String contraseñaNueva = passwords.get("contraseñaNueva");

        Usuario usuario = usuarioService.cambiarContraseña(id, contraseñaActual, contraseñaNueva);
        return ResponseEntity.ok(usuario);
    }


    // DELETE - Eliminar usuario (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }


    // DELETE - Eliminar usuario permanentemente
    @DeleteMapping("/{id}/permanente")
    public ResponseEntity<Void> eliminarUsuarioPermanente(@PathVariable Long id) {
        usuarioService.eliminarUsuarioPermanente(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH - Activar usuario
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Usuario> activarUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        usuario.setActivo(true);
        usuario.setVersion(usuario.getVersion() + 1);
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuario);
        return ResponseEntity.ok(usuarioActualizado);
    }
}