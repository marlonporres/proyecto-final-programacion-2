package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.ExistenciaInventario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExistenciaInventarioDAOImpl implements ExistenciaInventarioDAO {
    @Override
    public void crear(ExistenciaInventario e) {
        String sql = "INSERT INTO ExistenciaInventario (id_producto, id_bodega, existencia_actual, existencia_reservada) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, e.getProducto().getIdProducto());
            stmt.setInt(2, e.getBodega().getId());
            stmt.setInt(3, e.getExistenciaActual());
            stmt.setInt(4, e.getExistenciaReservada());
            stmt.executeUpdate();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    @Override
    public ExistenciaInventario obtener(int id) {
        String sql = "SELECT * FROM ExistenciaInventario WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ExistenciaInventario e = new ExistenciaInventario();
                    e.setId(rs.getInt("id"));
                    e.setExistenciaActual(rs.getInt("existencia_actual"));
                    e.setExistenciaReservada(rs.getInt("existencia_reservada"));
                    return e;
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        throw new RuntimeException("No se encontro existencia con ID " + id);
    }
    @Override
    public List<ExistenciaInventario> obtenerTodos() {
        List<ExistenciaInventario> lista = new ArrayList<>();
        String sql = "SELECT * FROM ExistenciaInventario";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while(rs.next()){
                ExistenciaInventario e = new ExistenciaInventario();
                e.setId(rs.getInt("id"));
                e.setExistenciaActual(rs.getInt("existencia_actual"));
                e.setExistenciaReservada(rs.getInt("existencia_reservada"));
                lista.add(e);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return lista;
    }
    @Override
    public void actualizar(ExistenciaInventario e) {
        String sql = "UPDATE ExistenciaInventario SET existencia_actual = ?, existencia_reservada = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, e.getExistenciaActual());
            stmt.setInt(2, e.getExistenciaReservada());
            stmt.setInt(3, e.getId());
            stmt.executeUpdate();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM ExistenciaInventario WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
