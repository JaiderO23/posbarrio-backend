package com.pos.backend.service;

import com.pos.backend.model.Abono;
import com.pos.backend.model.Cliente;
import com.pos.backend.model.Usuario;
import com.pos.backend.model.Venta;
import com.pos.backend.repository.AbonoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AbonoService {

    private final AbonoRepository abonoRepository;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;
    private final VentaService ventaService;

    /**
     * Registrar un nuevo abono
     * - Valida que el cliente existe
     * - Valida que el monto no exceda la deuda
     * - Valida que la venta existe (si se especifica)
     * - Reduce la deuda del cliente
     */
    public Abono registrarAbono(Abono abono) {
        // 1. Validar que el cliente existe
        if (abono.getCliente() == null || abono.getCliente().getId() == null) {
            throw new RuntimeException("El cliente es obligatorio");
        }
        Cliente cliente = clienteService.obtenerPorId(abono.getCliente().getId());
        abono.setCliente(cliente);

        // 2. Validar que el usuario (cajero) existe
        if (abono.getUsuario() == null || abono.getUsuario().getId() == null) {
            throw new RuntimeException("El usuario (cajero) es obligatorio");
        }
        Usuario usuario = usuarioService.obtenerPorId(abono.getUsuario().getId());
        abono.setUsuario(usuario);

        // 3. Validar que el monto es positivo
        if (abono.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El monto del abono debe ser mayor a cero");
        }

        // 4. Validar que el monto no exceda la deuda del cliente
        if (abono.getMonto().compareTo(cliente.getSaldoDeuda()) > 0) {
            throw new RuntimeException(
                    String.format("El monto del abono ($%s) no puede ser mayor a la deuda del cliente ($%s)",
                            abono.getMonto(), cliente.getSaldoDeuda())
            );
        }

        // 5. Si se especificó una venta, validar que existe y pertenece al cliente
        if (abono.getVenta() != null && abono.getVenta().getId() != null) {
            Venta venta = ventaService.obtenerPorId(abono.getVenta().getId());

            // Validar que la venta pertenece al cliente
            if (venta.getCliente() == null || !venta.getCliente().getId().equals(cliente.getId())) {
                throw new RuntimeException("La venta no pertenece al cliente especificado");
            }

            // Validar que la venta es a crédito
            if (venta.getTipoVenta() != com.pos.backend.enums.TipoVenta.CREDITO) {
                throw new RuntimeException("Solo se pueden registrar abonos a ventas a crédito");
            }

            // Validar que la venta no está completamente pagada
            BigDecimal totalPagado = abonoRepository.sumarAbonosVenta(venta.getId());
            BigDecimal pendiente = venta.getTotal().subtract(totalPagado);

            if (abono.getMonto().compareTo(pendiente) > 0) {
                throw new RuntimeException(
                        String.format("El abono ($%s) excede el saldo pendiente de la venta ($%s)",
                                abono.getMonto(), pendiente)
                );
            }

            abono.setVenta(venta);
        }

        // 6. Establecer fecha si no viene
        if (abono.getFecha() == null) {
            abono.setFecha(LocalDateTime.now());
        }

        // 7. Generar UUID si no tiene
        if (abono.getUuid() == null) {
            abono.setUuid(UUID.randomUUID());
        }

        // 8. Guardar el abono
        Abono abonoGuardado = abonoRepository.save(abono);

        // 9. Reducir la deuda del cliente
        clienteService.registrarAbono(cliente.getId(), abono.getMonto());

        return abonoGuardado;
    }


    public List<Abono> obtenerTodos() {
        return abonoRepository.findAll();
    }


    public Abono obtenerPorId(Long id) {
        return abonoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abono no encontrado con ID: " + id));
    }


    public Abono obtenerPorUuid(UUID uuid) {
        return abonoRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Abono no encontrado con UUID: " + uuid));
    }


    public List<Abono> obtenerPorCliente(Long clienteId) {
        return abonoRepository.findByClienteIdOrderByFechaDesc(clienteId);
    }

    public List<Abono> obtenerPorVenta(Long ventaId) {
        return abonoRepository.findByVentaId(ventaId);
    }

    public List<Abono> obtenerPorUsuario(Long usuarioId) {
        return abonoRepository.findByUsuarioId(usuarioId);
    }

    public List<Abono> obtenerAbonosHoy() {
        return abonoRepository.findAbonosDelDia();
    }


    public List<Abono> obtenerPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return abonoRepository.findByFechaBetween(inicio, fin);
    }


    public BigDecimal totalAbonosCliente(Long clienteId) {
        return abonoRepository.sumarAbonosCliente(clienteId);
    }


    public BigDecimal totalAbonosVenta(Long ventaId) {
        return abonoRepository.sumarAbonosVenta(ventaId);
    }


    public BigDecimal saldoPendienteVenta(Long ventaId) {
        Venta venta = ventaService.obtenerPorId(ventaId);
        BigDecimal totalPagado = abonoRepository.sumarAbonosVenta(ventaId);
        return venta.getTotal().subtract(totalPagado);
    }


    public Long contarAbonosDelDia() {
        return abonoRepository.contarAbonosDelDia();
    }


    public BigDecimal totalAbonosDelDia() {
        return abonoRepository.sumarAbonosDelDia();
    }


    public void cancelarAbono(Long id, String motivo) {
        Abono abono = obtenerPorId(id);

        // 1. Revertir la deuda del cliente (aumentar)
        clienteService.registrarCompraCredito(abono.getCliente().getId(), abono.getMonto());

        // 2. Agregar observación sobre la cancelación
        String observacionAnterior = abono.getObservaciones() != null ? abono.getObservaciones() : "";
        abono.setObservaciones(observacionAnterior + "\nCANCELADO: " + motivo);

        abonoRepository.save(abono);

        // 3. Eliminar el abono
        abonoRepository.delete(abono);
    }

    /**
     * Eliminar un abono
     */
    public void eliminar(Long id) {
        if (!abonoRepository.existsById(id)) {
            throw new RuntimeException("Abono no encontrado con ID: " + id);
        }
        abonoRepository.deleteById(id);
    }
}