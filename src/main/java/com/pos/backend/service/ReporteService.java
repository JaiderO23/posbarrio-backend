package com.pos.backend.service;

import com.pos.backend.dto.response.*;
import com.pos.backend.model.Cliente;
import com.pos.backend.model.Producto;
import com.pos.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final AbonoRepository abonoRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    /**
     * REPORTE 1: Productos más vendidos
     * Retorna los N productos con mayor cantidad vendida
     */
    public List<ProductoVendidoDTO> productosMasVendidos(int limite) {
        // Esta query necesita agregación personalizada
        // La implementaremos con una query nativa en el repository

        List<Object[]> resultados = detalleVentaRepository.findProductosMasVendidos(limite);

        return resultados.stream()
                .map(row -> new ProductoVendidoDTO(
                        ((Number) row[0]).longValue(),      // productoId
                        (String) row[1],                     // nombreProducto
                        ((Number) row[2]).longValue(),       // cantidadVendida
                        (BigDecimal) row[3]                  // totalVendido
                ))
                .collect(Collectors.toList());
    }

    /**
     * REPORTE 2: Ventas por día (últimos N días)
     */
    public List<VentaPorPeriodoDTO> ventasPorDia(int dias) {
        List<Object[]> resultados = ventaRepository.findVentasPorDia(dias);

        return resultados.stream()
                .map(row -> new VentaPorPeriodoDTO(
                        (String) row[0],                     // fecha
                        ((Number) row[1]).longValue(),       // cantidadVentas
                        (BigDecimal) row[2]                  // totalVendido
                ))
                .collect(Collectors.toList());
    }

    /**
     * REPORTE 3: Ventas por mes (últimos N meses)
     */
    public List<VentaPorPeriodoDTO> ventasPorMes(int meses) {
        List<Object[]> resultados = ventaRepository.findVentasPorMes(meses);

        return resultados.stream()
                .map(row -> new VentaPorPeriodoDTO(
                        (String) row[0],                     // periodo (YYYY-MM)
                        ((Number) row[1]).longValue(),       // cantidadVentas
                        (BigDecimal) row[2]                  // totalVendido
                ))
                .collect(Collectors.toList());
    }

    /**
     * REPORTE 4: Clientes con deuda (ordenados de mayor a menor)
     */
    public List<ClienteDeudaDTO> clientesConDeuda() {
        List<Cliente> clientes = clienteRepository.findClientesConDeuda();

        return clientes.stream()
                .map(cliente -> new ClienteDeudaDTO(
                        cliente.getId(),
                        cliente.getNombre() + " " + cliente.getApellido(),
                        cliente.getNumeroDocumento(),
                        cliente.getTelefono(),
                        cliente.getSaldoDeuda(),
                        cliente.getLimiteCredito(),
                        cliente.getCreditoDisponible(),
                        null  // diasDeuda lo calculamos después si es necesario
                ))
                .collect(Collectors.toList());
    }

    /**
     * REPORTE 5: Ventas por método de pago
     */
    public List<VentaPorMetodoPagoDTO> ventasPorMetodoPago() {
        List<Object[]> resultados = ventaRepository.findVentasPorMetodoPago();

        // Calcular total general para porcentajes
        BigDecimal totalGeneral = resultados.stream()
                .map(row -> (BigDecimal) row[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return resultados.stream()
                .map(row -> {
                    String metodoPago = (String) row[0];
                    Long cantidad = ((Number) row[1]).longValue();
                    BigDecimal total = (BigDecimal) row[2];

                    // Calcular porcentaje
                    BigDecimal porcentaje = BigDecimal.ZERO;
                    if (totalGeneral.compareTo(BigDecimal.ZERO) > 0) {
                        porcentaje = total.divide(totalGeneral, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .setScale(2, RoundingMode.HALF_UP);
                    }

                    VentaPorMetodoPagoDTO dto = new VentaPorMetodoPagoDTO(
                            metodoPago,
                            cantidad,
                            total
                    );
                    dto.setPorcentaje(porcentaje);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * REPORTE 6: Productos con stock bajo
     */
    public List<ProductoStockBajoDTO> productosConStockBajo() {
        List<Producto> productos = productoRepository.findProductosStockBajo();

        return productos.stream()
                .map(producto -> {
                    int faltante = producto.getStockMinimo() - producto.getStockActual();

                    String estado;
                    if (producto.getStockActual() == 0) {
                        estado = "AGOTADO";
                    } else if (faltante >= producto.getStockMinimo() * 0.5) {
                        estado = "CRÍTICO";
                    } else {
                        estado = "BAJO";
                    }

                    return new ProductoStockBajoDTO(
                            producto.getId(),
                            producto.getNombre(),
                            producto.getCodigoBarras(),
                            producto.getStockActual(),
                            producto.getStockMinimo(),
                            faltante,
                            estado
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * REPORTE 7: Resumen ejecutivo (Dashboard)
     */
    public ResumenEjecutivoDTO resumenEjecutivo() {
        ResumenEjecutivoDTO resumen = new ResumenEjecutivoDTO();

        // Ventas de hoy
        resumen.setVentasHoy(ventaRepository.contarVentasDelDia().intValue());
        resumen.setTotalVentasHoy(ventaRepository.sumarVentasDelDia());

        // Ventas del mes
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finMes = LocalDateTime.now();
        List<Object[]> ventasMes = ventaRepository.findVentasEnRango(inicioMes, finMes);

        if (!ventasMes.isEmpty()) {
            Object[] datos = ventasMes.get(0);
            resumen.setVentasMes(((Number) datos[0]).intValue());
            resumen.setTotalVentasMes((BigDecimal) datos[1]);
        } else {
            resumen.setVentasMes(0);
            resumen.setTotalVentasMes(BigDecimal.ZERO);
        }

        // Abonos de hoy
        resumen.setAbonosHoy(abonoRepository.contarAbonosDelDia().intValue());
        resumen.setTotalAbonosHoy(abonoRepository.sumarAbonosDelDia());

        // Clientes con deuda
        resumen.setClientesConDeuda(clienteRepository.contarClientesConDeuda());
        resumen.setTotalDeudas(clienteRepository.sumarTotalDeudas());

        // Productos con stock bajo
        resumen.setProductosStockBajo(productoRepository.contarProductosStockBajo());

        // Top 5 productos del mes
        resumen.setTop5ProductosMes(productosMasVendidos(5));

        return resumen;
    }

    /**
     * REPORTE 8: Ventas por usuario (cajero)
     */
    public List<Object> ventasPorUsuario(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findVentasPorUsuario(inicio, fin);
    }
}