package com.pos.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaPorMetodoPagoDTO {

    private String metodoPago;
    private Integer cantidadVentas;
    private BigDecimal totalVendido;
    private BigDecimal porcentaje;


    public VentaPorMetodoPagoDTO(String metodoPago, Long cantidadVentas, BigDecimal totalVendido) {
        this.metodoPago = metodoPago;
        this.cantidadVentas = cantidadVentas != null ? cantidadVentas.intValue() : 0;
        this.totalVendido = totalVendido;
    }
}