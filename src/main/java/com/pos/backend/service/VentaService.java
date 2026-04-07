package com.pos.backend.service;

import com.pos.backend.enums.EstadoVenta;
import com.pos.backend.enums.TipoVenta;
import com.pos.backend.model.Cliente;
import com.pos.backend.model.DetalleVenta;
import com.pos.backend.model.Usuario;
import com.pos.backend.model.Venta;
import com.pos.backend.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;


    // CREAR VENTA COMPLETA
    public Venta crearVenta(Venta venta) {
        // 1. Validar que tenga detalles
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            throw new RuntimeException("La venta debe tener al menos un producto");
        }

        // 2. Generar número de venta único
        if (venta.getNumeroVenta() == null || venta.getNumeroVenta().isEmpty()) {
            venta.setNumeroVenta(generarNumeroVenta());
        }

        // 3. Validar y obtener usuario (cajero)
        if (venta.getUsuario() == null || venta.getUsuario().getId() == null) {
            throw new RuntimeException("Debe especificar el usuario (cajero) que registra la venta");
        }
        Usuario usuario = usuarioService.obtenerPorId(venta.getUsuario().getId());
        venta.setUsuario(usuario);

        // 4. Validar y obtener cliente (si existe)
        if (venta.getCliente() != null && venta.getCliente().getId() != null) {
            Cliente cliente = clienteService.obtenerPorId(venta.getCliente().getId());
            venta.setCliente(cliente);

            // Si es venta a crédito, validar que el cliente tenga crédito disponible
            if (venta.getTipoVenta() == TipoVenta.CREDITO) {
                if (!cliente.esClienteCredito()) {
                    throw new RuntimeException("El cliente no tiene crédito habilitado");
                }
            }
        } else {
            // Venta sin cliente registrado (cliente ocasional)
            if (venta.getTipoVenta() == TipoVenta.CREDITO) {
                throw new RuntimeException("Las ventas a crédito requieren un cliente registrado");
            }
        }

        // 5. Establecer relación bidireccional con detalles Y calcular subtotales
        for (DetalleVenta detalle : venta.getDetalles()) {
            detalle.setVenta(venta);

            // Calcular subtotal manualmente (antes de @PrePersist)
            if (detalle.getSubtotal() == null) {
                BigDecimal subtotal = detalle.getPrecioUnitario()
                        .multiply(new BigDecimal(detalle.getCantidad()));
                detalle.setSubtotal(subtotal);
            }
        }

        // 6. Calcular totales
        venta.setSubtotal(venta.calcularSubtotal());
        venta.setTotal(venta.calcularTotal());

        // 7. Validar crédito disponible si es venta a crédito
        if (venta.getTipoVenta() == TipoVenta.CREDITO && venta.getCliente() != null) {
            Cliente cliente = venta.getCliente();
            if (!cliente.puedeComprarACredito(venta.getTotal())) {
                throw new RuntimeException(
                        String.format("Crédito insuficiente. Disponible: $%s, Solicitado: $%s",
                                cliente.getCreditoDisponible(), venta.getTotal())
                );
            }
        }

        // 8. Establecer fecha si no viene
        if (venta.getFecha() == null) {
            venta.setFecha(LocalDateTime.now());
        }

        // 9. Generar UUID si no tiene
        if (venta.getUuid() == null) {
            venta.setUuid(UUID.randomUUID());
        }

        // 10. Guardar venta (cascade guarda los detalles automáticamente)
        Venta ventaGuardada = ventaRepository.save(venta);

        // 11. Si es venta a crédito, aumentar deuda del cliente
        if (venta.getTipoVenta() == TipoVenta.CREDITO && venta.getCliente() != null) {
            clienteService.registrarCompraCredito(venta.getCliente().getId(), venta.getTotal());
        }

        return ventaGuardada;
    }

    // GENERAR NÚMERO DE VENTA ÚNICO
    private String generarNumeroVenta() {
        // Obtener última venta
        Venta ultimaVenta = ventaRepository.findUltimaVenta().orElse(null);

        long siguienteNumero = 1;

        if (ultimaVenta != null && ultimaVenta.getNumeroVenta() != null) {
            // Extraer número de "CV-000123" -> 123
            String numeroStr = ultimaVenta.getNumeroVenta().replace("CV-", "");
            try {
                siguienteNumero = Long.parseLong(numeroStr) + 1;
            } catch (NumberFormatException e) {
                // Si no se puede parsear, empezar desde 1
                siguienteNumero = 1;
            }
        }

        // Formatear como "CV-000001"
        return String.format("CV-%06d", siguienteNumero);
    }


    // OBTENER TODAS LAS VENTAS
    public List<Venta> obtenerTodas() {
        return ventaRepository.findAll();
    }


    // OBTENER VENTA POR ID (CON DETALLES)
    public Venta obtenerPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
    }

    // OBTENER VENTA POR UUID
    public Venta obtenerPorUuid(UUID uuid) {
        return ventaRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con UUID: " + uuid));
    }

    // OBTENER VENTA POR NÚMERO
    public Venta obtenerPorNumeroVenta(String numeroVenta) {
        return ventaRepository.findByNumeroVenta(numeroVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + numeroVenta));
    }

    // OBTENER VENTAS DE UN CLIENTE
    public List<Venta> obtenerPorCliente(Long clienteId) {
        return ventaRepository.findByClienteId(clienteId);
    }

    // OBTENER VENTAS DE UN USUARIO (CAJERO)
    public List<Venta> obtenerPorUsuario(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }


    // OBTENER VENTAS POR TIPO (CONTADO O CREDITO)
    public List<Venta> obtenerPorTipo(TipoVenta tipoVenta) {
        return ventaRepository.findByTipoVenta(tipoVenta);
    }

    // OBTENER VENTAS POR ESTADO
    public List<Venta> obtenerPorEstado(EstadoVenta estado) {
        return ventaRepository.findByEstado(estado);
    }

    // OBTENER VENTAS DEL DÍA
    public List<Venta> obtenerVentasDelDia() {
        return ventaRepository.findVentasDelDia();
    }

    // OBTENER VENTAS EN RANGO DE FECHAS
    public List<Venta> obtenerPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaBetween(inicio, fin);
    }

    // OBTENER VENTAS DE UN MES
    public List<Venta> obtenerVentasPorMes(int año, int mes) {
        return ventaRepository.findVentasPorMes(año, mes);
    }


    // OBTENER VENTAS DE HOY (ALTERNATIVA)
    public List<Venta> obtenerVentasHoy() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = LocalDate.now().atTime(LocalTime.MAX);
        return ventaRepository.findByFechaBetween(inicioDia, finDia);
    }


    // CAMBIAR ESTADO DE VENTA
    public Venta cambiarEstado(Long id, EstadoVenta nuevoEstado) {
        Venta venta = obtenerPorId(id);

        EstadoVenta estadoAnterior = venta.getEstado();
        venta.setEstado(nuevoEstado);

        // Si se cancela una venta a crédito que estaba completada,
        // revertir la deuda del cliente
        if (estadoAnterior == EstadoVenta.COMPLETADA &&
                nuevoEstado == EstadoVenta.CANCELADA &&
                venta.getTipoVenta() == TipoVenta.CREDITO &&
                venta.getCliente() != null) {

            clienteService.registrarAbono(venta.getCliente().getId(), venta.getTotal());
        }

        venta.setVersion(venta.getVersion() + 1);
        return ventaRepository.save(venta);
    }


    // CANCELAR VENTA
    public Venta cancelarVenta(Long id, String motivo) {
        Venta venta = obtenerPorId(id);

        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new RuntimeException("La venta ya está cancelada");
        }

        // Si era venta a crédito completada, revertir deuda
        if (venta.getEstado() == EstadoVenta.COMPLETADA &&
                venta.getTipoVenta() == TipoVenta.CREDITO &&
                venta.getCliente() != null) {

            clienteService.registrarAbono(venta.getCliente().getId(), venta.getTotal());
        }

        venta.setEstado(EstadoVenta.CANCELADA);
        venta.setObservaciones(venta.getObservaciones() + "\nCANCELADA: " + motivo);
        venta.setVersion(venta.getVersion() + 1);

        return ventaRepository.save(venta);
    }


    // ELIMINAR VENTA (HARD DELETE)
    public void eliminarVenta(Long id) {
        Venta venta = obtenerPorId(id);

        // Solo permitir eliminar ventas canceladas
        if (venta.getEstado() != EstadoVenta.CANCELADA) {
            throw new RuntimeException("Solo se pueden eliminar ventas canceladas");
        }

        ventaRepository.deleteById(id);
    }


    // ESTADÍSTICAS - CONTAR VENTAS DEL DÍA
    public Long contarVentasDelDia() {
        return ventaRepository.contarVentasDelDia();
    }


    // ESTADÍSTICAS - TOTAL VENDIDO HOY
    public BigDecimal totalVentasDelDia() {
        return ventaRepository.sumarVentasDelDia();
    }
}