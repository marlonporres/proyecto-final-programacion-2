package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.*;
import java.sql.*;
import java.util.*;

/** Persistencia de facturas inmutables y pagos, con confirmación transaccional. */
public class FacturaDAOImpl implements FacturaDAO {
    private static final String SELECT = "SELECT f.*, o.numero_orden, o.estado AS estado_orden, "
            + "c.nit, c.nombre AS cliente_nombre, c.direccion, c.telefono, c.correo, "
            + "u.nombre AS usuario_nombre, u.nombre_usuario, u.rol, u.activo "
            + "FROM dbo.factura f JOIN dbo.OrdenVenta o ON o.id = f.id_orden "
            + "JOIN dbo.cliente c ON c.id_cliente = f.cliente_id "
            + "JOIN dbo.usuario u ON u.id_usuario = f.usuario_id ";

    @Override
    public void guardar(Factura f) {
        if (f == null || f.getOrden() == null || f.getOrden().getId() == null
                || f.getUsuario() == null || f.getCliente() == null || f.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La factura debe tener orden, usuario, cliente y detalles.");
        }
        long idAnterior = f.getIdFactura();
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT o.total, o.id_cliente FROM dbo.OrdenVenta o WITH (UPDLOCK, HOLDLOCK) "
                        + "WHERE o.id = ? AND o.estado = 'COMPLETADA' "
                        + "AND EXISTS (SELECT 1 FROM dbo.Despacho d WHERE d.id_orden = o.id AND d.estado = 'CONFIRMADO') "
                        + "AND NOT EXISTS (SELECT 1 FROM dbo.factura f WHERE f.id_orden = o.id)")) {
                    ps.setInt(1, f.getOrden().getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalStateException("La orden no es facturable o ya tiene factura.");
                        if (rs.getBigDecimal("total").compareTo(f.calcularTotal()) != 0
                                || rs.getLong("id_cliente") != f.getCliente().getIdCliente()) {
                            throw new IllegalStateException("La factura no coincide con la orden.");
                        }
                    }
                }
                validarDetalles(con, f);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO dbo.factura (numero, fecha_hora, estado, observaciones, cliente_id, usuario_id, id_orden, total) "
                        + "VALUES (?, ?, 'EMITIDA', ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, f.getNumero());
                    ps.setTimestamp(2, Timestamp.valueOf(f.getFechaHora()));
                    ps.setString(3, f.getObservaciones());
                    ps.setLong(4, f.getCliente().getIdCliente());
                    ps.setLong(5, f.getUsuario().getIdUsuario());
                    ps.setInt(6, f.getOrden().getId());
                    ps.setBigDecimal(7, f.calcularTotal());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("No se obtuvo el ID de factura.");
                        f.setIdFactura(rs.getLong(1));
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO dbo.detalle_factura (factura_id, producto_id, cantidad, precio_unitario, porcentaje_impuesto, descuento, subtotal) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    for (DetalleFactura d : f.getDetalles()) {
                        ps.setLong(1, f.getIdFactura());
                        ps.setLong(2, d.getProducto().getIdProducto());
                        ps.setBigDecimal(3, d.getCantidad());
                        ps.setBigDecimal(4, d.getPrecioUnitario());
                        ps.setBigDecimal(5, d.getPorcentajeImpuesto());
                        ps.setBigDecimal(6, d.getDescuento());
                        ps.setBigDecimal(7, d.calcularSubtotal());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                con.commit();
                f.setEstado(EstadoFactura.EMITIDA);
            } catch (Exception e) {
                con.rollback();
                f.setIdFactura(idAnterior);
                throw new IllegalStateException("No se pudo guardar la factura: " + e.getMessage(), e);
            }
        } catch (SQLException e) { throw new IllegalStateException("Error de conexión al facturar.", e); }
    }

    private void validarDetalles(Connection con, Factura f) throws SQLException {
        Map<Long, DetalleFactura> detalles = new HashMap<>();
        for (DetalleFactura d : f.getDetalles()) {
            if (detalles.put(d.getProducto().getIdProducto(), d) != null) {
                throw new IllegalArgumentException("Producto repetido en factura.");
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_producto, cantidad, precio_unitario FROM dbo.DetalleOrdenVenta WHERE id_orden = ?")) {
            ps.setInt(1, f.getOrden().getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleFactura d = detalles.remove(rs.getLong("id_producto"));
                    if (d == null || d.getCantidad().compareTo(rs.getBigDecimal("cantidad")) != 0
                            || d.getPrecioUnitario().compareTo(rs.getBigDecimal("precio_unitario")) != 0
                            || d.getDescuento().signum() != 0
                            || d.getPorcentajeImpuesto().compareTo(new java.math.BigDecimal("12.00")) != 0) {
                        throw new IllegalStateException("Los detalles de factura deben coincidir con la orden.");
                    }
                }
            }
        }
        if (!detalles.isEmpty()) throw new IllegalStateException("Productos ajenos a la orden.");
    }

    @Override public Factura buscarPorNumero(String numero) {
        return buscar(SELECT + "WHERE f.numero = ?", numero);
    }
    @Override public Factura buscarPorOrden(int idOrden) {
        return buscar(SELECT + "WHERE f.id_orden = ?", idOrden);
    }

    private Factura buscar(String sql, Object valor) {
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Factura f = mapear(rs);
                cargarDetallesYPagos(con, f);
                return f;
            }
        } catch (SQLException e) { throw new IllegalStateException("No se pudo consultar la factura.", e); }
    }

    @Override public List<Factura> listar() {
        List<Factura> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SELECT + "ORDER BY f.id_factura DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Factura f = mapear(rs);
                cargarDetallesYPagos(con, f);
                lista.add(f);
            }
            return lista;
        } catch (SQLException e) { throw new IllegalStateException("No se pudieron listar facturas.", e); }
    }

    @Override public void actualizar(Factura f) {
        if (f == null || f.getIdFactura() <= 0 || f.getPagos().isEmpty()) {
            throw new IllegalArgumentException("Debe registrar un pago en una factura guardada.");
        }
        Pago nuevo = f.getPagos().get(f.getPagos().size() - 1);
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT estado, total FROM dbo.factura WITH (UPDLOCK, HOLDLOCK) WHERE id_factura = ?")) {
                    ps.setLong(1, f.getIdFactura());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !"EMITIDA".equals(rs.getString("estado"))) {
                            throw new IllegalStateException("La factura ya no admite pagos. Vuelva a consultarla.");
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*) AS n, COALESCE(SUM(monto), 0) AS pagado FROM dbo.pago WHERE factura_id = ?")) {
                    ps.setLong(1, f.getIdFactura());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt("n") != f.getPagos().size() - 1
                                || rs.getBigDecimal("pagado").compareTo(f.obtenerTotalPagado().subtract(nuevo.getMonto())) != 0) {
                            throw new IllegalStateException("Los pagos cambiaron. Vuelva a consultar la factura.");
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO dbo.pago (factura_id, fecha_hora, monto, metodo, referencia) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setLong(1, f.getIdFactura());
                    ps.setTimestamp(2, Timestamp.valueOf(nuevo.getFechaHora()));
                    ps.setBigDecimal(3, nuevo.getMonto());
                    ps.setString(4, nuevo.getMetodo().name());
                    ps.setString(5, nuevo.getReferencia());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE dbo.factura SET estado = CASE WHEN (SELECT SUM(monto) FROM dbo.pago WHERE factura_id = ?) >= total "
                        + "THEN 'PAGADA' ELSE 'EMITIDA' END WHERE id_factura = ?")) {
                    ps.setLong(1, f.getIdFactura());
                    ps.setLong(2, f.getIdFactura());
                    ps.executeUpdate();
                }
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw new IllegalStateException("No se pudo registrar el pago: " + e.getMessage(), e);
            }
        } catch (SQLException e) { throw new IllegalStateException("Error de conexión al registrar pago.", e); }
    }

    @Override public void anular(long id) {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE dbo.factura SET estado = 'ANULADA' WHERE id_factura = ? AND estado IN ('EMITIDA', 'PAGADA')")) {
            ps.setLong(1, id);
            if (ps.executeUpdate() != 1) throw new IllegalStateException("La factura no puede anularse.");
        } catch (SQLException e) { throw new IllegalStateException("No se pudo anular la factura.", e); }
    }

    private Factura mapear(ResultSet rs) throws SQLException {
        Factura f = new Factura();
        f.setIdFactura(rs.getLong("id_factura"));
        f.setNumero(rs.getString("numero"));
        f.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
        f.setEstado(EstadoFactura.valueOf(rs.getString("estado")));
        f.setObservaciones(rs.getString("observaciones"));
        f.setCliente(new Cliente(rs.getLong("cliente_id"), rs.getString("nit"), rs.getString("cliente_nombre"),
                rs.getString("direccion"), rs.getString("telefono"), rs.getString("correo")));
        f.setUsuario(new Usuario(rs.getLong("usuario_id"), rs.getString("usuario_nombre"), rs.getString("nombre_usuario"),
                rs.getString("rol"), rs.getBoolean("activo")));
        OrdenVenta o = new OrdenVenta();
        o.setId(rs.getInt("id_orden"));
        o.setNumeroOrden(rs.getString("numero_orden"));
        o.setEstado(EstadoOrdenVenta.valueOf(rs.getString("estado_orden")));
        o.setCliente(f.getCliente());
        f.setOrden(o);
        return f;
    }

    private void cargarDetallesYPagos(Connection con, Factura f) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT d.*, p.codigo, p.nombre FROM dbo.detalle_factura d "
                + "JOIN dbo.producto p ON p.id_producto = d.producto_id WHERE d.factura_id = ? ORDER BY d.id_detalle")) {
            ps.setLong(1, f.getIdFactura());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto p = new Producto();
                    p.setIdProducto(rs.getLong("producto_id")); p.setCodigo(rs.getString("codigo")); p.setNombre(rs.getString("nombre"));
                    f.agregarDetalleDirecto(new DetalleFactura(rs.getLong("id_detalle"), p, rs.getBigDecimal("cantidad"),
                            rs.getBigDecimal("precio_unitario"), rs.getBigDecimal("porcentaje_impuesto"), rs.getBigDecimal("descuento")));
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM dbo.pago WHERE factura_id = ? ORDER BY id_pago")) {
            ps.setLong(1, f.getIdFactura());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) f.agregarPagoDirecto(new Pago(rs.getLong("id_pago"), rs.getTimestamp("fecha_hora").toLocalDateTime(),
                        rs.getBigDecimal("monto"), MetodoPago.valueOf(rs.getString("metodo")), rs.getString("referencia")));
            }
        }
    }
}


