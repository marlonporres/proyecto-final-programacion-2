package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.Despacho;
import gt.edu.umg.ventas.modelo.EstadoDespacho;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DespachoDAOImpl implements DespachoDAO {
    @Override
    public void crear(Despacho d) {
        String sql = "INSERT INTO Despacho (numero_despacho, id_orden, id_bodega, fecha_despacho, estado) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, d.getNumeroDespacho());
            stmt.setInt(2, d.getOrden().getId());
            stmt.setInt(3, d.getBodega().getId());
            stmt.setTimestamp(4, Timestamp.valueOf(d.getFechaDespacho()));
            stmt.setString(5, d.getEstado().name());
            stmt.executeUpdate();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    @Override
    public Despacho obtener(int id) {
        String sql = "SELECT * FROM Despacho WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if(rs.next()){
                    Despacho d = new Despacho();
                    d.setId(rs.getInt("id"));
                    d.setNumeroDespacho(rs.getString("numero_despacho"));
                    d.setEstado(EstadoDespacho.valueOf(rs.getString("estado")));
                    d.setFechaDespacho(rs.getTimestamp("fecha_despacho").toLocalDateTime());
                    return d;
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        throw new RuntimeException("Despacho no encontrado");
    }
    @Override
    public List<Despacho> obtenerTodos() {
        List<Despacho> lista = new ArrayList<>();
        String sql = "SELECT * FROM Despacho";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while(rs.next()){
                Despacho d = new Despacho();
                d.setId(rs.getInt("id"));
                d.setNumeroDespacho(rs.getString("numero_despacho"));
                d.setEstado(EstadoDespacho.valueOf(rs.getString("estado")));
                d.setFechaDespacho(rs.getTimestamp("fecha_despacho").toLocalDateTime());
                lista.add(d);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return lista;
    }
    @Override
    public void actualizar(Despacho d) {
        String sql = "UPDATE Despacho SET estado = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, d.getEstado().name());
            stmt.setInt(2, d.getId());
            stmt.executeUpdate();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM Despacho WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
