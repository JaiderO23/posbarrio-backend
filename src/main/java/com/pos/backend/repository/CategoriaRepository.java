package com.pos.backend.repository;

import com.pos.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // Buscar por UUID
    Optional<Categoria> findByUuid(UUID uuid);

    // Buscar por nombre
    Optional<Categoria> findByNombre(String nombre);

    // Buscar por nombre (ignorando mayúsculas/minúsculas)
    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    // Buscar categorías activas
    List<Categoria> findByActivoTrue();

    // Verificar si existe por nombre
    boolean existsByNombre(String nombre);

    // Verificar si existe por nombre (ignorando mayúsculas/minúsculas)
    boolean existsByNombreIgnoreCase(String nombre);

    // Buscar por parte del nombre
    List<Categoria> findByNombreContainingIgnoreCase(String nombre);
}