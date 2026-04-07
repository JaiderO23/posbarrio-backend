package com.pos.backend.controller;

import com.pos.backend.dto.request.VentaRequest;
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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public ResponseEntity<Venta> crearVenta(@Valid @RequestBody VentaRequest request) {
        // Construir entidad Venta desde el DTO
        Venta venta = new Venta();

        // Cliente (opcional)
        if (request.getClienteId() != null) {
            Cliente cliente = new Cliente();
            cliente.setId(request.getClienteId());
            venta.setCliente(cliente);
        }

        // Usuario (cajero) - obligatorio
        Usuario usuario = new Usuario();
        usuario.setId(request.getUsuarioId());
        venta.setUsuario(usuario);

        // Tipo y método de pago
        venta.setTipoVenta(request.getTipoVenta());
        venta.setMetodoPago(request.getMetodoPago());
        venta.setDescuento(request.getDescuento());
        venta.setObservaciones(request.getObservaciones());

        // Construir detalles
        List<DetalleVenta> detalles = new ArrayList<>();
        for (VentaRequest.DetalleVentaRequest detalleReq : request.getDetalles()) {
            DetalleVenta detalle = new DetalleVenta();

            // Producto
            Producto producto = new Producto();
            producto.setId(detalleReq.getProductoId());
            detalle.setProducto(producto);

            detalle.setCantidad(detalleReq.getCantidad());
            detalle.setPrecioUnitario(detalleReq.getPrecioUnitario());

            detalles.add(detalle);
        }
        venta.setDetalles(detalles);

        // Crear venta
        Venta nuevaVenta = ventaService.crearVenta(venta);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaVenta);
    }

    // GET - Obtener todas las ventas
    @GetMapping
    public ResponseEntity<List<Venta>> obtenerTodas() {
        List<Venta> ventas = ventaService.obtenerTodas();
        return ResponseEntity.ok(ventas);
    }


    // GET - Obtener venta por ID
    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtenerPorId(@PathVariable Long id) {
        Venta venta = ventaService.obtenerPorId(id);
        return ResponseEntity.ok(venta);
    }

    // GET - Obtener venta por UUID
    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<Venta> obtenerPorUuid(@PathVariable UUID uuid) {
        Venta venta = ventaService.obtenerPorUuid(uuid);
        return ResponseEntity.ok(venta);
    }


    // GET - Obtener venta por número
    @GetMapping("/numero/{numeroVenta}")
    public ResponseEntity<Venta> obtenerPorNumero(@PathVariable String numeroVenta) {
        Venta venta = ventaService.obtenerPorNumeroVenta(numeroVenta);
        return ResponseEntity.ok(venta);
    }

    // GET - Obtener ventas de un cliente
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Venta>> obtenerPorCliente(@PathVariable Long clienteId) {
        List<Venta> ventas = ventaService.obtenerPorCliente(clienteId);
        return ResponseEntity.ok(ventas);
    }


    // GET - Obtener ventas de un usuario (cajero)
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Venta>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        List<Venta> ventas = ventaService.obtenerPorUsuario(usuarioId);
        return ResponseEntity.ok(ventas);
    }


    // GET - Obtener ventas por tipo (CONTADO/CREDITO)
    @GetMapping("/tipo/{tipoVenta}")
    public ResponseEntity<List<Venta>> obtenerPorTipo(@PathVariable TipoVenta tipoVenta) {
        List<Venta> ventas = ventaService.obtenerPorTipo(tipoVenta);
        return ResponseEntity.ok(ventas);
    }

    // GET - Obtener ventas del día
    @GetMapping("/hoy")
    public ResponseEntity<List<Venta>> obtenerVentasHoy() {
        List<Venta> ventas = ventaService.obtenerVentasHoy();
        return ResponseEntity.ok(ventas);
    }

    // GET - Obtener ventas por rango de fechas
    @GetMapping("/fecha")
    public ResponseEntity<List<Venta>> obtenerPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<Venta> ventas = ventaService.obtenerPorRangoFechas(inicio, fin);
        return ResponseEntity.ok(ventas);
    }


    // GET - Obtener ventas de un mes
    @GetMapping("/mes")
    public ResponseEntity<List<Venta>> obtenerVentasPorMes(
            @RequestParam int año,
            @RequestParam int mes) {
        List<Venta> ventas = ventaService.obtenerVentasPorMes(año, mes);
        return ResponseEntity.ok(ventas);
    }


    // PATCH - Cambiar estado de venta
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Venta> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoVenta estado) {
        Venta venta = ventaService.cambiarEstado(id, estado);
        return ResponseEntity.ok(venta);
    }


    // POST - Cancelar venta
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Venta> cancelarVenta(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        String motivoCancelacion = motivo != null ? motivo : "Sin motivo especificado";
        Venta venta = ventaService.cancelarVenta(id, motivoCancelacion);
        return ResponseEntity.ok(venta);
    }


    // DELETE - Eliminar venta (solo canceladas)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVenta(@PathVariable Long id) {
        ventaService.eliminarVenta(id);
        return ResponseEntity.noContent().build();
    }


    // GET - Estadísticas del día
    @GetMapping("/estadisticas/hoy")
    public ResponseEntity<Map<String, Object>> estadisticasDelDia() {
        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("cantidadVentas", ventaService.contarVentasDelDia());
        estadisticas.put("totalVendido", ventaService.totalVentasDelDia());
        estadisticas.put("ventas", ventaService.obtenerVentasHoy());

        return ResponseEntity.ok(estadisticas);
    }
}