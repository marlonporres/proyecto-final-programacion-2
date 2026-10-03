package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.ExistenciaInventario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExistenciaInventarioDAOImpl implements ExistenciaInventarioDAO {
    private static final String SELECT = "SELECT e.id,e.existencia_actual,e.existencia_reservada,"
            + MapeoInventario.COLUMNAS + "FROM dbo.ExistenciaInventario e" + MapeoInventario.relaciones("e");

    @Override public void crear(ExistenciaInventario e) {
        validar(e);
        String sql = "INSERT dbo.ExistenciaInventario(id_producto,id_bodega,existencia_actual,existencia_reservada) VALUES(?,?,?,?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, e.getProducto().getIdProducto()); ps.setInt(2, e.getBodega().getId());
            ps.setInt(3, e.getExistenciaActual()); ps.setInt(4, e.getExistenciaReservada());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) e.setId(rs.getInt(1)); }
        } catch (SQLException ex) { throw new IllegalStateException("No se pudo crear la existencia de inventario.", ex); }
    }

    @Override public ExistenciaInventario obtener(int id) {
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(SELECT + "WHERE e.id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { throw new IllegalStateException("No se pudo consultar la existencia de inventario.", ex); }
        throw new IllegalArgumentException("No se encontró existencia con ID " + id);
    }

    @Override public List<ExistenciaInventario> obtenerTodos() {
        List<ExistenciaInventario> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(SELECT + "ORDER BY e.id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
            return lista;
        } catch (SQLException ex) { throw new IllegalStateException("No se pudieron consultar las existencias.", ex); }
    }

    @Override public void actualizar(ExistenciaInventario e) {
        validar(e);
        if (e.getId() == null) throw new IllegalArgumentException("La existencia debe estar guardada.");
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(
                "UPDATE dbo.ExistenciaInventario SET existencia_actual=?,existencia_reservada=? WHERE id=?")) {
            ps.setInt(1, e.getExistenciaActual()); ps.setInt(2, e.getExistenciaReservada()); ps.setInt(3, e.getId());
            if (ps.executeUpdate() != 1) throw new IllegalArgumentException("Existencia no encontrada.");
        } catch (SQLException ex) { throw new IllegalStateException("No se pudo actualizar la existencia.", ex); }
    }

    @Override public void eliminar(int id) {
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement("DELETE dbo.ExistenciaInventario WHERE id=?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() != 1) throw new IllegalArgumentException("Existencia no encontrada.");
        } catch (SQLException ex) { throw new IllegalStateException("No se pudo eliminar la existencia.", ex); }
    }

    private static void validar(ExistenciaInventario e) {
        if (e == null || e.getProducto() == null || e.getProducto().getIdProducto() <= 0
                || e.getBodega() == null || e.getBodega().getId() == null || e.getBodega().getId() <= 0
                || e.getExistenciaActual() < 0 || e.getExistenciaReservada() < 0
                || e.getExistenciaReservada() > e.getExistenciaActual()) {
            throw new IllegalArgumentException("Producto, bodega y cantidades de inventario inválidos.");
        }
    }

    private static ExistenciaInventario mapear(ResultSet rs) throws SQLException {
        ExistenciaInventario e = new ExistenciaInventario();
        e.setId(rs.getInt("id")); e.setProducto(MapeoInventario.producto(rs)); e.setBodega(MapeoInventario.bodega(rs));
        e.setExistenciaActual(rs.getInt("existencia_actual")); e.setExistenciaReservada(rs.getInt("existencia_reservada"));
        return e;
    }
}
