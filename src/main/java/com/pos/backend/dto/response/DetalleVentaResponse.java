package com.pos.backend.dto.response;

import com.pos.backend.model.DetalleVenta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaResponse {

    private Long id;
    private UUID uuid;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    // Datos del producto
    private Long productoId;
    private String productoNombre;
    private String productoCodigoBarras;

    public static DetalleVentaResponse fromEntity(DetalleVenta detalle) {
        DetalleVentaResponse dto = new DetalleVentaResponse();
        dto.setId(detalle.getId());
        dto.setUuid(detalle.getUuid());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotal(detalle.getSubtotal());

        if (detalle.getProducto() != null) {
            dto.setProductoId(detalle.getProducto().getId());
            dto.setProductoNombre(detalle.getProducto().getNombre());
            dto.setProductoCodigoBarras(detalle.getProducto().getCodigoBarras());
        }

        return dto;
    }
}