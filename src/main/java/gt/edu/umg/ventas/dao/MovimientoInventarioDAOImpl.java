package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.MovimientoInventario;
import gt.edu.umg.ventas.modelo.TipoMovimientoInventario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovimientoInventarioDAOImpl implements MovimientoInventarioDAO {
    @Override
    public void crear(MovimientoInventario m) {
        String sql = "INSERT INTO MovimientoInventario (id_producto, id_bodega, tipo_movimiento, cantidad, fecha, referencia) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, m.getProducto().getIdProducto());
            stmt.setInt(2, m.getBodega().getId());
            stmt.setString(3, m.getTipo().name());
            stmt.setInt(4, m.getCantidad());
            stmt.setTimestamp(5, Timestamp.valueOf(m.getFecha()));
            stmt.setString(6, m.getReferencia());
            stmt.executeUpdate();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    @Override
    public MovimientoInventario obtener(int id) {
        String sql = "SELECT * FROM MovimientoInventario WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if(rs.next()){
                    MovimientoInventario m = new MovimientoInventario();
                    m.setId(rs.getInt("id"));
                    m.setTipo(TipoMovimientoInventario.valueOf(rs.getString("tipo_movimiento")));
                    m.setCantidad(rs.getInt("cantidad"));
                    m.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    m.setReferencia(rs.getString("referencia"));
                    return m;
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        throw new RuntimeException("Movimiento no encontrado");
    }
    @Override
    public List<MovimientoInventario> obtenerTodos() {
        List<MovimientoInventario> lista = new ArrayList<>();
        String sql = "SELECT * FROM MovimientoInventario";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while(rs.next()){
                MovimientoInventario m = new MovimientoInventario();
                m.setId(rs.getInt("id"));
                m.setTipo(TipoMovimientoInventario.valueOf(rs.getString("tipo_movimiento")));
                m.setCantidad(rs.getInt("cantidad"));
                m.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                m.setReferencia(rs.getString("referencia"));
                lista.add(m);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return lista;
    }
    @Override
    public void actualizar(MovimientoInventario m) {
        // No se deben actualizar movimientos historicos
    }
    @Override
    public void eliminar(int id) {
        // No se deben eliminar movimientos historicos
    }
}
