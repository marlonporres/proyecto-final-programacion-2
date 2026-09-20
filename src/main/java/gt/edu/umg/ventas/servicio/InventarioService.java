package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.ConexionBD;
import gt.edu.umg.ventas.dao.ExistenciaInventarioDAO;
import gt.edu.umg.ventas.dao.ExistenciaInventarioDAOImpl;
import gt.edu.umg.ventas.modelo.Bodega;
import gt.edu.umg.ventas.modelo.ExistenciaInventario;
import gt.edu.umg.ventas.modelo.MovimientoInventario;
import gt.edu.umg.ventas.modelo.Producto;
import gt.edu.umg.ventas.modelo.TipoMovimientoInventario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class InventarioService {
    
    public ExistenciaInventario consultarExistencia(Producto producto, Bodega bodega) {
        String sql = "SELECT * FROM ExistenciaInventario WHERE id_producto = ? AND id_bodega = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, (int) producto.getIdProducto());
            stmt.setInt(2, bodega.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ExistenciaInventario ex = new ExistenciaInventario();
                ex.setId(rs.getInt("id"));
                ex.setProducto(producto);
                ex.setBodega(bodega);
                ex.setExistenciaActual(rs.getInt("existencia_actual"));
                ex.setExistenciaReservada(rs.getInt("existencia_reservada"));
                return ex;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Existencia no encontrada");
    }

    public boolean hayDisponibilidad(Producto producto, Bodega bodega, int cantidad) {
        ExistenciaInventario ex = consultarExistencia(producto, bodega);
        if (ex != null) {
            return ex.getExistenciaDisponible() >= cantidad;
        }
        return false;
    }

    public void registrarEntrada(Producto producto, Bodega bodega, int cantidad, String referencia) throws Exception {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        
        String updateExistencia = "UPDATE ExistenciaInventario SET existencia_actual = existencia_actual + ? WHERE id_producto = ? AND id_bodega = ?";
        String insertExistencia = "INSERT INTO ExistenciaInventario (id_producto, id_bodega, existencia_actual) VALUES (?, ?, ?)";
        String insertMovimiento = "INSERT INTO MovimientoInventario (id_producto, id_bodega, tipo_movimiento, cantidad, fecha, referencia) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);
            try {
                ExistenciaInventario ex = consultarExistencia(producto, bodega);
                if (ex != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(updateExistencia)) {
                        stmt.setInt(1, cantidad);
                        stmt.setInt(2, (int) producto.getIdProducto());
                        stmt.setInt(3, bodega.getId());
                        stmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement stmt = conn.prepareStatement(insertExistencia)) {
                        stmt.setInt(1, (int) producto.getIdProducto());
                        stmt.setInt(2, bodega.getId());
                        stmt.setInt(3, cantidad);
                        stmt.executeUpdate();
                    }
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(insertMovimiento)) {
                    stmt.setInt(1, (int) producto.getIdProducto());
                    stmt.setInt(2, bodega.getId());
                    stmt.setString(3, TipoMovimientoInventario.ENTRADA.name());
                    stmt.setInt(4, cantidad);
                    stmt.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
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
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        if (!hayDisponibilidad(producto, bodega, cantidad)) {
            throw new Exception("No hay disponibilidad suficiente para el producto: " + producto.getNombre());
        }
        
        String updateExistencia = "UPDATE ExistenciaInventario SET existencia_actual = existencia_actual - ? WHERE id_producto = ? AND id_bodega = ?";
        String insertMovimiento = "INSERT INTO MovimientoInventario (id_producto, id_bodega, tipo_movimiento, cantidad, fecha, referencia) VALUES (?, ?, ?, ?, ?, ?)";
        
        boolean localConn = false;
        if (conn == null) {
            conn = ConexionBD.obtenerConexion();
            conn.setAutoCommit(false);
            localConn = true;
        }
        
        try {
            try (PreparedStatement stmt = conn.prepareStatement(updateExistencia)) {
                stmt.setInt(1, cantidad);
                stmt.setInt(2, (int) producto.getIdProducto());
                stmt.setInt(3, bodega.getId());
                stmt.executeUpdate();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(insertMovimiento)) {
                stmt.setInt(1, (int) producto.getIdProducto());
                stmt.setInt(2, bodega.getId());
                stmt.setString(3, TipoMovimientoInventario.SALIDA.name());
                stmt.setInt(4, cantidad);
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
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
}


