package com.pos.backend.dto.request;

import com.pos.backend.enums.MetodoPago;
import com.pos.backend.enums.TipoVenta;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaRequest {

    private Long clienteId;  // Opcional (para cliente ocasional)

    @NotNull(message = "El usuario (cajero) es obligatorio")
    private Long usuarioId;

    @NotNull(message = "El tipo de venta es obligatorio")
    private TipoVenta tipoVenta;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    private BigDecimal descuento = BigDecimal.ZERO;

    private String observaciones;

    @NotEmpty(message = "La venta debe tener al menos un producto")
    private List<DetalleVentaRequest> detalles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleVentaRequest {

        @NotNull(message = "El producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La cantidad es obligatoria")
        private Integer cantidad;

        @NotNull(message = "El precio unitario es obligatorio")
        private BigDecimal precioUnitario;
    }
}