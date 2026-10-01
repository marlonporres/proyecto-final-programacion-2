package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.Bodega;
import java.sql.*;
import java.util.*;

public class BodegaDAOImpl implements BodegaDAO {
    @Override public void crear(Bodega b) { escribir(b, false); }
    @Override public void actualizar(Bodega b) { escribir(b, true); }
    private void escribir(Bodega b, boolean editar) {
        if (b == null || b.getNombre() == null || b.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de bodega es obligatorio.");
        }
        String sql = editar ? "UPDATE dbo.Bodega SET nombre=?,ubicacion=?,activa=? WHERE id=?"
                : "INSERT dbo.Bodega (nombre,ubicacion,activa) VALUES (?,?,?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getNombre().trim()); ps.setString(2, b.getUbicacion()); ps.setBoolean(3, b.isActiva());
            if (editar) ps.setInt(4, b.getId());
            if (ps.executeUpdate() != 1) throw new SQLException("Bodega no encontrada.");
            if (!editar) try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) b.setId(rs.getInt(1)); }
        } catch (SQLException e) { throw new IllegalStateException("No se pudo guardar la bodega.", e); }
    }
    @Override public Bodega obtener(int id) {
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement("SELECT * FROM dbo.Bodega WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapear(rs) : null; }
        } catch (SQLException e) { throw new IllegalStateException("No se pudo consultar la bodega.", e); }
    }
    @Override public List<Bodega> obtenerTodos() {
        List<Bodega> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement("SELECT * FROM dbo.Bodega ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
            return lista;
        } catch (SQLException e) { throw new IllegalStateException("No se pudieron consultar las bodegas.", e); }
    }
    @Override public void eliminar(int id) {
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement("UPDATE dbo.Bodega SET activa=0 WHERE id=?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() != 1) throw new SQLException("Bodega no encontrada.");
        } catch (SQLException e) { throw new IllegalStateException("No se pudo desactivar la bodega.", e); }
    }
    private Bodega mapear(ResultSet rs) throws SQLException {
        Bodega b = new Bodega(); b.setId(rs.getInt("id")); b.setNombre(rs.getString("nombre"));
        b.setUbicacion(rs.getString("ubicacion")); b.setActiva(rs.getBoolean("activa")); return b;
    }
}



