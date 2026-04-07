package com.pos.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoStockBajoDTO {

    private Long productoId;
    private String nombre;
    private String codigoBarras;
    private Integer stockActual;
    private Integer stockMinimo;
    private Integer cantidadFaltante;
    private String estado;
}