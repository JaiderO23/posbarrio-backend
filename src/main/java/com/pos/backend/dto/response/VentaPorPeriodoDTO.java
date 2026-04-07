package com.pos.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaPorPeriodoDTO {

    private String periodo;
    private Integer cantidadVentas;
    private BigDecimal totalVendido;

    // Constructor para queries nativas
    public VentaPorPeriodoDTO(String periodo, Long cantidadVentas, BigDecimal totalVendido) {
        this.periodo = periodo;
        this.cantidadVentas = cantidadVentas != null ? cantidadVentas.intValue() : 0;
        this.totalVendido = totalVendido;
    }
}