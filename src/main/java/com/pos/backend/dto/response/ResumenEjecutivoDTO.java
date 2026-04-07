package com.pos.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenEjecutivoDTO {

    // Ventas
    private Integer ventasHoy;
    private BigDecimal totalVentasHoy;
    private Integer ventasMes;
    private BigDecimal totalVentasMes;

    // Abonos
    private Integer abonosHoy;
    private BigDecimal totalAbonosHoy;

    // Clientes
    private Integer clientesConDeuda;
    private BigDecimal totalDeudas;

    // Productos
    private Integer productosStockBajo;

    // Top 5
    private List<ProductoVendidoDTO> top5ProductosMes;
}