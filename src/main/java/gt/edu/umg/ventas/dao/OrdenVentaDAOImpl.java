package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrdenVentaDAOImpl implements OrdenVentaDAO {

    @Override
    public void crear(OrdenVenta o) {
        String sql = "INSERT INTO dbo.OrdenVenta (numero_orden, fecha, id_cliente, id_usuario, total, estado, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, o.getNumeroOrden());
                    stmt.setTimestamp(2, Timestamp.valueOf(o.getFecha()));
                    stmt.setLong(3, o.getCliente().getIdCliente());
                    stmt.setLong(4, o.getUsuario().getIdUsuario());
                    stmt.setBigDecimal(5, o.getTotal());
                    stmt.setString(6, o.getEstado().name());
                    stmt.setString(7, o.getObservaciones());
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            o.setId(rs.getInt(1));
                        }
                    }
                }

                String sqlDet = "INSERT INTO dbo.DetalleOrdenVenta (id_orden, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmtDet = conn.prepareStatement(sqlDet, Statement.RETURN_GENERATED_KEYS)) {
                    for (DetalleOrdenVenta det : o.getDetalles()) {
                        stmtDet.setInt(1, o.getId());
                        stmtDet.setLong(2, det.getProducto().getIdProducto());
                        stmtDet.setInt(3, det.getCantidad());
                        stmtDet.setBigDecimal(4, det.getPrecioUnitario());
                        stmtDet.setBigDecimal(5, det.getSubtotal());
                        stmtDet.addBatch();
                    }
                    stmtDet.executeBatch();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Error al guardar la orden de venta: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión al crear orden de venta: " + e.getMessage(), e);
        }
    }

    @Override
    public OrdenVenta obtener(int id) {
        String sql = "SELECT o.id, o.numero_orden, o.fecha, o.total, o.estado, o.observaciones, "
                + "c.id_cliente, c.nit, c.nombre AS cliente_nombre, c.direccion, c.telefono, c.correo, "
                + "u.id_usuario, u.nombre AS usuario_nombre, u.nombre_usuario, u.rol, u.activo "
                + "FROM dbo.OrdenVenta o "
                + "INNER JOIN dbo.cliente c ON o.id_cliente = c.id_cliente "
                + "INNER JOIN dbo.usuario u ON o.id_usuario = u.id_usuario "
                + "WHERE o.id = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    OrdenVenta o = new OrdenVenta();
                    o.setId(rs.getInt("id"));
                    o.setNumeroOrden(rs.getString("numero_orden"));
                    o.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    o.setTotal(rs.getBigDecimal("total"));
                    o.setEstado(EstadoOrdenVenta.valueOf(rs.getString("estado")));
                    o.setObservaciones(rs.getString("observaciones"));

                    Cliente c = new Cliente(
                            rs.getLong("id_cliente"),
                            rs.getString("nit"),
                            rs.getString("cliente_nombre"),
                            rs.getString("direccion"),
                            rs.getString("telefono"),
                            rs.getString("correo")
                    );
                    o.setCliente(c);

                    Usuario u = new Usuario(
                            rs.getLong("id_usuario"),
                            rs.getString("usuario_nombre"),
                            rs.getString("nombre_usuario"),
                            rs.getString("rol"),
                            rs.getBoolean("activo")
                    );
                    o.setUsuario(u);

                    cargarDetalles(conn, o);
                    return o;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener orden de venta: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<OrdenVenta> obtenerTodos() {
        return obtenerPorEstado(null);
    }

    @Override
    public List<OrdenVenta> obtenerPendientes() {
        return obtenerPorEstado(EstadoOrdenVenta.PENDIENTE);
    }

    private List<OrdenVenta> obtenerPorEstado(EstadoOrdenVenta filtroEstado) {
        List<OrdenVenta> lista = new ArrayList<>();
        String sql = "SELECT o.id, o.numero_orden, o.fecha, o.total, o.estado, o.observaciones, "
                + "c.id_cliente, c.nit, c.nombre AS cliente_nombre, c.direccion, c.telefono, c.correo, "
                + "u.id_usuario, u.nombre AS usuario_nombre, u.nombre_usuario, u.rol, u.activo "
                + "FROM dbo.OrdenVenta o "
                + "INNER JOIN dbo.cliente c ON o.id_cliente = c.id_cliente "
                + "INNER JOIN dbo.usuario u ON o.id_usuario = u.id_usuario "
                + (filtroEstado != null ? "WHERE o.estado = ? " : "")
                + "ORDER BY o.id DESC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (filtroEstado != null) {
                stmt.setString(1, filtroEstado.name());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrdenVenta o = new OrdenVenta();
                    o.setId(rs.getInt("id"));
                    o.setNumeroOrden(rs.getString("numero_orden"));
                    o.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    o.setTotal(rs.getBigDecimal("total"));
                    o.setEstado(EstadoOrdenVenta.valueOf(rs.getString("estado")));
                    o.setObservaciones(rs.getString("observaciones"));

                    Cliente c = new Cliente(
                            rs.getLong("id_cliente"),
                            rs.getString("nit"),
                            rs.getString("cliente_nombre"),
                            rs.getString("direccion"),
                            rs.getString("telefono"),
                            rs.getString("correo")
                    );
                    o.setCliente(c);

                    Usuario u = new Usuario(
                            rs.getLong("id_usuario"),
                            rs.getString("usuario_nombre"),
                            rs.getString("nombre_usuario"),
                            rs.getString("rol"),
                            rs.getBoolean("activo")
                    );
                    o.setUsuario(u);

                    cargarDetalles(conn, o);
                    lista.add(o);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar órdenes de venta: " + e.getMessage(), e);
        }
        return lista;
    }

    private void cargarDetalles(Connection conn, OrdenVenta o) throws SQLException {
        String sql = "SELECT d.id, d.id_orden, d.cantidad, d.precio_unitario, d.subtotal, "
                + "p.id_producto, p.codigo, p.nombre AS prod_nombre, p.descripcion, p.precio_venta, p.activo, "
                + "c.id_categoria, c.nombre AS cat_nombre, c.descripcion AS cat_desc, c.activa AS cat_activa "
                + "FROM dbo.DetalleOrdenVenta d "
                + "INNER JOIN dbo.producto p ON d.id_producto = p.id_producto "
                + "INNER JOIN dbo.categoria c ON p.categoria_id = c.id_categoria "
                + "WHERE d.id_orden = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, o.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Categoria cat = new Categoria(
                            rs.getLong("id_categoria"),
                            rs.getString("cat_nombre"),
                            rs.getString("cat_desc"),
                            rs.getBoolean("cat_activa")
                    );
                    Producto prod = new Producto(
                            rs.getLong("id_producto"),
                            rs.getString("codigo"),
                            rs.getString("prod_nombre"),
                            rs.getString("descripcion"),
                            rs.getBigDecimal("precio_venta"),
                            rs.getBoolean("activo"),
                            cat
                    );

                    DetalleOrdenVenta det = new DetalleOrdenVenta();
                    det.setId(rs.getInt("id"));
                    det.setOrden(o);
                    det.setProducto(prod);
                    det.setCantidad(rs.getInt("cantidad"));
                    det.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    det.setSubtotal(rs.getBigDecimal("subtotal"));

                    o.getDetalles().add(det);
                }
            }
        }
    }

    @Override
    public void actualizar(OrdenVenta o) {
        String sql = "UPDATE dbo.OrdenVenta SET estado = ?, observaciones = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, o.getEstado().name());
            stmt.setString(2, o.getObservaciones());
            stmt.setInt(3, o.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar orden de venta: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM dbo.OrdenVenta WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar orden de venta: " + e.getMessage(), e);
        }
    }

    @Override
    public List<OrdenVentaResumen> buscarPorFecha(LocalDateTime desde, LocalDateTime hasta) {
        List<OrdenVentaResumen> lista = new ArrayList<>();
        String sql = "SELECT o.id, o.numero_orden, o.fecha, c.nombre AS cliente, o.estado, o.total, "
                + "ISNULL(d.estado, 'Pendiente') AS estado_despacho, "
                + "ISNULL(b.nombre, '-') AS bodega "
                + "FROM dbo.OrdenVenta o "
                + "INNER JOIN dbo.cliente c ON o.id_cliente = c.id_cliente "
                + "LEFT JOIN dbo.Despacho d ON d.id_orden = o.id "
                + "LEFT JOIN dbo.Bodega b ON d.id_bodega = b.id "
                + "WHERE o.fecha >= ? AND o.fecha < ? "
                + "ORDER BY o.fecha DESC";

        LocalDateTime hastaFin = hasta.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime desdeInicio = desde.withHour(0).withMinute(0).withSecond(0).withNano(0);

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(desdeInicio));
            stmt.setTimestamp(2, Timestamp.valueOf(hastaFin));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrdenVentaResumen r = new OrdenVentaResumen();
                    r.setId(rs.getInt("id"));
                    r.setNumeroOrden(rs.getString("numero_orden"));
                    r.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    r.setNombreCliente(rs.getString("cliente"));
                    r.setEstado(EstadoOrdenVenta.valueOf(rs.getString("estado")));
                    r.setTotal(rs.getBigDecimal("total"));
                    r.setEstadoDespacho(rs.getString("estado_despacho"));
                    r.setNombreBodega(rs.getString("bodega"));
                    lista.add(r);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error en consulta de fechas: " + e.getMessage(), e);
        }
        return lista;
    }
}
