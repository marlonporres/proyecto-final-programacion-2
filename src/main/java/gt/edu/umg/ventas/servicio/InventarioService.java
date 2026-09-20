package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.ConexionBD;
import gt.edu.umg.ventas.modelo.Bodega;
import gt.edu.umg.ventas.modelo.ExistenciaInventario;
import gt.edu.umg.ventas.modelo.InventarioResumen;
import gt.edu.umg.ventas.modelo.Producto;
import gt.edu.umg.ventas.modelo.TipoMovimientoInventario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventarioService {

    public ExistenciaInventario consultarExistencia(Producto producto, Bodega bodega) {
        return consultarExistencia(producto, bodega, null);
    }

    public ExistenciaInventario consultarExistencia(Producto producto, Bodega bodega, Connection conn) {
        String sql = "SELECT id, id_producto, id_bodega, existencia_actual, existencia_reservada "
                + "FROM dbo.ExistenciaInventario WHERE id_producto = ? AND id_bodega = ?";
        boolean localConn = false;
        try {
            if (conn == null) {
                conn = ConexionBD.obtenerConexion();
                localConn = true;
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, producto.getIdProducto());
                stmt.setInt(2, bodega.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        ExistenciaInventario ex = new ExistenciaInventario();
                        ex.setId(rs.getInt("id"));
                        ex.setProducto(producto);
                        ex.setBodega(bodega);
                        ex.setExistenciaActual(rs.getInt("existencia_actual"));
                        ex.setExistenciaReservada(rs.getInt("existencia_reservada"));
                        return ex;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar existencia: " + e.getMessage(), e);
        } finally {
            if (localConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return null;
    }

    public boolean hayDisponibilidad(Producto producto, Bodega bodega, int cantidad) {
        return hayDisponibilidad(producto, bodega, cantidad, null);
    }

    public boolean hayDisponibilidad(Producto producto, Bodega bodega, int cantidad, Connection conn) {
        ExistenciaInventario ex = consultarExistencia(producto, bodega, conn);
        if (ex != null) {
            return ex.getExistenciaDisponible() >= cantidad;
        }
        return false;
    }

    public void registrarEntrada(Producto producto, Bodega bodega, int cantidad, String referencia) throws Exception {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        String updateExistencia = "UPDATE dbo.ExistenciaInventario SET existencia_actual = existencia_actual + ? WHERE id_producto = ? AND id_bodega = ?";
        String insertExistencia = "INSERT INTO dbo.ExistenciaInventario (id_producto, id_bodega, existencia_actual, existencia_reservada) VALUES (?, ?, ?, 0)";
        String insertMovimiento = "INSERT INTO dbo.MovimientoInventario (id_producto, id_bodega, tipo_movimiento, cantidad, fecha, referencia) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);
            try {
                ExistenciaInventario ex = consultarExistencia(producto, bodega, conn);
                if (ex != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(updateExistencia)) {
                        stmt.setInt(1, cantidad);
                        stmt.setLong(2, producto.getIdProducto());
                        stmt.setInt(3, bodega.getId());
                        stmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement stmt = conn.prepareStatement(insertExistencia)) {
                        stmt.setLong(1, producto.getIdProducto());
                        stmt.setInt(2, bodega.getId());
                        stmt.setInt(3, cantidad);
                        stmt.executeUpdate();
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(insertMovimiento)) {
                    stmt.setLong(1, producto.getIdProducto());
                    stmt.setInt(2, bodega.getId());
                    stmt.setString(3, TipoMovimientoInventario.ENTRADA.name());
                    stmt.setInt(4, cantidad);
                    stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setString(6, referencia);
                    stmt.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void registrarSalida(Producto producto, Bodega bodega, int cantidad, String referencia, Connection conn) throws Exception {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        ExistenciaInventario ex = consultarExistencia(producto, bodega, conn);
        int disp = (ex != null) ? ex.getExistenciaDisponible() : 0;
        if (ex == null || disp < cantidad) {
            throw new Exception("Stock insuficiente en bodega '" + bodega.getNombre() 
                    + "' para el producto '" + producto.getNombre() 
                    + "'. Disponible: " + disp + ", Solicitado: " + cantidad);
        }

        String updateExistencia = "UPDATE dbo.ExistenciaInventario SET existencia_actual = existencia_actual - ? WHERE id_producto = ? AND id_bodega = ?";
        String insertMovimiento = "INSERT INTO dbo.MovimientoInventario (id_producto, id_bodega, tipo_movimiento, cantidad, fecha, referencia) VALUES (?, ?, ?, ?, ?, ?)";

        boolean localConn = false;
        if (conn == null) {
            conn = ConexionBD.obtenerConexion();
            conn.setAutoCommit(false);
            localConn = true;
        }

        try {
            try (PreparedStatement stmt = conn.prepareStatement(updateExistencia)) {
                stmt.setInt(1, cantidad);
                stmt.setLong(2, producto.getIdProducto());
                stmt.setInt(3, bodega.getId());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertMovimiento)) {
                stmt.setLong(1, producto.getIdProducto());
                stmt.setInt(2, bodega.getId());
                stmt.setString(3, TipoMovimientoInventario.SALIDA.name());
                stmt.setInt(4, cantidad);
                stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(6, referencia);
                stmt.executeUpdate();
            }

            if (localConn) conn.commit();
        } catch (Exception e) {
            if (localConn) conn.rollback();
            throw e;
        } finally {
            if (localConn) conn.close();
        }
    }

    public List<InventarioResumen> listarExistencias(Integer idBodega, String filtroTexto) {
        List<InventarioResumen> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.codigo, p.nombre AS producto, b.nombre AS bodega, "
                + "ei.existencia_actual, ei.existencia_reservada, "
                + "(ei.existencia_actual - ei.existencia_reservada) AS disponible "
                + "FROM dbo.ExistenciaInventario ei "
                + "INNER JOIN dbo.producto p ON ei.id_producto = p.id_producto "
                + "INNER JOIN dbo.Bodega b ON ei.id_bodega = b.id "
                + "WHERE 1=1 "
        );

        if (idBodega != null && idBodega > 0) {
            sql.append("AND ei.id_bodega = ? ");
        }
        if (filtroTexto != null && !filtroTexto.isBlank()) {
            sql.append("AND (p.nombre LIKE ? OR p.codigo LIKE ?) ");
        }
        sql.append("ORDER BY b.nombre, p.nombre");

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (idBodega != null && idBodega > 0) {
                stmt.setInt(paramIndex++, idBodega);
            }
            if (filtroTexto != null && !filtroTexto.isBlank()) {
                String patron = "%" + filtroTexto.trim() + "%";
                stmt.setString(paramIndex++, patron);
                stmt.setString(paramIndex++, patron);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InventarioResumen ir = new InventarioResumen(
                            rs.getString("codigo"),
                            rs.getString("producto"),
                            rs.getString("bodega"),
                            rs.getInt("existencia_actual"),
                            rs.getInt("existencia_reservada"),
                            rs.getInt("disponible")
                    );
                    lista.add(ir);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar existencias de inventario: " + e.getMessage(), e);
        }
        return lista;
    }

    public int obtenerDisponibilidadTotal(long idProducto) {
        String sql = "SELECT SUM(existencia_actual - existencia_reservada) AS disponible "
                   + "FROM dbo.ExistenciaInventario ei "
                   + "INNER JOIN dbo.Bodega b ON ei.id_bodega = b.id "
                   + "WHERE ei.id_producto = ? AND b.activa = 1";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idProducto);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("disponible");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener disponibilidad total: " + e.getMessage());
        }
        return 0;
    }
}
