package com.pos.backend.repository;

import com.pos.backend.enums.Rol;
import com.pos.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar por UUID
    Optional<Usuario> findByUuid(UUID uuid);

    // Buscar por nombre de usuario (para login)
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    // Buscar usuarios activos
    List<Usuario> findByActivoTrue();

    // Buscar por rol
    List<Usuario> findByRol(Rol rol);

    // Buscar por rol y activo
    List<Usuario> findByRolAndActivoTrue(Rol rol);

    // Verificar si existe por nombre de usuario
    boolean existsByNombreUsuario(String nombreUsuario);
}