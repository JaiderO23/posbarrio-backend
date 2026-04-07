package com.pos.backend.repository;

import com.pos.backend.model.DetalleVenta;
import com.pos.backend.model.Producto;
import com.pos.backend.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    // Buscar por UUID
    Optional<DetalleVenta> findByUuid(UUID uuid);

    // Buscar detalles de una venta específica
    List<DetalleVenta> findByVenta(Venta venta);

    // Buscar detalles por venta ID
    List<DetalleVenta> findByVentaId(Long ventaId);

    // Buscar detalles que contienen un producto específico
    List<DetalleVenta> findByProducto(Producto producto);

    // Buscar detalles por producto ID
    List<DetalleVenta> findByProductoId(Long productoId);

    @Query(value = """
        SELECT 
            p.id as producto_id,
            p.nombre as nombre_producto,
            SUM(dv.cantidad) as cantidad_vendida,
            SUM(dv.subtotal) as total_vendido
        FROM detalle_venta dv
        JOIN productos p ON dv.producto_id = p.id
        JOIN ventas v ON dv.venta_id = v.id
        WHERE v.estado = 'COMPLETADA'
        GROUP BY p.id, p.nombre
        ORDER BY cantidad_vendida DESC
        LIMIT :limite
        """, nativeQuery = true)
    List<Object[]> findProductosMasVendidos(@Param("limite") int limite);
}