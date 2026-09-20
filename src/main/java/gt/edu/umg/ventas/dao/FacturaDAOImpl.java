package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.Categoria;
import gt.edu.umg.ventas.modelo.Cliente;
import gt.edu.umg.ventas.modelo.DetalleFactura;
import gt.edu.umg.ventas.modelo.EstadoFactura;
import gt.edu.umg.ventas.modelo.Factura;
import gt.edu.umg.ventas.modelo.MetodoPago;
import gt.edu.umg.ventas.modelo.Pago;
import gt.edu.umg.ventas.modelo.Producto;
import gt.edu.umg.ventas.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC de FacturaDAO con soporte para transacciones y SQL Server.
 */
public class FacturaDAOImpl implements FacturaDAO {

    private final ConexionBD conexion;

    public FacturaDAOImpl() {
        this.conexion = new ConexionBD();
    }

    public FacturaDAOImpl(ConexionBD conexion) {
        this.conexion = conexion != null ? conexion : new ConexionBD();
    }

    @Override
    public void guardar(Factura factura) {
        if (factura == null) {
            throw new IllegalArgumentException("La factura a guardar no puede ser nula.");
        }

        String sqlFactura = "INSERT INTO dbo.factura (numero, fecha_hora, estado, observaciones, cliente_id, usuario_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO dbo.detalle_factura (factura_id, producto_id, cantidad, precio_unitario, "
                + "porcentaje_impuesto, descuento, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlPago = "INSERT INTO dbo.pago (factura_id, fecha_hora, monto, metodo, referencia) "
                + "VALUES (?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = conexion.obtenerConexion();
            con.setAutoCommit(false);

            // 1. Guardar encabezado de la factura
            try (PreparedStatement psF = con.prepareStatement(sqlFactura, Statement.RETURN_GENERATED_KEYS)) {
                psF.setString(1, factura.getNumero());
                psF.setTimestamp(2, Timestamp.valueOf(factura.getFechaHora()));
                psF.setString(3, factura.getEstado().name());
                psF.setString(4, factura.getObservaciones());

                if (factura.getCliente() != null && factura.getCliente().getIdCliente() > 0) {
                    psF.setLong(5, factura.getCliente().getIdCliente());
                } else {
                    psF.setNull(5, Types.BIGINT);
                }

                if (factura.getUsuario() != null && factura.getUsuario().getIdUsuario() > 0) {
                    psF.setLong(6, factura.getUsuario().getIdUsuario());
                } else {
                    psF.setLong(6, 1L); // Usuario por defecto (admin) si no viene seteado
                }

                psF.executeUpdate();

                try (ResultSet rs = psF.getGeneratedKeys()) {
                    if (rs.next()) {
                        factura.setIdFactura(rs.getLong(1));
                    }
                }
            }

            // 2. Guardar líneas de detalle
            if (!factura.getDetalles().isEmpty()) {
                try (PreparedStatement psD = con.prepareStatement(sqlDetalle, Statement.RETURN_GENERATED_KEYS)) {
                    for (DetalleFactura det : factura.getDetalles()) {
                        psD.setLong(1, factura.getIdFactura());
                        psD.setLong(2, det.getProducto().getIdProducto());
                        psD.setBigDecimal(3, det.getCantidad());
                        psD.setBigDecimal(4, det.getPrecioUnitario());
                        psD.setBigDecimal(5, det.getPorcentajeImpuesto());
                        psD.setBigDecimal(6, det.getDescuento());
                        psD.setBigDecimal(7, det.calcularSubtotal());
                        psD.addBatch();
                    }
                    psD.executeBatch();
                }
            }

            // 3. Guardar pagos asociados
            if (!factura.getPagos().isEmpty()) {
                try (PreparedStatement psP = con.prepareStatement(sqlPago, Statement.RETURN_GENERATED_KEYS)) {
                    for (Pago p : factura.getPagos()) {
                        psP.setLong(1, factura.getIdFactura());
                        psP.setTimestamp(2, Timestamp.valueOf(p.getFechaHora()));
                        psP.setBigDecimal(3, p.getMonto());
                        psP.setString(4, p.getMetodo().name());
                        psP.setString(5, p.getReferencia());
                        psP.addBatch();
                    }
                    psP.executeBatch();
                }
            }

            con.commit();
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error en rollback: " + ex.getMessage());
                }
            }
            throw new RuntimeException("Error al persistir la factura en SQL Server: " + e.getMessage(), e);
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public Factura buscarPorNumero(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new RuntimeException("Factura no encontrada");
        }

        String sql = "SELECT f.id_factura, f.numero, f.fecha_hora, f.estado, f.observaciones, "
                + "c.id_cliente, c.nit, c.nombre AS cliente_nombre, c.direccion, c.telefono, c.correo, "
                + "u.id_usuario, u.nombre AS usuario_nombre, u.nombre_usuario, u.rol, u.activo "
                + "FROM dbo.factura f "
                + "LEFT JOIN dbo.cliente c ON f.cliente_id = c.id_cliente "
                + "INNER JOIN dbo.usuario u ON f.usuario_id = u.id_usuario "
                + "WHERE f.numero = ?";

        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, numero.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Factura factura = mapearEncabezado(rs);
                    cargarDetallesYPagos(con, factura);
                    return factura;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar factura por número: " + e.getMessage(), e);
        }
        throw new RuntimeException("Factura no encontrada");
    }

    @Override
    public List<Factura> listar() {
        List<Factura> lista = new ArrayList<>();
        String sql = "SELECT f.id_factura, f.numero, f.fecha_hora, f.estado, f.observaciones, "
                + "c.id_cliente, c.nit, c.nombre AS cliente_nombre, c.direccion, c.telefono, c.correo, "
                + "u.id_usuario, u.nombre AS usuario_nombre, u.nombre_usuario, u.rol, u.activo "
                + "FROM dbo.factura f "
                + "LEFT JOIN dbo.cliente c ON f.cliente_id = c.id_cliente "
                + "INNER JOIN dbo.usuario u ON f.usuario_id = u.id_usuario "
                + "ORDER BY f.id_factura DESC";

        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Factura f = mapearEncabezado(rs);
                cargarDetallesYPagos(con, f);
                lista.add(f);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar facturas: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public void actualizar(Factura factura) {
        if (factura == null || factura.getIdFactura() <= 0) {
            throw new IllegalArgumentException("Factura no válida para actualización.");
        }

        String sqlFactura = "UPDATE dbo.factura SET estado = ?, observaciones = ?, cliente_id = ? "
                + "WHERE id_factura = ?";
        String sqlEliminarDetalles = "DELETE FROM dbo.detalle_factura WHERE factura_id = ?";
        String sqlDetalle = "INSERT INTO dbo.detalle_factura (factura_id, producto_id, cantidad, precio_unitario, "
                + "porcentaje_impuesto, descuento, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlEliminarPagos = "DELETE FROM dbo.pago WHERE factura_id = ?";
        String sqlPago = "INSERT INTO dbo.pago (factura_id, fecha_hora, monto, metodo, referencia) "
                + "VALUES (?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = conexion.obtenerConexion();
            con.setAutoCommit(false);

            try (PreparedStatement psF = con.prepareStatement(sqlFactura)) {
                psF.setString(1, factura.getEstado().name());
                psF.setString(2, factura.getObservaciones());
                if (factura.getCliente() != null && factura.getCliente().getIdCliente() > 0) {
                    psF.setLong(3, factura.getCliente().getIdCliente());
                } else {
                    psF.setNull(3, Types.BIGINT);
                }
                psF.setLong(4, factura.getIdFactura());
                psF.executeUpdate();
            }

            // Actualizar detalles
            try (PreparedStatement psDelDet = con.prepareStatement(sqlEliminarDetalles)) {
                psDelDet.setLong(1, factura.getIdFactura());
                psDelDet.executeUpdate();
            }
            if (!factura.getDetalles().isEmpty()) {
                try (PreparedStatement psD = con.prepareStatement(sqlDetalle)) {
                    for (DetalleFactura det : factura.getDetalles()) {
                        psD.setLong(1, factura.getIdFactura());
                        psD.setLong(2, det.getProducto().getIdProducto());
                        psD.setBigDecimal(3, det.getCantidad());
                        psD.setBigDecimal(4, det.getPrecioUnitario());
                        psD.setBigDecimal(5, det.getPorcentajeImpuesto());
                        psD.setBigDecimal(6, det.getDescuento());
                        psD.setBigDecimal(7, det.calcularSubtotal());
                        psD.addBatch();
                    }
                    psD.executeBatch();
                }
            }

            // Actualizar pagos
            try (PreparedStatement psDelP = con.prepareStatement(sqlEliminarPagos)) {
                psDelP.setLong(1, factura.getIdFactura());
                psDelP.executeUpdate();
            }
            if (!factura.getPagos().isEmpty()) {
                try (PreparedStatement psP = con.prepareStatement(sqlPago)) {
                    for (Pago p : factura.getPagos()) {
                        psP.setLong(1, factura.getIdFactura());
                        psP.setTimestamp(2, Timestamp.valueOf(p.getFechaHora()));
                        psP.setBigDecimal(3, p.getMonto());
                        psP.setString(4, p.getMetodo().name());
                        psP.setString(5, p.getReferencia());
                        psP.addBatch();
                    }
                    psP.executeBatch();
                }
            }

            con.commit();
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error en rollback: " + ex.getMessage());
                }
            }
            throw new RuntimeException("Error al actualizar la factura: " + e.getMessage(), e);
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void anular(long id) {
        String sql = "UPDATE dbo.factura SET estado = 'ANULADA' WHERE id_factura = ?";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al anular factura: " + e.getMessage(), e);
        }
    }

    private Factura mapearEncabezado(ResultSet rs) throws SQLException {
        Factura f = new Factura();
        f.setIdFactura(rs.getLong("id_factura"));
        f.setNumero(rs.getString("numero"));
        Timestamp ts = rs.getTimestamp("fecha_hora");
        if (ts != null) {
            f.setFechaHora(ts.toLocalDateTime());
        }
        f.setEstado(EstadoFactura.valueOf(rs.getString("estado")));
        f.setObservaciones(rs.getString("observaciones"));

        long idCliente = rs.getLong("id_cliente");
        if (!rs.wasNull() && idCliente > 0) {
            Cliente c = new Cliente(
                    idCliente,
                    rs.getString("nit"),
                    rs.getString("cliente_nombre"),
                    rs.getString("direccion"),
                    rs.getString("telefono"),
                    rs.getString("correo")
            );
            f.setCliente(c);
        }

        Usuario u = new Usuario(
                rs.getLong("id_usuario"),
                rs.getString("usuario_nombre"),
                rs.getString("nombre_usuario"),
                rs.getString("rol"),
                rs.getBoolean("activo")
        );
        f.setUsuario(u);

        return f;
    }

    private void cargarDetallesYPagos(Connection con, Factura factura) throws SQLException {
        String sqlDetalles = "SELECT d.id_detalle, d.cantidad, d.precio_unitario, d.porcentaje_impuesto, "
                + "d.descuento, d.subtotal, "
                + "p.id_producto, p.codigo, p.nombre AS prod_nombre, p.descripcion, p.precio_venta, p.activo, "
                + "c.id_categoria, c.nombre AS cat_nombre, c.descripcion AS cat_desc, c.activa AS cat_activa "
                + "FROM dbo.detalle_factura d "
                + "INNER JOIN dbo.producto p ON d.producto_id = p.id_producto "
                + "INNER JOIN dbo.categoria c ON p.categoria_id = c.id_categoria "
                + "WHERE d.factura_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sqlDetalles)) {
            ps.setLong(1, factura.getIdFactura());
            try (ResultSet rs = ps.executeQuery()) {
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

                    DetalleFactura det = new DetalleFactura(
                            rs.getLong("id_detalle"),
                            prod,
                            rs.getBigDecimal("cantidad"),
                            rs.getBigDecimal("precio_unitario"),
                            rs.getBigDecimal("porcentaje_impuesto"),
                            rs.getBigDecimal("descuento")
                    );
                    factura.agregarDetalleDirecto(det);
                }
            }
        }

        String sqlPagos = "SELECT id_pago, fecha_hora, monto, metodo, referencia "
                + "FROM dbo.pago WHERE factura_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sqlPagos)) {
            ps.setLong(1, factura.getIdFactura());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp tsP = rs.getTimestamp("fecha_hora");
                    Pago pago = new Pago(
                            rs.getLong("id_pago"),
                            tsP != null ? tsP.toLocalDateTime() : null,
                            rs.getBigDecimal("monto"),
                            MetodoPago.valueOf(rs.getString("metodo")),
                            rs.getString("referencia")
                    );
                    factura.agregarPagoDirecto(pago);
                }
            }
        }
    }
}


