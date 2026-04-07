package com.pos.backend.dto.request;

import com.pos.backend.enums.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbonoRequest {

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    private Long ventaId;  // Opcional - puede ser abono general

    @NotNull(message = "El usuario (cajero) es obligatorio")
    private Long usuarioId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    private String observaciones;
}