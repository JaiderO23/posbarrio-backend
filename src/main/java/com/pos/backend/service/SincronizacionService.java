package com.pos.backend.service;

import com.pos.backend.enums.MetodoPago;
import com.pos.backend.enums.Rol;
import com.pos.backend.enums.TipoDocumento;
import com.pos.backend.enums.TipoVenta;
import com.pos.backend.enums.EstadoVenta;
import com.pos.backend.model.*;
import com.pos.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SincronizacionService {

    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AbonoRepository abonoRepository;

    @Value("${spring.datasource.remote.url:}")
    private String remoteUrl;

    @Value("${spring.datasource.remote.username:}")
    private String remoteUsername;

    @Value("${spring.datasource.remote.password:}")
    private String remotePassword;

    private static final String SINCRONIZADO = "SINCRONIZADO";

    public boolean hayInternet() {
        try {
            java.net.InetAddress.getByName("google.com");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hayConexionRemota() {
        if (remoteUrl == null || remoteUrl.isEmpty()) return false;
        try (Connection conn = DriverManager.getConnection(remoteUrl, remoteUsername, remotePassword)) {
            return conn.isValid(3);
        } catch (Exception e) {
            return false;
        }
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void sincronizar() {
        if (!hayInternet() || !hayConexionRemota()) {
            System.out.println("Sin conexión remota - trabajando offline");
            return;
        }
        System.out.println("Iniciando sincronización con Supabase...");
        try (Connection remota = DriverManager.getConnection(remoteUrl, remoteUsername, remotePassword)) {
            remota.setAutoCommit(false);

            // LOCAL → SUPABASE
            sincronizarCategorias(remota);
            sincronizarProductos(remota);
            sincronizarClientes(remota);
            sincronizarUsuarios(remota);
            sincronizarVentas(remota);
            sincronizarAbonos(remota);
            remota.commit();

            // SUPABASE → LOCAL
            System.out.println("Descargando datos de Supabase...");
            descargarCategorias(remota);
            descargarProductos(remota);
            descargarClientes(remota);
            descargarUsuarios(remota);
            descargarVentas(remota);
            descargarAbonos(remota);

            System.out.println("Sincronización completada ✓");
        } catch (Exception e) {
            System.err.println("Error en sincronización: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== LOCAL → SUPABASE ====================

    private void sincronizarCategorias(Connection remota) throws SQLException {
        List<Categoria> pendientes = categoriaRepository.findAll().stream()
                .filter(c -> !SINCRONIZADO.equals(c.getSincronizadoDesde()))
                .toList();

        String sql = """
            INSERT INTO categorias (uuid, nombre, descripcion, activo, creado_en, actualizado_en, sincronizado_desde, version)
            VALUES (?::uuid, ?, ?, ?, ?, ?, 'SINCRONIZADO', ?)
            ON CONFLICT (uuid) DO UPDATE SET
                nombre = EXCLUDED.nombre,
                descripcion = EXCLUDED.descripcion,
                activo = EXCLUDED.activo,
                actualizado_en = EXCLUDED.actualizado_en,
                version = EXCLUDED.version
            """;

        try (PreparedStatement ps = remota.prepareStatement(sql)) {
            for (Categoria c : pendientes) {
                ps.setString(1, c.getUuid().toString());
                ps.setString(2, c.getNombre());
                ps.setString(3, c.getDescripcion());
                ps.setBoolean(4, c.getActivo() != null && c.getActivo());
                ps.setTimestamp(5, Timestamp.valueOf(c.getCreadoEn()));
                ps.setTimestamp(6, Timestamp.valueOf(c.getActualizadoEn()));
                ps.setInt(7, c.getVersion());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        pendientes.forEach(c -> c.setSincronizadoDesde(SINCRONIZADO));
        categoriaRepository.saveAll(pendientes);
        System.out.println("Categorías subidas: " + pendientes.size());
    }

    private void sincronizarProductos(Connection remota) throws SQLException {
        List<Producto> pendientes = productoRepository.findAll().stream()
                .filter(p -> !SINCRONIZADO.equals(p.getSincronizadoDesde()))
                .toList();

        String sql = """
            INSERT INTO productos (uuid, nombre, descripcion, codigo_barras, precio_compra, precio_venta,
                stock_actual, stock_minimo, activo, creado_en, actualizado_en, sincronizado_desde, version, categoria_id)
            VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'SINCRONIZADO', ?,
                (SELECT id FROM categorias WHERE uuid = ?::uuid))
            ON CONFLICT (uuid) DO UPDATE SET
                nombre = EXCLUDED.nombre,
                descripcion = EXCLUDED.descripcion,
                precio_compra = EXCLUDED.precio_compra,
                precio_venta = EXCLUDED.precio_venta,
                stock_actual = EXCLUDED.stock_actual,
                stock_minimo = EXCLUDED.stock_minimo,
                activo = EXCLUDED.activo,
                actualizado_en = EXCLUDED.actualizado_en,
                version = EXCLUDED.version
            """;

        try (PreparedStatement ps = remota.prepareStatement(sql)) {
            for (Producto p : pendientes) {
                ps.setString(1, p.getUuid().toString());
                ps.setString(2, p.getNombre());
                ps.setString(3, p.getDescripcion());
                ps.setString(4, p.getCodigoBarras());
                ps.setBigDecimal(5, p.getPrecioCompra());
                ps.setBigDecimal(6, p.getPrecioVenta());
                ps.setInt(7, p.getStockActual());
                ps.setInt(8, p.getStockMinimo());
                ps.setBoolean(9, p.getActivo() != null && p.getActivo());
                ps.setTimestamp(10, Timestamp.valueOf(p.getCreadoEn()));
                ps.setTimestamp(11, Timestamp.valueOf(p.getActualizadoEn()));
                ps.setInt(12, p.getVersion());
                ps.setString(13, p.getCategoria() != null ? p.getCategoria().getUuid().toString() : null);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        pendientes.forEach(p -> p.setSincronizadoDesde(SINCRONIZADO));
        productoRepository.saveAll(pendientes);
        System.out.println("Productos subidos: " + pendientes.size());
    }

    private void sincronizarClientes(Connection remota) throws SQLException {
        List<Cliente> pendientes = clienteRepository.findAll().stream()
                .filter(c -> !SINCRONIZADO.equals(c.getSincronizadoDesde()))
                .toList();

        String sql = """
            INSERT INTO clientes (uuid, nombre, apellido, tipo_documento, numero_documento,
                telefono, email, direccion, limite_credito, saldo_deuda, activo, creado_en, actualizado_en, sincronizado_desde, version)
            VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'SINCRONIZADO', ?)
            ON CONFLICT (uuid) DO UPDATE SET
                nombre = EXCLUDED.nombre,
                apellido = EXCLUDED.apellido,
                telefono = EXCLUDED.telefono,
                saldo_deuda = EXCLUDED.saldo_deuda,
                activo = EXCLUDED.activo,
                actualizado_en = EXCLUDED.actualizado_en,
                version = EXCLUDED.version
            """;

        try (PreparedStatement ps = remota.prepareStatement(sql)) {
            for (Cliente c : pendientes) {
                ps.setString(1, c.getUuid().toString());
                ps.setString(2, c.getNombre());
                ps.setString(3, c.getApellido());
                ps.setString(4, c.getTipoDocumento() != null ? c.getTipoDocumento().name() : null);
                ps.setString(5, c.getNumeroDocumento());
                ps.setString(6, c.getTelefono());
                ps.setString(7, c.getEmail());
                ps.setString(8, c.getDireccion());
                ps.setBigDecimal(9, c.getLimiteCredito());
                ps.setBigDecimal(10, c.getSaldoDeuda());
                ps.setBoolean(11, c.getActivo() != null && c.getActivo());
                ps.setTimestamp(12, Timestamp.valueOf(c.getCreadoEn()));
                ps.setTimestamp(13, Timestamp.valueOf(c.getActualizadoEn()));
                ps.setInt(14, c.getVersion());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        pendientes.forEach(c -> c.setSincronizadoDesde(SINCRONIZADO));
        clienteRepository.saveAll(pendientes);
        System.out.println("Clientes subidos: " + pendientes.size());
    }

    private void sincronizarUsuarios(Connection remota) throws SQLException {
        List<Usuario> pendientes = usuarioRepository.findPendientesSincronizacion();

        String sql = """
            INSERT INTO usuarios (uuid, nombre_usuario, contraseña, nombre_completo, rol, activo, creado_en, actualizado_en, sincronizado_desde, version)
            VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?, 'SINCRONIZADO', ?)
            ON CONFLICT (nombre_usuario) DO UPDATE SET
                uuid = EXCLUDED.uuid,
                contraseña = EXCLUDED.contraseña,
                nombre_completo = EXCLUDED.nombre_completo,
                activo = EXCLUDED.activo,
                actualizado_en = EXCLUDED.actualizado_en,
                version = EXCLUDED.version
            """;

        try (PreparedStatement ps = remota.prepareStatement(sql)) {
            for (Usuario u : pendientes) {
                ps.setString(1, u.getUuid().toString());
                ps.setString(2, u.getNombreUsuario());
                ps.setString(3, u.getContraseña());
                ps.setString(4, u.getNombreCompleto());
                ps.setString(5, u.getRol() != null ? u.getRol().name() : null);
                ps.setBoolean(6, u.getActivo() != null && u.getActivo());
                ps.setTimestamp(7, Timestamp.valueOf(u.getCreadoEn()));
                ps.setTimestamp(8, Timestamp.valueOf(u.getActualizadoEn()));
                ps.setInt(9, u.getVersion());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        pendientes.forEach(u -> u.setSincronizadoDesde(SINCRONIZADO));
        usuarioRepository.saveAll(pendientes);
        System.out.println("Usuarios subidos: " + pendientes.size());
    }

    private void sincronizarVentas(Connection remota) throws SQLException {
        List<Venta> pendientes = ventaRepository.findPendientesSincronizacion();

        String sqlVenta = """
            INSERT INTO ventas (uuid, numero_venta, fecha, tipo_venta, metodo_pago, subtotal, descuento, total,
                estado, observaciones, creado_en, actualizado_en, sincronizado_desde, version,
                usuario_id, cliente_id)
            VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'SINCRONIZADO', ?,
                (SELECT id FROM usuarios WHERE uuid = ?::uuid),
                (SELECT id FROM clientes WHERE uuid = ?::uuid))
            ON CONFLICT (uuid) DO UPDATE SET
                estado = EXCLUDED.estado,
                total = EXCLUDED.total,
                actualizado_en = EXCLUDED.actualizado_en,
                version = EXCLUDED.version
            """;

        String sqlDetalle = """
            INSERT INTO detalle_venta (uuid, cantidad, precio_unitario, subtotal, venta_id, producto_id)
            VALUES (?::uuid, ?, ?, ?,
                (SELECT id FROM ventas WHERE uuid = ?::uuid),
                (SELECT id FROM productos WHERE uuid = ?::uuid))
            ON CONFLICT (uuid) DO NOTHING
            """;

        try (PreparedStatement psVenta = remota.prepareStatement(sqlVenta);
             PreparedStatement psDetalle = remota.prepareStatement(sqlDetalle)) {

            for (Venta v : pendientes) {
                psVenta.setString(1, v.getUuid().toString());
                psVenta.setString(2, v.getNumeroVenta());
                psVenta.setTimestamp(3, Timestamp.valueOf(v.getFecha()));
                psVenta.setString(4, v.getTipoVenta() != null ? v.getTipoVenta().name() : null);
                psVenta.setString(5, v.getMetodoPago() != null ? v.getMetodoPago().name() : null);
                psVenta.setBigDecimal(6, v.getSubtotal());
                psVenta.setBigDecimal(7, v.getDescuento());
                psVenta.setBigDecimal(8, v.getTotal());
                psVenta.setString(9, v.getEstado() != null ? v.getEstado().name() : null);
                psVenta.setString(10, v.getObservaciones());
                psVenta.setTimestamp(11, Timestamp.valueOf(v.getCreadoEn()));
                psVenta.setTimestamp(12, Timestamp.valueOf(v.getActualizadoEn()));
                psVenta.setInt(13, v.getVersion());
                psVenta.setString(14, v.getUsuario().getUuid().toString());
                psVenta.setString(15, v.getCliente() != null ? v.getCliente().getUuid().toString() : null);
                psVenta.addBatch();

                for (DetalleVenta d : v.getDetalles()) {
                    psDetalle.setString(1, d.getUuid().toString());
                    psDetalle.setInt(2, d.getCantidad());
                    psDetalle.setBigDecimal(3, d.getPrecioUnitario());
                    psDetalle.setBigDecimal(4, d.getSubtotal());
                    psDetalle.setString(5, v.getUuid().toString());
                    psDetalle.setString(6, d.getProducto().getUuid().toString());
                    psDetalle.addBatch();
                }
            }
            psVenta.executeBatch();
            psDetalle.executeBatch();
        }
        pendientes.forEach(v -> v.setSincronizadoDesde(SINCRONIZADO));
        ventaRepository.saveAll(pendientes);
        System.out.println("Ventas subidas: " + pendientes.size());
    }

    private void sincronizarAbonos(Connection remota) throws SQLException {
        List<Abono> pendientes = abonoRepository.findPendientesSincronizacion();

        String sql = """
            INSERT INTO abonos (uuid, monto, metodo_pago, fecha, observaciones, creado_en, actualizado_en, sincronizado_desde, version, cliente_id, venta_id, usuario_id)
            VALUES (?::uuid, ?, ?, ?, ?, ?, ?, 'SINCRONIZADO', ?,
                (SELECT id FROM clientes WHERE uuid = ?::uuid),
                (SELECT id FROM ventas WHERE uuid = ?::uuid),
                (SELECT id FROM usuarios WHERE uuid = ?::uuid))
            ON CONFLICT (uuid) DO UPDATE SET
                monto = EXCLUDED.monto,
                observaciones = EXCLUDED.observaciones,
                actualizado_en = EXCLUDED.actualizado_en,
                version = EXCLUDED.version
            """;

        try (PreparedStatement ps = remota.prepareStatement(sql)) {
            for (Abono a : pendientes) {
                ps.setString(1, a.getUuid().toString());
                ps.setBigDecimal(2, a.getMonto());
                ps.setString(3, a.getMetodoPago() != null ? a.getMetodoPago().name() : null);
                ps.setTimestamp(4, Timestamp.valueOf(a.getFecha()));
                ps.setString(5, a.getObservaciones());
                ps.setTimestamp(6, Timestamp.valueOf(a.getCreadoEn()));
                ps.setTimestamp(7, Timestamp.valueOf(a.getActualizadoEn()));
                ps.setInt(8, a.getVersion());
                ps.setString(9, a.getCliente().getUuid().toString());
                ps.setString(10, a.getVenta() != null ? a.getVenta().getUuid().toString() : null);
                ps.setString(11, a.getUsuario().getUuid().toString());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        pendientes.forEach(a -> a.setSincronizadoDesde(SINCRONIZADO));
        abonoRepository.saveAll(pendientes);
        System.out.println("Abonos subidos: " + pendientes.size());
    }

    // ==================== SUPABASE → LOCAL ====================

    private void descargarCategorias(Connection remota) throws SQLException {
        String sql = "SELECT uuid, nombre, descripcion, activo, creado_en, actualizado_en, version FROM categorias";

        // 1) Recolectar TODAS las filas remotas en memoria
        List<Map<String, Object>> remotos = new ArrayList<>();
        try (PreparedStatement ps = remota.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("uuid", UUID.fromString(rs.getString("uuid")));
                row.put("nombre", rs.getString("nombre"));
                row.put("descripcion", rs.getString("descripcion"));
                row.put("activo", rs.getBoolean("activo"));
                row.put("creadoEn", rs.getTimestamp("creado_en").toLocalDateTime());
                row.put("actualizadoEn", rs.getTimestamp("actualizado_en").toLocalDateTime());
                row.put("version", rs.getInt("version"));
                remotos.add(row);
            }
        }

        if (remotos.isEmpty()) {
            System.out.println("Categorías → nuevas: 0");
            return;
        }

        // 2) UNA sola query a la BD local
        Set<UUID> uuids = remotos.stream().map(r -> (UUID) r.get("uuid")).collect(Collectors.toSet());
        Set<UUID> existentes = categoriaRepository.findAllByUuidIn(uuids).stream()
                .map(Categoria::getUuid).collect(Collectors.toSet());

        // 3) Insertar solo los que no existen
        int nuevas = 0;
        for (Map<String, Object> r : remotos) {
            UUID uuid = (UUID) r.get("uuid");
            if (existentes.contains(uuid)) continue;

            Categoria c = new Categoria();
            c.setUuid(uuid);
            c.setNombre((String) r.get("nombre"));
            c.setDescripcion((String) r.get("descripcion"));
            c.setActivo((Boolean) r.get("activo"));
            c.setCreadoEn((LocalDateTime) r.get("creadoEn"));
            c.setActualizadoEn((LocalDateTime) r.get("actualizadoEn"));
            c.setVersion((Integer) r.get("version"));
            c.setSincronizadoDesde(SINCRONIZADO);
            categoriaRepository.save(c);
            nuevas++;
        }

        System.out.println("Categorías → nuevas: " + nuevas);
    }

    private void descargarProductos(Connection remota) throws SQLException {
        String sql = """
        SELECT p.uuid, p.nombre, p.descripcion, p.codigo_barras, p.precio_compra, p.precio_venta,
               p.stock_actual, p.stock_minimo, p.activo, p.creado_en, p.actualizado_en, p.version,
               c.uuid as categoria_uuid
        FROM productos p
        LEFT JOIN categorias c ON p.categoria_id = c.id
        """;

        // 1) Recolectar todo lo remoto en memoria
        List<Map<String, Object>> remotos = new ArrayList<>();
        Set<UUID> categoriaUuids = new HashSet<>();
        try (PreparedStatement ps = remota.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("uuid", UUID.fromString(rs.getString("uuid")));
                row.put("nombre", rs.getString("nombre"));
                row.put("descripcion", rs.getString("descripcion"));
                row.put("codigoBarras", rs.getString("codigo_barras"));
                row.put("precioCompra", rs.getBigDecimal("precio_compra"));
                row.put("precioVenta", rs.getBigDecimal("precio_venta"));
                row.put("stockActual", rs.getInt("stock_actual"));
                row.put("stockMinimo", rs.getInt("stock_minimo"));
                row.put("activo", rs.getBoolean("activo"));
                row.put("creadoEn", rs.getTimestamp("creado_en").toLocalDateTime());
                row.put("actualizadoEn", rs.getTimestamp("actualizado_en").toLocalDateTime());
                row.put("version", rs.getInt("version"));

                String catUuid = rs.getString("categoria_uuid");
                if (catUuid != null) {
                    UUID cu = UUID.fromString(catUuid);
                    row.put("categoriaUuid", cu);
                    categoriaUuids.add(cu);
                }
                remotos.add(row);
            }
        }

        if (remotos.isEmpty()) {
            System.out.println("Productos → nuevos: 0");
            return;
        }

        // 2) Cargar productos existentes en UNA query
        Set<UUID> productoUuids = remotos.stream().map(r -> (UUID) r.get("uuid")).collect(Collectors.toSet());
        Set<UUID> existentes = productoRepository.findAllByUuidIn(productoUuids).stream()
                .map(Producto::getUuid).collect(Collectors.toSet());

        // 3) Cargar categorías relacionadas en UNA query (en lugar de N queries)
        Map<UUID, Categoria> mapaCategorias = categoriaRepository.findAllByUuidIn(categoriaUuids).stream()
                .collect(Collectors.toMap(Categoria::getUuid, c -> c));

        // 4) Insertar solo los nuevos usando el mapa en memoria
        int nuevos = 0;
        for (Map<String, Object> r : remotos) {
            UUID uuid = (UUID) r.get("uuid");
            if (existentes.contains(uuid)) continue;

            Producto p = new Producto();
            p.setUuid(uuid);
            p.setNombre((String) r.get("nombre"));
            p.setDescripcion((String) r.get("descripcion"));
            p.setCodigoBarras((String) r.get("codigoBarras"));
            p.setPrecioCompra((BigDecimal) r.get("precioCompra"));
            p.setPrecioVenta((BigDecimal) r.get("precioVenta"));
            p.setStockActual((Integer) r.get("stockActual"));
            p.setStockMinimo((Integer) r.get("stockMinimo"));
            p.setActivo((Boolean) r.get("activo"));
            p.setCreadoEn((LocalDateTime) r.get("creadoEn"));
            p.setActualizadoEn((LocalDateTime) r.get("actualizadoEn"));
            p.setVersion((Integer) r.get("version"));
            p.setSincronizadoDesde(SINCRONIZADO);

            UUID catUuid = (UUID) r.get("categoriaUuid");
            if (catUuid != null) {
                Categoria cat = mapaCategorias.get(catUuid);
                if (cat != null) p.setCategoria(cat);
            }

            productoRepository.save(p);
            nuevos++;
        }

        System.out.println("Productos → nuevos: " + nuevos);
    }
    private void descargarClientes(Connection remota) throws SQLException {
        String sql = """
        SELECT uuid, nombre, apellido, tipo_documento, numero_documento, telefono, email,
               direccion, limite_credito, saldo_deuda, activo, creado_en, actualizado_en, version
        FROM clientes
        """;

        List<Map<String, Object>> remotos = new ArrayList<>();
        try (PreparedStatement ps = remota.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("uuid", UUID.fromString(rs.getString("uuid")));
                row.put("nombre", rs.getString("nombre"));
                row.put("apellido", rs.getString("apellido"));
                row.put("tipoDocumento", rs.getString("tipo_documento"));
                row.put("numeroDocumento", rs.getString("numero_documento"));
                row.put("telefono", rs.getString("telefono"));
                row.put("email", rs.getString("email"));
                row.put("direccion", rs.getString("direccion"));
                row.put("limiteCredito", rs.getBigDecimal("limite_credito"));
                row.put("saldoDeuda", rs.getBigDecimal("saldo_deuda"));
                row.put("activo", rs.getBoolean("activo"));
                row.put("creadoEn", rs.getTimestamp("creado_en").toLocalDateTime());
                row.put("actualizadoEn", rs.getTimestamp("actualizado_en").toLocalDateTime());
                row.put("version", rs.getInt("version"));
                remotos.add(row);
            }
        }

        if (remotos.isEmpty()) {
            System.out.println("Clientes → nuevos: 0");
            return;
        }

        Set<UUID> uuids = remotos.stream().map(r -> (UUID) r.get("uuid")).collect(Collectors.toSet());
        Set<UUID> existentes = clienteRepository.findAllByUuidIn(uuids).stream()
                .map(Cliente::getUuid).collect(Collectors.toSet());

        int nuevos = 0;
        for (Map<String, Object> r : remotos) {
            UUID uuid = (UUID) r.get("uuid");
            if (existentes.contains(uuid)) continue;

            Cliente c = new Cliente();
            c.setUuid(uuid);
            c.setNombre((String) r.get("nombre"));
            c.setApellido((String) r.get("apellido"));
            String tipoDoc = (String) r.get("tipoDocumento");
            if (tipoDoc != null) c.setTipoDocumento(TipoDocumento.valueOf(tipoDoc));
            c.setNumeroDocumento((String) r.get("numeroDocumento"));
            c.setTelefono((String) r.get("telefono"));
            c.setEmail((String) r.get("email"));
            c.setDireccion((String) r.get("direccion"));
            c.setLimiteCredito((BigDecimal) r.get("limiteCredito"));
            c.setSaldoDeuda((BigDecimal) r.get("saldoDeuda"));
            c.setActivo((Boolean) r.get("activo"));
            c.setCreadoEn((LocalDateTime) r.get("creadoEn"));
            c.setActualizadoEn((LocalDateTime) r.get("actualizadoEn"));
            c.setVersion((Integer) r.get("version"));
            c.setSincronizadoDesde(SINCRONIZADO);
            clienteRepository.save(c);
            nuevos++;
        }

        System.out.println("Clientes → nuevos: " + nuevos);
    }

    private void descargarUsuarios(Connection remota) throws SQLException {
        String sql = """
        SELECT uuid, nombre_usuario, contraseña, nombre_completo, rol, activo,
               creado_en, actualizado_en, version
        FROM usuarios
        """;
        int nuevos = 0;
        int actualizados = 0;
        int adoptados = 0;

        try (PreparedStatement ps = remota.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String nombreUsuario = rs.getString("nombre_usuario");
                String contrasena = rs.getString("contraseña");
                String nombreCompleto = rs.getString("nombre_completo");
                String rolStr = rs.getString("rol");
                boolean activo = rs.getBoolean("activo");
                LocalDateTime creadoEn = rs.getTimestamp("creado_en").toLocalDateTime();
                LocalDateTime actualizadoEn = rs.getTimestamp("actualizado_en").toLocalDateTime();
                int version = rs.getInt("version");

                // 1) Buscar por UUID primero
                Optional<Usuario> existentePorUuid = usuarioRepository.findByUuid(uuid);
                if (existentePorUuid.isPresent()) {
                    // Existe localmente con el mismo UUID → actualizar si el remoto es más reciente
                    Usuario local = existentePorUuid.get();
                    if (local.getActualizadoEn() == null || actualizadoEn.isAfter(local.getActualizadoEn())) {
                        local.setNombreCompleto(nombreCompleto);
                        if (rolStr != null) local.setRol(Rol.valueOf(rolStr));
                        local.setActivo(activo);
                        local.setActualizadoEn(actualizadoEn);
                        local.setVersion(version);
                        local.setSincronizadoDesde(SINCRONIZADO);
                        usuarioRepository.save(local);
                        actualizados++;
                    }
                    continue;
                }

                // 2) No existe por UUID → revisar si hay colisión por nombre_usuario
                Optional<Usuario> existentePorNombre = usuarioRepository.findByNombreUsuario(nombreUsuario);
                if (existentePorNombre.isPresent()) {
                    // Hay un usuario local con el mismo nombre pero UUID distinto.
                    // Adoptamos el UUID remoto para alinear ambas BD.
                    Usuario local = existentePorNombre.get();
                    local.setUuid(uuid);
                    local.setNombreCompleto(nombreCompleto);
                    if (rolStr != null) local.setRol(Rol.valueOf(rolStr));
                    local.setActivo(activo);
                    local.setActualizadoEn(actualizadoEn);
                    local.setVersion(version);
                    local.setSincronizadoDesde(SINCRONIZADO);
                    usuarioRepository.save(local);
                    adoptados++;
                    continue;
                }

                // 3) No existe ni por UUID ni por nombre → crear nuevo
                Usuario u = new Usuario();
                u.setUuid(uuid);
                u.setNombreUsuario(nombreUsuario);
                u.setContraseña(contrasena);
                u.setNombreCompleto(nombreCompleto);
                if (rolStr != null) u.setRol(Rol.valueOf(rolStr));
                u.setActivo(activo);
                u.setCreadoEn(creadoEn);
                u.setActualizadoEn(actualizadoEn);
                u.setVersion(version);
                u.setSincronizadoDesde(SINCRONIZADO);
                usuarioRepository.save(u);
                nuevos++;
            }
        }

        System.out.println("Usuarios → nuevos: " + nuevos
                + ", actualizados: " + actualizados
                + ", adoptados: " + adoptados);
    }

    private void descargarVentas(Connection remota) throws SQLException {
        String sqlVentas = """
            SELECT v.uuid, v.numero_venta, v.fecha, v.tipo_venta, v.metodo_pago,
                   v.subtotal, v.descuento, v.total, v.estado, v.observaciones,
                   v.creado_en, v.actualizado_en, v.version,
                   u.uuid as usuario_uuid, c.uuid as cliente_uuid
            FROM ventas v
            JOIN usuarios u ON v.usuario_id = u.id
            LEFT JOIN clientes c ON v.cliente_id = c.id
            """;

        String sqlDetalles = """
            SELECT dv.uuid, dv.cantidad, dv.precio_unitario, dv.subtotal, p.uuid as producto_uuid
            FROM detalle_venta dv
            JOIN productos p ON dv.producto_id = p.id
            JOIN ventas v ON dv.venta_id = v.id
            WHERE v.uuid = ?::uuid
            """;

        int nuevas = 0;
        try (PreparedStatement psVentas = remota.prepareStatement(sqlVentas);
             ResultSet rs = psVentas.executeQuery()) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                if (ventaRepository.findByUuid(uuid).isEmpty()) {
                    Venta v = new Venta();
                    v.setUuid(uuid);
                    v.setNumeroVenta(rs.getString("numero_venta"));
                    v.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    String tipoVenta = rs.getString("tipo_venta");
                    if (tipoVenta != null) v.setTipoVenta(TipoVenta.valueOf(tipoVenta));
                    String metodoPago = rs.getString("metodo_pago");
                    if (metodoPago != null) v.setMetodoPago(MetodoPago.valueOf(metodoPago));
                    v.setSubtotal(rs.getBigDecimal("subtotal"));
                    v.setDescuento(rs.getBigDecimal("descuento"));
                    v.setTotal(rs.getBigDecimal("total"));
                    String estado = rs.getString("estado");
                    if (estado != null) v.setEstado(EstadoVenta.valueOf(estado));
                    v.setObservaciones(rs.getString("observaciones"));
                    v.setCreadoEn(rs.getTimestamp("creado_en").toLocalDateTime());
                    v.setActualizadoEn(rs.getTimestamp("actualizado_en").toLocalDateTime());
                    v.setVersion(rs.getInt("version"));
                    v.setSincronizadoDesde(SINCRONIZADO);

                    String usuarioUuid = rs.getString("usuario_uuid");
                    usuarioRepository.findByUuid(UUID.fromString(usuarioUuid))
                            .ifPresent(v::setUsuario);

                    String clienteUuid = rs.getString("cliente_uuid");
                    if (clienteUuid != null) {
                        clienteRepository.findByUuid(UUID.fromString(clienteUuid))
                                .ifPresent(v::setCliente);
                    }

                    // Descargar detalles
                    try (PreparedStatement psDetalles = remota.prepareStatement(sqlDetalles)) {
                        psDetalles.setString(1, uuid.toString());
                        try (ResultSet rsD = psDetalles.executeQuery()) {
                            while (rsD.next()) {
                                DetalleVenta d = new DetalleVenta();
                                d.setUuid(UUID.fromString(rsD.getString("uuid")));
                                d.setCantidad(rsD.getInt("cantidad"));
                                d.setPrecioUnitario(rsD.getBigDecimal("precio_unitario"));
                                d.setSubtotal(rsD.getBigDecimal("subtotal"));
                                d.setVenta(v);
                                String prodUuid = rsD.getString("producto_uuid");
                                productoRepository.findByUuid(UUID.fromString(prodUuid))
                                        .ifPresent(d::setProducto);
                                v.getDetalles().add(d);
                            }
                        }
                    }
                    ventaRepository.save(v);
                    nuevas++;
                }
            }
        }
        System.out.println("Ventas descargadas de Supabase: " + nuevas);
    }

    private void descargarAbonos(Connection remota) throws SQLException {
        String sql = """
        SELECT a.uuid, a.monto, a.metodo_pago, a.fecha, a.observaciones,
               a.creado_en, a.actualizado_en, a.version,
               c.uuid as cliente_uuid, v.uuid as venta_uuid, u.uuid as usuario_uuid
        FROM abonos a
        JOIN clientes c ON a.cliente_id = c.id
        LEFT JOIN ventas v ON a.venta_id = v.id
        JOIN usuarios u ON a.usuario_id = u.id
        """;
        int nuevos = 0;
        try (PreparedStatement ps = remota.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                if (abonoRepository.findByUuid(uuid).isEmpty()) {
                    Abono a = new Abono();
                    a.setUuid(uuid);
                    a.setMonto(rs.getBigDecimal("monto"));
                    String metodoPago = rs.getString("metodo_pago");
                    if (metodoPago != null) a.setMetodoPago(MetodoPago.valueOf(metodoPago));
                    a.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    a.setObservaciones(rs.getString("observaciones"));
                    a.setCreadoEn(rs.getTimestamp("creado_en").toLocalDateTime());
                    a.setActualizadoEn(rs.getTimestamp("actualizado_en").toLocalDateTime());
                    a.setVersion(rs.getInt("version"));
                    a.setSincronizadoDesde(SINCRONIZADO);

                    clienteRepository.findByUuid(UUID.fromString(rs.getString("cliente_uuid")))
                            .ifPresent(a::setCliente);

                    String ventaUuid = rs.getString("venta_uuid");
                    if (ventaUuid != null) {
                        ventaRepository.findByUuid(UUID.fromString(ventaUuid))
                                .ifPresent(a::setVenta);
                    }

                    usuarioRepository.findByUuid(UUID.fromString(rs.getString("usuario_uuid")))
                            .ifPresent(a::setUsuario);

                    abonoRepository.save(a);
                    nuevos++;
                }
            }
        }
        System.out.println("Abonos descargados de Supabase: " + nuevos);
    }
}