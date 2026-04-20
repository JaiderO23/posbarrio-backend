package com.pos.backend.controller;

import com.pos.backend.dto.response.*;
import com.pos.backend.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReporteController {

    private final ReporteService reporteService;


    @GetMapping("/productos/mas-vendidos")
    public ResponseEntity<List<ProductoVendidoDTO>> productosMasVendidos(
            @RequestParam(defaultValue = "10") int limite) {
        List<ProductoVendidoDTO> reporte = reporteService.productosMasVendidos(limite);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/ventas/por-dia")
    public ResponseEntity<List<VentaPorPeriodoDTO>> ventasPorDia(
            @RequestParam(defaultValue = "7") int dias) {
        List<VentaPorPeriodoDTO> reporte = reporteService.ventasPorDia(dias);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/ventas/por-mes")
    public ResponseEntity<List<VentaPorPeriodoDTO>> ventasPorMes(
            @RequestParam(defaultValue = "6") int meses) {
        List<VentaPorPeriodoDTO> reporte = reporteService.ventasPorMes(meses);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/clientes/con-deuda")
    public ResponseEntity<List<ClienteDeudaDTO>> clientesConDeuda() {
        List<ClienteDeudaDTO> reporte = reporteService.clientesConDeuda();
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/ventas/por-metodo-pago")
    public ResponseEntity<List<VentaPorMetodoPagoDTO>> ventasPorMetodoPago(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<VentaPorMetodoPagoDTO> reporte = reporteService.ventasPorMetodoPago(inicio, fin);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/productos/stock-bajo")
    public ResponseEntity<List<ProductoStockBajoDTO>> productosStockBajo() {
        List<ProductoStockBajoDTO> reporte = reporteService.productosConStockBajo();
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ResumenEjecutivoDTO> dashboard() {
        ResumenEjecutivoDTO resumen = reporteService.resumenEjecutivo();
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/ventas/por-usuario")
    public ResponseEntity<List<Object>> ventasPorUsuario(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<Object> reporte = reporteService.ventasPorUsuario(inicio, fin);
        return ResponseEntity.ok(reporte);
    }
}