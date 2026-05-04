package com.pos.backend.repository;

import com.pos.backend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Buscar por UUID
    Optional<Cliente> findByUuid(UUID uuid);

    // Para sincronización masiva
    List<Cliente> findAllByUuidIn(Collection<UUID> uuids);

    // Buscar por número de documento
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);

    // Buscar por email
    Optional<Cliente> findByEmail(String email);

    // Buscar por nombre (búsqueda parcial)
    List<Cliente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(
            String nombre, String apellido
    );

    // Buscar clientes activos
    List<Cliente> findByActivoTrue();

    // Buscar clientes con deuda
    List<Cliente> findBySaldoDeudaGreaterThan(BigDecimal monto);

    // Buscar clientes con crédito habilitado
    List<Cliente> findByLimiteCreditoGreaterThan(BigDecimal monto);

    // Verificar si existe por documento
    boolean existsByNumeroDocumento(String numeroDocumento);

    // Verificar si existe por email
    boolean existsByEmail(String email);

    // Clientes con deuda ordenados de mayor a menor
    @Query("SELECT c FROM Cliente c WHERE c.saldoDeuda > 0 ORDER BY c.saldoDeuda DESC")
    List<Cliente> findClientesConDeuda();

    // Contar clientes con deuda
    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.saldoDeuda > 0")
    Integer contarClientesConDeuda();

    // Sumar total de deudas
    @Query("SELECT COALESCE(SUM(c.saldoDeuda), 0) FROM Cliente c WHERE c.saldoDeuda > 0")
    BigDecimal sumarTotalDeudas();
}