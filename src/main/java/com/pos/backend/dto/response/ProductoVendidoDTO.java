package com.pos.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoVendidoDTO {

    private Long productoId;
    private String nombreProducto;
    private Integer cantidadVendida;
    private BigDecimal totalVendido;

    // Constructor para queries nativas
    public ProductoVendidoDTO(Long productoId, String nombreProducto, Long cantidadVendida, BigDecimal totalVendido) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidadVendida = cantidadVendida != null ? cantidadVendida.intValue() : 0;
        this.totalVendido = totalVendido;
    }
}