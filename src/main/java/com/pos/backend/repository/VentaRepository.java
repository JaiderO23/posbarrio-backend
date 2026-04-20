package com.pos.backend.repository;

import com.pos.backend.enums.EstadoVenta;
import com.pos.backend.enums.TipoVenta;
import com.pos.backend.model.Cliente;
import com.pos.backend.model.Usuario;
import com.pos.backend.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Buscar por UUID
    Optional<Venta> findByUuid(UUID uuid);

    // Buscar por número de venta
    Optional<Venta> findByNumeroVenta(String numeroVenta);

    // Verificar si existe número de venta
    boolean existsByNumeroVenta(String numeroVenta);

    // Buscar ventas de un cliente específico
    List<Venta> findByCliente(Cliente cliente);

    // Buscar ventas por cliente ID
    List<Venta> findByClienteId(Long clienteId);

    // Buscar ventas de un usuario (cajero)
    List<Venta> findByUsuario(Usuario usuario);

    // Buscar ventas por usuario ID
    List<Venta> findByUsuarioId(Long usuarioId);

    // Buscar ventas por estado
    List<Venta> findByEstado(EstadoVenta estado);

    // Buscar ventas por tipo (CONTADO o CREDITO)
    List<Venta> findByTipoVenta(TipoVenta tipoVenta);

    // Buscar ventas en un rango de fechas
    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    // Buscar ventas del día actual - NATIVE QUERY
    @Query(value = "SELECT * FROM ventas v WHERE DATE(v.fecha) = CURRENT_DATE ORDER BY v.fecha DESC", nativeQuery = true)
    List<Venta> findVentasDelDia();

    // Buscar ventas de un mes específico - NATIVE QUERY
    @Query(value = "SELECT * FROM ventas v WHERE EXTRACT(YEAR FROM v.fecha) = :año AND EXTRACT(MONTH FROM v.fecha) = :mes ORDER BY v.fecha DESC", nativeQuery = true)
    List<Venta> findVentasPorMes(@Param("año") int año, @Param("mes") int mes);

    // Buscar ventas de un cliente en un rango de fechas
    @Query("SELECT v FROM Venta v WHERE v.cliente.id = :clienteId AND v.fecha BETWEEN :inicio AND :fin ORDER BY v.fecha DESC")
    List<Venta> findVentasClientePorFecha(@Param("clienteId") Long clienteId,
                                          @Param("inicio") LocalDateTime inicio,
                                          @Param("fin") LocalDateTime fin);

    // Obtener el último número de venta
    @Query("SELECT v FROM Venta v ORDER BY v.id DESC LIMIT 1")
    Optional<Venta> findUltimaVenta();

    // Contar ventas del día - NATIVE QUERY
    @Query(value = "SELECT COUNT(*) FROM ventas v WHERE DATE(v.fecha) = CURRENT_DATE", nativeQuery = true)
    Long contarVentasDelDia();

    // Sumar total de ventas del día - NATIVE QUERY
    @Query(value = "SELECT COALESCE(SUM(v.total), 0) FROM ventas v WHERE DATE(v.fecha) = CURRENT_DATE AND v.estado = 'COMPLETADA'", nativeQuery = true)
    java.math.BigDecimal sumarVentasDelDia();

    // Ventas por día (últimos N días)
    @Query(value = """
    SELECT 
        TO_CHAR(fecha, 'YYYY-MM-DD') as periodo,
        COUNT(*) as cantidad,
        COALESCE(SUM(total), 0) as total
    FROM ventas
    WHERE estado = 'COMPLETADA'
    AND fecha >= (CURRENT_DATE - :dias)
    GROUP BY TO_CHAR(fecha, 'YYYY-MM-DD')
    ORDER BY periodo DESC
    """, nativeQuery = true)
    List<Object[]> findVentasPorDia(@Param("dias") int dias);

    // Ventas por mes (últimos N meses)
    @Query(value = """
    SELECT 
        TO_CHAR(fecha, 'YYYY-MM') as periodo,
        COUNT(*) as cantidad,
        COALESCE(SUM(total), 0) as total
    FROM ventas
    WHERE estado = 'COMPLETADA'
    AND fecha >= (CURRENT_DATE - (:meses * 30))
    GROUP BY TO_CHAR(fecha, 'YYYY-MM')
    ORDER BY periodo DESC
    """, nativeQuery = true)
    List<Object[]> findVentasPorMes(@Param("meses") int meses);

    // Ventas por método de pago
    @Query(value = """
        SELECT 
            metodo_pago,
            COUNT(*) as cantidad,
            COALESCE(SUM(total), 0) as total
        FROM ventas
        WHERE estado = 'COMPLETADA'
        GROUP BY metodo_pago
        ORDER BY total DESC
        """, nativeQuery = true)
    List<Object[]> findVentasPorMetodoPago();

    // Ventas en un rango (para resumen)
    @Query(value = """
        SELECT 
            COUNT(*) as cantidad,
            COALESCE(SUM(total), 0) as total
        FROM ventas
        WHERE estado = 'COMPLETADA'
        AND fecha BETWEEN :inicio AND :fin
        """, nativeQuery = true)
    List<Object[]> findVentasEnRango(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Ventas por usuario
    @Query(value = """
        SELECT 
            u.id,
            u.nombre_completo,
            COUNT(v.id) as cantidad_ventas,
            COALESCE(SUM(v.total), 0) as total_vendido
        FROM usuarios u
        LEFT JOIN ventas v ON u.id = v.usuario_id 
            AND v.estado = 'COMPLETADA'
            AND v.fecha BETWEEN :inicio AND :fin
        GROUP BY u.id, u.nombre_completo
        ORDER BY total_vendido DESC
        """, nativeQuery = true)
    List<Object> findVentasPorUsuario(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query(value = "SELECT * FROM ventas WHERE sincronizado_desde IS NULL OR sincronizado_desde != 'SINCRONIZADO'", nativeQuery = true)
    List<Venta> findPendientesSincronizacion();

    @Query(value = """
    SELECT 
        metodo_pago,
        COUNT(*) as cantidad,
        COALESCE(SUM(total), 0) as total
    FROM ventas
    WHERE estado = 'COMPLETADA'
    AND fecha BETWEEN :inicio AND :fin
    GROUP BY metodo_pago
    ORDER BY total DESC
    """, nativeQuery = true)
    List<Object[]> findVentasPorMetodoPagoEnRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}