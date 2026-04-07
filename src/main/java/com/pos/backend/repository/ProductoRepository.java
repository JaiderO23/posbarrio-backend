package com.pos.backend.repository;

import com.pos.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByUuid(UUID uuid);
    Optional<Producto> findByCodigoBarras(String codigoBarras);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByActivoTrue();
    List<Producto> findByStockActualLessThanEqualAndActivoTrue(Integer stockMinimo);
    boolean existsByCodigoBarras(String codigoBarras);

    // Productos con stock bajo (stock actual <= stock mínimo)
    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.stockMinimo ORDER BY p.stockActual ASC")
    List<Producto> findProductosStockBajo();

    // Contar productos con stock bajo
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.stockActual <= p.stockMinimo")
    Integer contarProductosStockBajo();
}