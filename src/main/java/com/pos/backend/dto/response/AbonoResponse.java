package com.pos.backend.dto.response;

import com.pos.backend.enums.MetodoPago;
import com.pos.backend.model.Abono;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbonoResponse {

    private Long id;
    private UUID uuid;
    private BigDecimal monto;
    private MetodoPago metodoPago;
    private LocalDateTime fecha;
    private String observaciones;

    // Datos del cliente
    private Long clienteId;
    private String clienteNombre;

    // Datos de la venta (puede ser null si es abono general)
    private Long ventaId;
    private String ventaNumero;

    // Datos del usuario/cajero
    private Long usuarioId;
    private String usuarioNombre;

    // Conversor estático: entidad -> DTO
    public static AbonoResponse fromEntity(Abono abono) {
        AbonoResponse dto = new AbonoResponse();
        dto.setId(abono.getId());
        dto.setUuid(abono.getUuid());
        dto.setMonto(abono.getMonto());
        dto.setMetodoPago(abono.getMetodoPago());
        dto.setFecha(abono.getFecha());
        dto.setObservaciones(abono.getObservaciones());

        // Cliente (obligatorio según el modelo)
        if (abono.getCliente() != null) {
            dto.setClienteId(abono.getCliente().getId());
            dto.setClienteNombre(abono.getCliente().getNombre() + " " +
                    (abono.getCliente().getApellido() != null ? abono.getCliente().getApellido() : ""));
        }

        // Venta (opcional)
        if (abono.getVenta() != null) {
            dto.setVentaId(abono.getVenta().getId());
            dto.setVentaNumero(abono.getVenta().getNumeroVenta());
        }

        // Usuario
        if (abono.getUsuario() != null) {
            dto.setUsuarioId(abono.getUsuario().getId());
            dto.setUsuarioNombre(abono.getUsuario().getNombreCompleto());
        }

        return dto;
    }
}