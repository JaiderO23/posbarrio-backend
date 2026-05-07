package com.pos.backend.controller;

import com.pos.backend.dto.request.VentaRequest;
import com.pos.backend.dto.response.VentaResponse;
import com.pos.backend.enums.EstadoVenta;
import com.pos.backend.enums.TipoVenta;
import com.pos.backend.model.Cliente;
import com.pos.backend.model.DetalleVenta;
import com.pos.backend.model.Producto;
import com.pos.backend.model.Usuario;
import com.pos.backend.model.Venta;
import com.pos.backend.service.ClienteService;
import com.pos.backend.service.ProductoService;
import com.pos.backend.service.UsuarioService;
import com.pos.backend.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VentaController {

    private final VentaService ventaService;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;
    private final ProductoService productoService;

    // POST - Crear nueva venta
    @PostMapping
    @Transactional
    public ResponseEntity<VentaResponse> crearVenta(@Valid @RequestBody VentaRequest request) {
        Venta venta = new Venta();

        if (request.getClienteId() != null) {
            Cliente cliente = new Cliente();
            cliente.setId(request.getClienteId());
            venta.setCliente(cliente);
        }

        Usuario usuario = new Usuario();
        usuario.setId(request.getUsuarioId());
        venta.setUsuario(usuario);

        venta.setTipoVenta(request.getTipoVenta());
        venta.setMetodoPago(request.getMetodoPago());
        venta.setDescuento(request.getDescuento());
        venta.setObservaciones(request.getObservaciones());

        List<DetalleVenta> detalles = new ArrayList<>();
        for (VentaRequest.DetalleVentaRequest detalleReq : request.getDetalles()) {
            DetalleVenta detalle = new DetalleVenta();
            Producto producto = new Producto();
            producto.setId(detalleReq.getProductoId());
            detalle.setProducto(producto);
            detalle.setCantidad(detalleReq.getCantidad());
            detalle.setPrecioUnitario(detalleReq.getPrecioUnitario());
            detalles.add(detalle);
        }
        venta.setDetalles(detalles);

        Venta nuevaVenta = ventaService.crearVenta(venta);
        return ResponseEntity.status(HttpStatus.CREATED).body(VentaResponse.fromEntity(nuevaVenta));
    }

    // GET - Obtener todas las ventas
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<VentaResponse>> obtenerTodas() {
        List<VentaResponse> ventas = ventaService.obtenerTodas().stream()
                .map(VentaResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ventas);
    }

    // GET - Obtener venta por ID
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<VentaResponse> obtenerPorId(@PathVariable Long id) {
        Venta venta = ventaService.obtenerPorId(id);
        return ResponseEntity.ok(VentaResponse.fromEntity(venta));
    }

    // GET - Obtener venta por UUID
    @GetMapping("/uuid/{uuid}")
    @Transactional(readOnly = true)
    public ResponseEntity<VentaResponse> obtenerPorUuid(@PathVariable UUID uuid) {
        Venta venta = ventaService.obtenerPorUuid(uuid);
        return ResponseEntity.ok(VentaResponse.fromEntity(venta));
    }

    // GET - Obtener venta por número
    @GetMapping("/numero/{numeroVenta}")
    @Transactional(readOnly = true)
    public ResponseEntity<VentaResponse> obtenerPorNumero(@PathVariable String numeroVenta) {
        Venta venta = ventaService.obtenerPorNumeroVenta(numeroVenta);
        return ResponseEntity.ok(VentaResponse.fromEntity(venta));
    }

    // GET - Ventas de un cliente
    @GetMapping("/cliente/{clienteId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<VentaResponse>> obtenerPorCliente(@PathVariable Long clienteId) {
        List<VentaResponse> ventas = ventaService.obtenerPorCliente(clienteId).stream()
                .map(VentaResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ventas);
    }

    // GET - Ventas de un usuario
    @GetMapping("/usuario/{usuarioId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<VentaResponse>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        List<VentaResponse> ventas = ventaService.obtenerPorUsuario(usuarioId).stream()
                .map(VentaResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ventas);
    }

    // GET - Ventas por tipo
    @GetMapping("/tipo/{tipoVenta}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<VentaResponse>> obtenerPorTipo(@PathVariable TipoVenta tipoVenta) {
        List<VentaResponse> ventas = ventaService.obtenerPorTipo(tipoVenta).stream()
                .map(VentaResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ventas);
    }

    // GET - Ventas del día
    @GetMapping("/hoy")
    @Transactional(readOnly = true)
    public ResponseEntity<List<VentaResponse>> obtenerVentasHoy() {
        List<VentaResponse> ventas = ventaService.obtenerVentasHoy().stream()
                .map(VentaResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ventas);
    }

    // GET - Ventas por rango de fechas
    @GetMapping("/fecha")
    @Transactional(readOnly = true)
    public ResponseEntity<List<VentaResponse>> obtenerPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<VentaResponse> ventas = ventaService.obtenerPorRangoFechas(inicio, fin).stream()
                .map(VentaResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ventas);
    }

    // GET - Ventas de un mes
    @GetMapping("/mes")
    @Transactional(readOnly = true)
    public ResponseEntity<List<VentaResponse>> obtenerVentasPorMes(
            @RequestParam int año,
            @RequestParam int mes) {
        List<VentaResponse> ventas = ventaService.obtenerVentasPorMes(año, mes).stream()
                .map(VentaResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ventas);
    }

    // PATCH - Cambiar estado
    @PatchMapping("/{id}/estado")
    @Transactional
    public ResponseEntity<VentaResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoVenta estado) {
        Venta venta = ventaService.cambiarEstado(id, estado);
        return ResponseEntity.ok(VentaResponse.fromEntity(venta));
    }

    // POST - Cancelar venta
    @PostMapping("/{id}/cancelar")
    @Transactional
    public ResponseEntity<VentaResponse> cancelarVenta(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        String motivoCancelacion = motivo != null ? motivo : "Sin motivo especificado";
        Venta venta = ventaService.cancelarVenta(id, motivoCancelacion);
        return ResponseEntity.ok(VentaResponse.fromEntity(venta));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVenta(@PathVariable Long id) {
        ventaService.eliminarVenta(id);
        return ResponseEntity.noContent().build();
    }

    // GET - Estadísticas del día
    @GetMapping("/estadisticas/hoy")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> estadisticasDelDia() {
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("cantidadVentas", ventaService.contarVentasDelDia());
        estadisticas.put("totalVendido", ventaService.totalVentasDelDia());
        estadisticas.put("ventas", ventaService.obtenerVentasHoy().stream()
                .map(VentaResponse::fromEntity)
                .collect(Collectors.toList()));
        return ResponseEntity.ok(estadisticas);
    }
}