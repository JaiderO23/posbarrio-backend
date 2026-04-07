package com.pos.backend.service;

import com.pos.backend.model.Categoria;
import com.pos.backend.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;


    // CREAR CATEGORÍA
    public Categoria crearCategoria(Categoria categoria) {
        // Validar que no exista el nombre (ignorando mayúsculas/minúsculas)
        if (categoriaRepository.existsByNombreIgnoreCase(categoria.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con el nombre: " +
                    categoria.getNombre());
        }

        // Generar UUID si no tiene
        if (categoria.getUuid() == null) {
            categoria.setUuid(UUID.randomUUID());
        }

        return categoriaRepository.save(categoria);
    }

    // OBTENER TODAS LAS CATEGORÍAS
    public List<Categoria> obtenerTodas() {
        return categoriaRepository.findAll();
    }


    // OBTENER CATEGORÍAS ACTIVAS
    public List<Categoria> obtenerActivas() {
        return categoriaRepository.findByActivoTrue();
    }


    // OBTENER CATEGORÍA POR ID
    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
    }


    // OBTENER CATEGORÍA POR UUID
    public Categoria obtenerPorUuid(UUID uuid) {
        return categoriaRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con UUID: " + uuid));
    }


    // OBTENER CATEGORÍA POR NOMBRE
    public Categoria obtenerPorNombre(String nombre) {
        return categoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + nombre));
    }

    // BUSCAR CATEGORÍAS POR NOMBRE (PARCIAL)
    public List<Categoria> buscarPorNombre(String texto) {
        return categoriaRepository.findByNombreContainingIgnoreCase(texto);
    }


    // ACTUALIZAR CATEGORÍA
    public Categoria actualizarCategoria(Long id, Categoria categoriaActualizada) {
        Categoria categoriaExistente = obtenerPorId(id);

        // Validar nombre único (si cambió)
        if (!categoriaExistente.getNombre().equalsIgnoreCase(categoriaActualizada.getNombre())) {
            if (categoriaRepository.existsByNombreIgnoreCase(categoriaActualizada.getNombre())) {
                throw new RuntimeException("Ya existe una categoría con el nombre: " +
                        categoriaActualizada.getNombre());
            }
        }

        // Actualizar campos
        categoriaExistente.setNombre(categoriaActualizada.getNombre());
        categoriaExistente.setDescripcion(categoriaActualizada.getDescripcion());
        categoriaExistente.setActivo(categoriaActualizada.getActivo());

        // Incrementar versión
        categoriaExistente.setVersion(categoriaExistente.getVersion() + 1);

        return categoriaRepository.save(categoriaExistente);
    }


    // ELIMINAR CATEGORÍA (SOFT DELETE)
    public void eliminarCategoria(Long id) {
        Categoria categoria = obtenerPorId(id);
        categoria.setActivo(false);
        categoriaRepository.save(categoria);
    }


    // ELIMINAR CATEGORÍA (HARD DELETE)
    public void eliminarCategoriaPermanente(Long id) {
        categoriaRepository.deleteById(id);
    }
}