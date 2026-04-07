package com.pos.backend.service;

import com.pos.backend.model.Cliente;
import com.pos.backend.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;


    // CREAR CLIENTE
    public Cliente crearCliente(Cliente cliente) {
        // Validar que no exista el documento (si fue proporcionado)
        if (cliente.getNumeroDocumento() != null &&
                !cliente.getNumeroDocumento().isEmpty()) {
            if (clienteRepository.existsByNumeroDocumento(cliente.getNumeroDocumento())) {
                throw new RuntimeException("Ya existe un cliente con el documento: " +
                        cliente.getNumeroDocumento());
            }
        }

        // Validar que no exista el email
        if (cliente.getEmail() != null && !cliente.getEmail().isEmpty()) {
            if (clienteRepository.existsByEmail(cliente.getEmail())) {
                throw new RuntimeException("Ya existe un cliente con el email: " +
                        cliente.getEmail());
            }
        }

        // Generar UUID si no tiene
        if (cliente.getUuid() == null) {
            cliente.setUuid(UUID.randomUUID());
        }

        return clienteRepository.save(cliente);
    }


    // OBTENER TODOS LOS CLIENTES

    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    // OBTENER CLIENTES ACTIVOS

    public List<Cliente> obtenerActivos() {
        return clienteRepository.findByActivoTrue();
    }

    // OBTENER CLIENTE POR ID
    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }


    // OBTENER CLIENTE POR UUID
    public Cliente obtenerPorUuid(UUID uuid) {
        return clienteRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con UUID: " + uuid));
    }


    // OBTENER CLIENTE POR DOCUMENTO
    public Cliente obtenerPorDocumento(String numeroDocumento) {
        return clienteRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con documento: " +
                        numeroDocumento));
    }


    // OBTENER CLIENTE POR EMAIL
    public Cliente obtenerPorEmail(String email) {
        return clienteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con email: " + email));
    }


    // BUSCAR CLIENTES POR NOMBRE O APELLIDO
    public List<Cliente> buscarPorNombre(String texto) {
        return clienteRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(
                texto, texto
        );
    }


    // OBTENER CLIENTES CON DEUDA
    public List<Cliente> obtenerClientesConDeuda() {
        return clienteRepository.findBySaldoDeudaGreaterThan(BigDecimal.ZERO);
    }


    // OBTENER CLIENTES CON CRÉDITO HABILITADO

    public List<Cliente> obtenerClientesConCredito() {
        return clienteRepository.findByLimiteCreditoGreaterThan(BigDecimal.ZERO);
    }


    // ACTUALIZAR CLIENTE
    public Cliente actualizarCliente(Long id, Cliente clienteActualizado) {
        Cliente clienteExistente = obtenerPorId(id);

        // Validar documento único (si cambió)
        if (clienteActualizado.getNumeroDocumento() != null &&
                !clienteActualizado.getNumeroDocumento().equals(clienteExistente.getNumeroDocumento())) {
            if (clienteRepository.existsByNumeroDocumento(clienteActualizado.getNumeroDocumento())) {
                throw new RuntimeException("Ya existe un cliente con el documento: " +
                        clienteActualizado.getNumeroDocumento());
            }
        }

        // Validar email único (si cambió)
        if (clienteActualizado.getEmail() != null &&
                !clienteActualizado.getEmail().equals(clienteExistente.getEmail())) {
            if (clienteRepository.existsByEmail(clienteActualizado.getEmail())) {
                throw new RuntimeException("Ya existe un cliente con el email: " +
                        clienteActualizado.getEmail());
            }
        }

        // Actualizar campos
        clienteExistente.setNombre(clienteActualizado.getNombre());
        clienteExistente.setApellido(clienteActualizado.getApellido());
        clienteExistente.setTipoDocumento(clienteActualizado.getTipoDocumento());
        clienteExistente.setNumeroDocumento(clienteActualizado.getNumeroDocumento());
        clienteExistente.setTelefono(clienteActualizado.getTelefono());
        clienteExistente.setEmail(clienteActualizado.getEmail());
        clienteExistente.setDireccion(clienteActualizado.getDireccion());
        clienteExistente.setLimiteCredito(clienteActualizado.getLimiteCredito());
        clienteExistente.setActivo(clienteActualizado.getActivo());

        // Incrementar versión
        clienteExistente.setVersion(clienteExistente.getVersion() + 1);

        return clienteRepository.save(clienteExistente);
    }


    // ELIMINAR CLIENTE (SOFT DELETE)
    public void eliminarCliente(Long id) {
        Cliente cliente = obtenerPorId(id);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }


    // ELIMINAR CLIENTE (HARD DELETE)
    public void eliminarClientePermanente(Long id) {
        clienteRepository.deleteById(id);
    }


    // REGISTRAR COMPRA A CRÉDITO (aumentar deuda)
    public Cliente registrarCompraCredito(Long id, BigDecimal monto) {
        Cliente cliente = obtenerPorId(id);

        // Verificar que tiene crédito habilitado
        if (!cliente.esClienteCredito()) {
            throw new RuntimeException("El cliente no tiene crédito habilitado");
        }

        // Verificar que tiene crédito disponible
        if (!cliente.puedeComprarACredito(monto)) {
            throw new RuntimeException("Crédito insuficiente. Disponible: $" +
                    cliente.getCreditoDisponible() + ", Solicitado: $" + monto);
        }

        // Aumentar deuda
        cliente.setSaldoDeuda(cliente.getSaldoDeuda().add(monto));
        cliente.setVersion(cliente.getVersion() + 1);

        return clienteRepository.save(cliente);
    }


    // REGISTRAR ABONO (disminuir deuda)
    public Cliente registrarAbono(Long id, BigDecimal monto) {
        Cliente cliente = obtenerPorId(id);

        // Validar que el monto no sea mayor a la deuda
        if (monto.compareTo(cliente.getSaldoDeuda()) > 0) {
            throw new RuntimeException("El monto del abono ($" + monto +
                    ") es mayor a la deuda ($" + cliente.getSaldoDeuda() + ")");
        }

        // Disminuir deuda
        cliente.setSaldoDeuda(cliente.getSaldoDeuda().subtract(monto));
        cliente.setVersion(cliente.getVersion() + 1);

        return clienteRepository.save(cliente);
    }


    // ACTUALIZAR LÍMITE DE CRÉDITO
    public Cliente actualizarLimiteCredito(Long id, BigDecimal nuevoLimite) {
        Cliente cliente = obtenerPorId(id);

        // Validar que el nuevo límite sea >= a la deuda actual
        if (nuevoLimite.compareTo(cliente.getSaldoDeuda()) < 0) {
            throw new RuntimeException("El nuevo límite de crédito no puede ser menor " +
                    "a la deuda actual ($" + cliente.getSaldoDeuda() + ")");
        }

        cliente.setLimiteCredito(nuevoLimite);
        cliente.setVersion(cliente.getVersion() + 1);

        return clienteRepository.save(cliente);
    }
}