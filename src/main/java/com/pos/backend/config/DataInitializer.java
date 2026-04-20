package com.pos.backend.config;

import com.pos.backend.enums.Rol;
import com.pos.backend.model.Usuario;
import com.pos.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.propietario.password}")
    private String propietarioPassword;

    @Value("${app.cajero.password}")
    private String cajeroPassword;

    @Override
    public void run(String... args) {
        try {
            crearUsuarioSiNoExiste("admin", adminPassword, "Administrador", Rol.ADMIN);
            crearUsuarioSiNoExiste("propietario", propietarioPassword, "Propietario", Rol.PROPIETARIO);
            crearUsuarioSiNoExiste("cajero", cajeroPassword, "Cajero", Rol.CAJERO);
        } catch (Exception e) {
            System.err.println("ERROR en DataInitializer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void crearUsuarioSiNoExiste(String nombreUsuario, String contraseña,
                                        String nombreCompleto, Rol rol) {
        if (!usuarioRepository.existsByNombreUsuario(nombreUsuario)) {
            Usuario usuario = new Usuario();
            usuario.setUuid(UUID.randomUUID());
            usuario.setNombreUsuario(nombreUsuario);
            usuario.setContraseña(passwordEncoder.encode(contraseña));
            usuario.setNombreCompleto(nombreCompleto);
            usuario.setRol(rol);
            usuario.setActivo(true);
            usuario.setVersion(1);
            usuarioRepository.save(usuario);
            System.out.println("Usuario creado: " + nombreUsuario + " [" + rol.name() + "]");
        }
    }
}