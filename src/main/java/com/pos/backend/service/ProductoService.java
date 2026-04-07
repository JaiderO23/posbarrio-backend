package com.pos.backend.service;

import com.pos.backend.model.Producto;
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


    // CREAR PRODUCTO

    public Producto crearProducto(Producto producto) {
        // Verificar que no exista el código de barras
        if (productoRepository.existsByCodigoBarras(producto.getCodigoBarras())) {
            throw new RuntimeException("Ya existe un producto con el código de barras: " + producto.getCodigoBarras());
        }

        // Generar UUID si no tiene
        if (producto.getUuid() == null) {
            producto.setUuid(UUID.randomUUID());
        }

        return productoRepository.save(producto);
    }


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
        // Obtiene todos los productos activos donde stockActual <= stockMinimo
        return productoRepository.findAll().stream()
                .filter(p -> p.getActivo() && p.getStockActual() <= p.getStockMinimo())
                .toList();
    }


    // ACTUALIZAR PRODUCTO

    public Producto actualizarProducto(Long id, Producto productoActualizado) {
        Producto productoExistente = obtenerPorId(id);

        // Verificar código de barras único si cambió
        if (!productoExistente.getCodigoBarras().equals(productoActualizado.getCodigoBarras())) {
            if (productoRepository.existsByCodigoBarras(productoActualizado.getCodigoBarras())) {
                throw new RuntimeException("Ya existe un producto con el código de barras: " + productoActualizado.getCodigoBarras());
            }
        }

        // Actualizar campos
        productoExistente.setCodigoBarras(productoActualizado.getCodigoBarras());
        productoExistente.setNombre(productoActualizado.getNombre());
        productoExistente.setDescripcion(productoActualizado.getDescripcion());
        productoExistente.setPrecioCompra(productoActualizado.getPrecioCompra());
        productoExistente.setPrecioVenta(productoActualizado.getPrecioVenta());
        productoExistente.setStockActual(productoActualizado.getStockActual());
        productoExistente.setStockMinimo(productoActualizado.getStockMinimo());
        productoExistente.setActivo(productoActualizado.getActivo());

        // Incrementar versión para sincronización
        productoExistente.setVersion(productoExistente.getVersion() + 1);

        return productoRepository.save(productoExistente);
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
