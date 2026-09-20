package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.Bodega;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BodegaDAOImpl implements BodegaDAO {
    @Override
    public void crear(Bodega bodega) {
        String sql = "INSERT INTO Bodega (nombre, ubicacion, activa) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bodega.getNombre());
            stmt.setString(2, bodega.getUbicacion());
            stmt.setBoolean(3, bodega.isActiva());
            stmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public Bodega obtener(int id) {
        String sql = "SELECT * FROM Bodega WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if(rs.next()){
                    Bodega b = new Bodega();
                    b.setId(rs.getInt("id"));
                    b.setNombre(rs.getString("nombre"));
                    b.setUbicacion(rs.getString("ubicacion"));
                    b.setActiva(rs.getBoolean("activa"));
                    return b;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Bodega> obtenerTodos() {
        List<Bodega> lista = new ArrayList<>();
        String sql = "SELECT * FROM Bodega";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while(rs.next()){
                Bodega b = new Bodega();
                b.setId(rs.getInt("id"));
                b.setNombre(rs.getString("nombre"));
                b.setUbicacion(rs.getString("ubicacion"));
                b.setActiva(rs.getBoolean("activa"));
                lista.add(b);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    @Override
    public void actualizar(Bodega bodega) {
        // Implementar
    }

    @Override
    public void eliminar(int id) {
        // Implementar
    }
}



