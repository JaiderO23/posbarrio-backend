package com.pos.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDeudaDTO {

    private Long clienteId;
    private String nombreCompleto;
    private String numeroDocumento;
    private String telefono;
    private BigDecimal saldoDeuda;
    private BigDecimal limiteCredito;
    private BigDecimal creditoDisponible;
    private Integer diasDeuda;  // Días desde la última venta a crédito
}