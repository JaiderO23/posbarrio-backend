package com.pos.backend.controller;

import com.pos.backend.model.Cliente;
import com.pos.backend.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClienteController {

    private final ClienteService clienteService;

    // Obtener todos los clientes
    @GetMapping
    public ResponseEntity<List<Cliente>> obtenerTodos() {
        List<Cliente> clientes = clienteService.obtenerTodos();
        return ResponseEntity.ok(clientes);
    }

    // Obtener solo clientes activos
    @GetMapping("/activos")
    public ResponseEntity<List<Cliente>> obtenerActivos() {
        List<Cliente> clientes = clienteService.obtenerActivos();
        return ResponseEntity.ok(clientes);
    }


    //  Obtener clientes con deuda
    @GetMapping("/con-deuda")
    public ResponseEntity<List<Cliente>> obtenerClientesConDeuda() {
        List<Cliente> clientes = clienteService.obtenerClientesConDeuda();
        return ResponseEntity.ok(clientes);
    }


    // Obtener clientes con crédito habilitado
    @GetMapping("/con-credito")
    public ResponseEntity<List<Cliente>> obtenerClientesConCredito() {
        List<Cliente> clientes = clienteService.obtenerClientesConCredito();
        return ResponseEntity.ok(clientes);
    }

    // Obtener cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Long id) {
        Cliente cliente = clienteService.obtenerPorId(id);
        return ResponseEntity.ok(cliente);
    }

    // Obtener cliente por UUID
    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<Cliente> obtenerPorUuid(@PathVariable UUID uuid) {
        Cliente cliente = clienteService.obtenerPorUuid(uuid);
        return ResponseEntity.ok(cliente);
    }

    //  Obtener cliente por documento
    @GetMapping("/documento/{numeroDocumento}")
    public ResponseEntity<Cliente> obtenerPorDocumento(@PathVariable String numeroDocumento) {
        Cliente cliente = clienteService.obtenerPorDocumento(numeroDocumento);
        return ResponseEntity.ok(cliente);
    }

    // Obtener cliente por email
    @GetMapping("/email/{email}")
    public ResponseEntity<Cliente> obtenerPorEmail(@PathVariable String email) {
        Cliente cliente = clienteService.obtenerPorEmail(email);
        return ResponseEntity.ok(cliente);
    }

    // Buscar clientes por nombre o apellido
    @GetMapping("/buscar")
    public ResponseEntity<List<Cliente>> buscarPorNombre(@RequestParam String texto) {
        List<Cliente> clientes = clienteService.buscarPorNombre(texto);
        return ResponseEntity.ok(clientes);
    }

    // Crear nuevo cliente
    @PostMapping
    public ResponseEntity<Cliente> crearCliente(@Valid @RequestBody Cliente cliente) {
        Cliente nuevoCliente = clienteService.crearCliente(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCliente);
    }

    // Actualizar cliente
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody Cliente cliente) {
        Cliente clienteActualizado = clienteService.actualizarCliente(id, cliente);
        return ResponseEntity.ok(clienteActualizado);
    }

    //Eliminar cliente (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }

    //  Eliminar cliente permanentemente
    @DeleteMapping("/{id}/permanente")
    public ResponseEntity<Void> eliminarClientePermanente(@PathVariable Long id) {
        clienteService.eliminarClientePermanente(id);
        return ResponseEntity.noContent().build();
    }

    // Registrar compra a crédito
    @PostMapping("/{id}/compra-credito")
    public ResponseEntity<Cliente> registrarCompraCredito(
            @PathVariable Long id,
            @RequestParam BigDecimal monto) {
        Cliente cliente = clienteService.registrarCompraCredito(id, monto);
        return ResponseEntity.ok(cliente);
    }


    // Registrar abono
    @PostMapping("/{id}/abono")
    public ResponseEntity<Cliente> registrarAbono(
            @PathVariable Long id,
            @RequestParam BigDecimal monto) {
        Cliente cliente = clienteService.registrarAbono(id, monto);
        return ResponseEntity.ok(cliente);
    }

    // Actualizar límite de crédito
    @PatchMapping("/{id}/limite-credito")
    public ResponseEntity<Cliente> actualizarLimiteCredito(
            @PathVariable Long id,
            @RequestParam BigDecimal nuevoLimite) {
        Cliente cliente = clienteService.actualizarLimiteCredito(id, nuevoLimite);
        return ResponseEntity.ok(cliente);
    }
}