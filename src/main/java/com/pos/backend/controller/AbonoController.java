package com.pos.backend.controller;

import com.pos.backend.dto.request.AbonoRequest;
import com.pos.backend.model.Abono;
import com.pos.backend.model.Cliente;
import com.pos.backend.model.Usuario;
import com.pos.backend.model.Venta;
import com.pos.backend.service.AbonoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/abonos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AbonoController {

    private final AbonoService abonoService;


    @PostMapping
    public ResponseEntity<Abono> registrarAbono(@Valid @RequestBody AbonoRequest request) {
        // Construir entidad Abono desde el DTO
        Abono abono = new Abono();

        // Cliente
        Cliente cliente = new Cliente();
        cliente.setId(request.getClienteId());
        abono.setCliente(cliente);

        // Venta (opcional)
        if (request.getVentaId() != null) {
            Venta venta = new Venta();
            venta.setId(request.getVentaId());
            abono.setVenta(venta);
        }

        // Usuario (cajero)
        Usuario usuario = new Usuario();
        usuario.setId(request.getUsuarioId());
        abono.setUsuario(usuario);

        // Datos del abono
        abono.setMonto(request.getMonto());
        abono.setMetodoPago(request.getMetodoPago());
        abono.setObservaciones(request.getObservaciones());

        // Registrar abono
        Abono nuevoAbono = abonoService.registrarAbono(abono);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoAbono);
    }


    @GetMapping
    public ResponseEntity<List<Abono>> obtenerTodos() {
        List<Abono> abonos = abonoService.obtenerTodos();
        return ResponseEntity.ok(abonos);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Abono> obtenerPorId(@PathVariable Long id) {
        Abono abono = abonoService.obtenerPorId(id);
        return ResponseEntity.ok(abono);
    }


    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Abono>> obtenerPorCliente(@PathVariable Long clienteId) {
        List<Abono> abonos = abonoService.obtenerPorCliente(clienteId);
        return ResponseEntity.ok(abonos);
    }


    @GetMapping("/venta/{ventaId}")
    public ResponseEntity<List<Abono>> obtenerPorVenta(@PathVariable Long ventaId) {
        List<Abono> abonos = abonoService.obtenerPorVenta(ventaId);
        return ResponseEntity.ok(abonos);
    }


    @GetMapping("/venta/{ventaId}/saldo")
    public ResponseEntity<Map<String, Object>> obtenerSaldoVenta(@PathVariable Long ventaId) {
        BigDecimal totalPagado = abonoService.totalAbonosVenta(ventaId);
        BigDecimal saldoPendiente = abonoService.saldoPendienteVenta(ventaId);

        Map<String, Object> saldo = new HashMap<>();
        saldo.put("ventaId", ventaId);
        saldo.put("totalPagado", totalPagado);
        saldo.put("saldoPendiente", saldoPendiente);

        return ResponseEntity.ok(saldo);
    }


    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Abono>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        List<Abono> abonos = abonoService.obtenerPorUsuario(usuarioId);
        return ResponseEntity.ok(abonos);
    }


    @GetMapping("/hoy")
    public ResponseEntity<List<Abono>> obtenerAbonosHoy() {
        List<Abono> abonos = abonoService.obtenerAbonosHoy();
        return ResponseEntity.ok(abonos);
    }


    @GetMapping("/fecha")
    public ResponseEntity<List<Abono>> obtenerPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<Abono> abonos = abonoService.obtenerPorRangoFechas(inicio, fin);
        return ResponseEntity.ok(abonos);
    }


    @GetMapping("/estadisticas/hoy")
    public ResponseEntity<Map<String, Object>> estadisticasDelDia() {
        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("cantidadAbonos", abonoService.contarAbonosDelDia());
        estadisticas.put("totalRecaudado", abonoService.totalAbonosDelDia());
        estadisticas.put("abonos", abonoService.obtenerAbonosHoy());

        return ResponseEntity.ok(estadisticas);
    }


    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarAbono(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        String motivoCancelacion = motivo != null ? motivo : "Sin motivo especificado";
        abonoService.cancelarAbono(id, motivoCancelacion);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        abonoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}