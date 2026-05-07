package com.pos.backend.controller;

import com.pos.backend.dto.request.AbonoRequest;
import com.pos.backend.dto.response.AbonoResponse;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/abonos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AbonoController {

    private final AbonoService abonoService;

    @PostMapping
    @Transactional
    public ResponseEntity<AbonoResponse> registrarAbono(@Valid @RequestBody AbonoRequest request) {
        Abono abono = new Abono();

        Cliente cliente = new Cliente();
        cliente.setId(request.getClienteId());
        abono.setCliente(cliente);

        if (request.getVentaId() != null) {
            Venta venta = new Venta();
            venta.setId(request.getVentaId());
            abono.setVenta(venta);
        }

        Usuario usuario = new Usuario();
        usuario.setId(request.getUsuarioId());
        abono.setUsuario(usuario);

        abono.setMonto(request.getMonto());
        abono.setMetodoPago(request.getMetodoPago());
        abono.setObservaciones(request.getObservaciones());

        Abono nuevoAbono = abonoService.registrarAbono(abono);
        return ResponseEntity.status(HttpStatus.CREATED).body(AbonoResponse.fromEntity(nuevoAbono));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<AbonoResponse>> obtenerTodos() {
        List<AbonoResponse> abonos = abonoService.obtenerTodos().stream()
                .map(AbonoResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(abonos);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<AbonoResponse> obtenerPorId(@PathVariable Long id) {
        Abono abono = abonoService.obtenerPorId(id);
        return ResponseEntity.ok(AbonoResponse.fromEntity(abono));
    }

    @GetMapping("/cliente/{clienteId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AbonoResponse>> obtenerPorCliente(@PathVariable Long clienteId) {
        List<AbonoResponse> abonos = abonoService.obtenerPorCliente(clienteId).stream()
                .map(AbonoResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(abonos);
    }

    @GetMapping("/venta/{ventaId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AbonoResponse>> obtenerPorVenta(@PathVariable Long ventaId) {
        List<AbonoResponse> abonos = abonoService.obtenerPorVenta(ventaId).stream()
                .map(AbonoResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(abonos);
    }

    @GetMapping("/venta/{ventaId}/saldo")
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public ResponseEntity<List<AbonoResponse>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        List<AbonoResponse> abonos = abonoService.obtenerPorUsuario(usuarioId).stream()
                .map(AbonoResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(abonos);
    }

    @GetMapping("/hoy")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AbonoResponse>> obtenerAbonosHoy() {
        List<AbonoResponse> abonos = abonoService.obtenerAbonosHoy().stream()
                .map(AbonoResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(abonos);
    }

    @GetMapping("/fecha")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AbonoResponse>> obtenerPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<AbonoResponse> abonos = abonoService.obtenerPorRangoFechas(inicio, fin).stream()
                .map(AbonoResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(abonos);
    }

    @GetMapping("/estadisticas/hoy")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> estadisticasDelDia() {
        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("cantidadAbonos", abonoService.contarAbonosDelDia());
        estadisticas.put("totalRecaudado", abonoService.totalAbonosDelDia());
        estadisticas.put("abonos", abonoService.obtenerAbonosHoy().stream()
                .map(AbonoResponse::fromEntity)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(estadisticas);
    }

    @PostMapping("/{id}/cancelar")
    @Transactional
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