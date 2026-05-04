package com.pos.backend.repository;

import com.pos.backend.model.Abono;
import com.pos.backend.model.Cliente;
import com.pos.backend.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AbonoRepository extends JpaRepository<Abono, Long> {

    // Buscar por UUID
    Optional<Abono> findByUuid(UUID uuid);

    // Buscar abonos de un cliente específico
    List<Abono> findByCliente(Cliente cliente);

    // Buscar abonos por cliente ID
    List<Abono> findByClienteId(Long clienteId);

    // Buscar abonos por cliente ID ordenados por fecha descendente
    List<Abono> findByClienteIdOrderByFechaDesc(Long clienteId);

    // Buscar abonos de una venta específica
    List<Abono> findByVenta(Venta venta);

    // Buscar abonos por venta ID
    List<Abono> findByVentaId(Long ventaId);

    // Buscar abonos de un usuario (cajero)
    List<Abono> findByUsuarioId(Long usuarioId);

    // Buscar abonos en un rango de fechas
    List<Abono> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    // Buscar abonos de un cliente en un rango de fechas
    @Query("SELECT a FROM Abono a WHERE a.cliente.id = :clienteId AND a.fecha BETWEEN :inicio AND :fin ORDER BY a.fecha DESC")
    List<Abono> findAbonosClientePorFecha(@Param("clienteId") Long clienteId,
                                          @Param("inicio") LocalDateTime inicio,
                                          @Param("fin") LocalDateTime fin);

    // Sumar total de abonos de un cliente
    @Query("SELECT COALESCE(SUM(a.monto), 0) FROM Abono a WHERE a.cliente.id = :clienteId")
    BigDecimal sumarAbonosCliente(@Param("clienteId") Long clienteId);

    // Sumar total de abonos de una venta
    @Query("SELECT COALESCE(SUM(a.monto), 0) FROM Abono a WHERE a.venta.id = :ventaId")
    BigDecimal sumarAbonosVenta(@Param("ventaId") Long ventaId);

    // Buscar abonos del día actual - NATIVE QUERY
    @Query(value = "SELECT * FROM abonos a WHERE DATE(a.fecha) = CURRENT_DATE ORDER BY a.fecha DESC", nativeQuery = true)
    List<Abono> findAbonosDelDia();

    // Contar abonos del día - NATIVE QUERY
    @Query(value = "SELECT COUNT(*) FROM abonos a WHERE DATE(a.fecha) = CURRENT_DATE", nativeQuery = true)
    Long contarAbonosDelDia();

    // Sumar total de abonos del día - NATIVE QUERY
    @Query(value = "SELECT COALESCE(SUM(a.monto), 0) FROM abonos a WHERE DATE(a.fecha) = CURRENT_DATE", nativeQuery = true)
    BigDecimal sumarAbonosDelDia();

    @Query(value = "SELECT * FROM abonos WHERE sincronizado_desde IS NULL OR sincronizado_desde != 'SINCRONIZADO'", nativeQuery = true)
    List<Abono> findPendientesSincronizacion();

}