package com.pos.backend.service;

import com.pos.backend.dto.ProductoRequest;
import com.pos.backend.dto.request.*;
import com.pos.backend.model.Categoria;
import com.pos.backend.model.Producto;
import com.pos.backend.repository.CategoriaRepository;
import com.pos.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    // CREAR PRODUCTO
    public Producto crearProducto(ProductoRequest request) {
        // Verificar que no exista el código de barras
        if (productoRepository.existsByCodigoBarras(request.getCodigoBarras())) {
            throw new RuntimeException("Ya existe un producto con el código de barras: " + request.getCodigoBarras());
        }

        // Buscar la categoría
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + request.getCategoriaId()));

        // Crear el producto
        Producto producto = new Producto();
        producto.setUuid(UUID.randomUUID());
        producto.setCodigoBarras(request.getCodigoBarras());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setCategoria(categoria);
        producto.setPrecioCompra(request.getPrecioCompra());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setStockActual(request.getStockActual());
        producto.setStockMinimo(request.getStockMinimo());
        producto.setActivo(request.getActivo());

        return productoRepository.save(producto);
    }

    // ACTUALIZAR PRODUCTO
    public Producto actualizarProducto(Long id, ProductoRequest request) {
        Producto productoExistente = obtenerPorId(id);

        // Verificar código de barras único si cambió
        if (!productoExistente.getCodigoBarras().equals(request.getCodigoBarras())) {
            if (productoRepository.existsByCodigoBarras(request.getCodigoBarras())) {
                throw new RuntimeException("Ya existe un producto con el código de barras: " + request.getCodigoBarras());
            }
        }

        // Buscar la categoría si cambió
        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + request.getCategoriaId()));
            productoExistente.setCategoria(categoria);
        }

        // Actualizar campos
        productoExistente.setCodigoBarras(request.getCodigoBarras());
        productoExistente.setNombre(request.getNombre());
        productoExistente.setDescripcion(request.getDescripcion());
        productoExistente.setPrecioCompra(request.getPrecioCompra());
        productoExistente.setPrecioVenta(request.getPrecioVenta());
        productoExistente.setStockActual(request.getStockActual());
        productoExistente.setStockMinimo(request.getStockMinimo());
        productoExistente.setActivo(request.getActivo());

        // Incrementar versión para sincronización
        productoExistente.setVersion(productoExistente.getVersion() + 1);

        return productoRepository.save(productoExistente);
    }

    // ... resto de métodos sin cambios ...

    // OBTENER TODOS LOS PRODUCTOS
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    // OBTENER PRODUCTOS ACTIVOS
    public List<Producto> obtenerActivos() {
        return productoRepository.findByActivoTrue();
    }

    // OBTENER PRODUCTO POR ID
    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    // OBTENER PRODUCTO POR UUID
    public Producto obtenerPorUuid(UUID uuid) {
        return productoRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con UUID: " + uuid));
    }

    // OBTENER PRODUCTO POR CÓDIGO DE BARRAS
    public Producto obtenerPorCodigoBarras(String codigoBarras) {
        return productoRepository.findByCodigoBarras(codigoBarras)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con código: " + codigoBarras));
    }

    // BUSCAR PRODUCTOS POR NOMBRE
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // OBTENER PRODUCTOS CON STOCK BAJO
    public List<Producto> obtenerStockBajo() {
        return productoRepository.findAll().stream()
                .filter(p -> p.getActivo() && p.getStockActual() <= p.getStockMinimo())
                .toList();
    }

    // ELIMINAR PRODUCTO (SOFT DELETE)
    public void eliminarProducto(Long id) {
        Producto producto = obtenerPorId(id);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    // ELIMINAR PRODUCTO (HARD DELETE)
    public void eliminarProductoPermanente(Long id) {
        productoRepository.deleteById(id);
    }

    // ACTUALIZAR STOCK
    public Producto actualizarStock(Long id, Integer cantidad) {
        Producto producto = obtenerPorId(id);
        producto.setStockActual(producto.getStockActual() + cantidad);
        producto.setVersion(producto.getVersion() + 1);
        return productoRepository.save(producto);
    }
}