package com.pos.backend.service;

import com.pos.backend.enums.Rol;
import com.pos.backend.model.Usuario;
import com.pos.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // CREAR USUARIO
    public Usuario crearUsuario(Usuario usuario) {
        if (usuarioRepository.existsByNombreUsuario(usuario.getNombreUsuario())) {
            throw new RuntimeException("Ya existe un usuario con el nombre: " +
                    usuario.getNombreUsuario());
        }

        usuario.setContraseña(passwordEncoder.encode(usuario.getContraseña()));

        if (usuario.getUuid() == null) {
            usuario.setUuid(UUID.randomUUID());
        }

        return usuarioRepository.save(usuario);
    }

    // LOGIN - VERIFICAR CREDENCIALES
    public Usuario login(String nombreUsuario, String contraseña) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));

        if (!usuario.getActivo()) {
            throw new RuntimeException("El usuario está inactivo");
        }

        if (!passwordEncoder.matches(contraseña, usuario.getContraseña())) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }

        return usuario;
    }

    // OBTENER TODOS LOS USUARIOS
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    // OBTENER USUARIOS ACTIVOS
    public List<Usuario> obtenerActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    // OBTENER USUARIO POR ID
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    // OBTENER USUARIO POR UUID
    public Usuario obtenerPorUuid(UUID uuid) {
        return usuarioRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con UUID: " + uuid));
    }

    // OBTENER USUARIO POR NOMBRE DE USUARIO
    public Usuario obtenerPorNombreUsuario(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + nombreUsuario));
    }

    // OBTENER USUARIOS POR ROL
    public List<Usuario> obtenerPorRol(Rol rol) {
        return usuarioRepository.findByRol(rol);
    }

    // OBTENER USUARIOS POR ROL (SOLO ACTIVOS)
    public List<Usuario> obtenerPorRolActivos(Rol rol) {
        return usuarioRepository.findByRolAndActivoTrue(rol);
    }

    // ACTUALIZAR USUARIO
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        Usuario usuarioExistente = obtenerPorId(id);

        if (!usuarioExistente.getNombreUsuario().equals(usuarioActualizado.getNombreUsuario())) {
            if (usuarioRepository.existsByNombreUsuario(usuarioActualizado.getNombreUsuario())) {
                throw new RuntimeException("Ya existe un usuario con el nombre: " +
                        usuarioActualizado.getNombreUsuario());
            }
        }

        usuarioExistente.setNombreUsuario(usuarioActualizado.getNombreUsuario());
        usuarioExistente.setNombreCompleto(usuarioActualizado.getNombreCompleto());
        usuarioExistente.setRol(usuarioActualizado.getRol());
        usuarioExistente.setActivo(usuarioActualizado.getActivo());
        usuarioExistente.setVersion(usuarioExistente.getVersion() + 1);

        return usuarioRepository.save(usuarioExistente);
    }

    // CAMBIAR CONTRASEÑA
    public Usuario cambiarContraseña(Long id, String contraseñaActual, String contraseñaNueva) {
        Usuario usuario = obtenerPorId(id);

        if (!passwordEncoder.matches(contraseñaActual, usuario.getContraseña())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        if (contraseñaNueva == null || contraseñaNueva.length() < 6) {
            throw new RuntimeException("La nueva contraseña debe tener al menos 6 caracteres");
        }

        usuario.setContraseña(passwordEncoder.encode(contraseñaNueva));
        usuario.setVersion(usuario.getVersion() + 1);

        return usuarioRepository.save(usuario);
    }

    // ELIMINAR USUARIO (SOFT DELETE)
    public void eliminarUsuario(Long id) {
        Usuario usuario = obtenerPorId(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    // ELIMINAR USUARIO (HARD DELETE)
    public void eliminarUsuarioPermanente(Long id) {
        usuarioRepository.deleteById(id);
    }
}