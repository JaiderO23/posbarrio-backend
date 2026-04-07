package com.pos.backend.controller;

import com.pos.backend.dto.ProductoRequest;
import com.pos.backend.model.Producto;
import com.pos.backend.service.ProductoService;
import com.pos.backend.dto.request.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        List<Producto> productos = productoService.obtenerTodos();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Producto>> obtenerActivos() {
        List<Producto> productos = productoService.obtenerActivos();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        Producto producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(producto);
    }

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<Producto> obtenerPorUuid(@PathVariable UUID uuid) {
        Producto producto = productoService.obtenerPorUuid(uuid);
        return ResponseEntity.ok(producto);
    }

    @GetMapping("/codigo/{codigoBarras}")
    public ResponseEntity<Producto> obtenerPorCodigoBarras(@PathVariable String codigoBarras) {
        Producto producto = productoService.obtenerPorCodigoBarras(codigoBarras);
        return ResponseEntity.ok(producto);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarPorNombre(@RequestParam String nombre) {
        List<Producto> productos = productoService.buscarPorNombre(nombre);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<Producto>> obtenerStockBajo() {
        List<Producto> productos = productoService.obtenerStockBajo();
        return ResponseEntity.ok(productos);
    }

    @PostMapping
    public ResponseEntity<Producto> crearProducto(@Valid @RequestBody ProductoRequest request) {
        Producto nuevoProducto = productoService.crearProducto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        Producto productoActualizado = productoService.actualizarProducto(id, request);
        return ResponseEntity.ok(productoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permanente")
    public ResponseEntity<Void> eliminarProductoPermanente(@PathVariable Long id) {
        productoService.eliminarProductoPermanente(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Producto> actualizarStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        Producto producto = productoService.actualizarStock(id, cantidad);
        return ResponseEntity.ok(producto);
    }
}
