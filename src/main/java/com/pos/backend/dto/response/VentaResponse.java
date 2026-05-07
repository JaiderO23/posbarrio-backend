package com.pos.backend.dto.response;

import com.pos.backend.enums.EstadoVenta;
import com.pos.backend.enums.MetodoPago;
import com.pos.backend.enums.TipoVenta;
import com.pos.backend.model.Venta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaResponse {

    private Long id;
    private UUID uuid;
    private String numeroVenta;
    private LocalDateTime fecha;
    private TipoVenta tipoVenta;
    private MetodoPago metodoPago;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal total;
    private EstadoVenta estado;
    private String observaciones;

    // Datos del cliente (sin la entidad completa)
    private Long clienteId;
    private String clienteNombre;

    // Datos del usuario/cajero
    private Long usuarioId;
    private String usuarioNombre;

    // Detalles ya resueltos
    private List<DetalleVentaResponse> detalles;

    // Conversor estático: entidad -> DTO
    public static VentaResponse fromEntity(Venta venta) {
        VentaResponse dto = new VentaResponse();
        dto.setId(venta.getId());
        dto.setUuid(venta.getUuid());
        dto.setNumeroVenta(venta.getNumeroVenta());
        dto.setFecha(venta.getFecha());
        dto.setTipoVenta(venta.getTipoVenta());
        dto.setMetodoPago(venta.getMetodoPago());
        dto.setSubtotal(venta.getSubtotal());
        dto.setDescuento(venta.getDescuento());
        dto.setTotal(venta.getTotal());
        dto.setEstado(venta.getEstado());
        dto.setObservaciones(venta.getObservaciones());

        // Cliente (puede ser null en ventas de contado sin cliente)
        if (venta.getCliente() != null) {
            dto.setClienteId(venta.getCliente().getId());
            dto.setClienteNombre(venta.getCliente().getNombre() + " " +
                    (venta.getCliente().getApellido() != null ? venta.getCliente().getApellido() : ""));
        }

        // Usuario
        if (venta.getUsuario() != null) {
            dto.setUsuarioId(venta.getUsuario().getId());
            dto.setUsuarioNombre(venta.getUsuario().getNombreCompleto());
        }

        // Detalles
        if (venta.getDetalles() != null) {
            dto.setDetalles(venta.getDetalles().stream()
                    .map(DetalleVentaResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}