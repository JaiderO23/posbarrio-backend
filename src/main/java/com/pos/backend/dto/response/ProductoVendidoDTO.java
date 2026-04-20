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
    private String categoriaNombre;
    private Integer cantidadVendida;
    private BigDecimal totalVendido;

    public ProductoVendidoDTO(Long productoId, String nombreProducto, String categoriaNombre, Long cantidadVendida, BigDecimal totalVendido) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.categoriaNombre = categoriaNombre;
        this.cantidadVendida = cantidadVendida != null ? cantidadVendida.intValue() : 0;
        this.totalVendido = totalVendido;
    }
}