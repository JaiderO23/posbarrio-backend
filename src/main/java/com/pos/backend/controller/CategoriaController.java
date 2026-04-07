package com.pos.backend.controller;

import com.pos.backend.model.Categoria;
import com.pos.backend.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final CategoriaService categoriaService;


    // GET - Obtener todas las categorías
    @GetMapping
    public ResponseEntity<List<Categoria>> obtenerTodas() {
        List<Categoria> categorias = categoriaService.obtenerTodas();
        return ResponseEntity.ok(categorias);
    }


    // GET - Obtener solo categorías activas
    @GetMapping("/activas")
    public ResponseEntity<List<Categoria>> obtenerActivas() {
        List<Categoria> categorias = categoriaService.obtenerActivas();
        return ResponseEntity.ok(categorias);
    }


    // GET - Obtener categoría por ID
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerPorId(@PathVariable Long id) {
        Categoria categoria = categoriaService.obtenerPorId(id);
        return ResponseEntity.ok(categoria);
    }


    // GET - Obtener categoría por UUID
    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<Categoria> obtenerPorUuid(@PathVariable UUID uuid) {
        Categoria categoria = categoriaService.obtenerPorUuid(uuid);
        return ResponseEntity.ok(categoria);
    }


    // GET - Obtener categoría por nombre
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<Categoria> obtenerPorNombre(@PathVariable String nombre) {
        Categoria categoria = categoriaService.obtenerPorNombre(nombre);
        return ResponseEntity.ok(categoria);
    }


    // GET - Buscar categorías por nombre (parcial)
    @GetMapping("/buscar")
    public ResponseEntity<List<Categoria>> buscarPorNombre(@RequestParam String texto) {
        List<Categoria> categorias = categoriaService.buscarPorNombre(texto);
        return ResponseEntity.ok(categorias);
    }


    // POST - Crear nueva categoría
    @PostMapping
    public ResponseEntity<Categoria> crearCategoria(@Valid @RequestBody Categoria categoria) {
        Categoria nuevaCategoria = categoriaService.crearCategoria(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCategoria);
    }


    // PUT - Actualizar categoría
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody Categoria categoria) {
        Categoria categoriaActualizada = categoriaService.actualizarCategoria(id, categoria);
        return ResponseEntity.ok(categoriaActualizada);
    }


    // DELETE - Eliminar categoría (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }


    // DELETE - Eliminar categoría permanentemente
    @DeleteMapping("/{id}/permanente")
    public ResponseEntity<Void> eliminarCategoriaPermanente(@PathVariable Long id) {
        categoriaService.eliminarCategoriaPermanente(id);
        return ResponseEntity.noContent().build();
    }
}