package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC para CategoriaDAO.
 */
public class CategoriaDAOImpl implements CategoriaDAO {

    private final ConexionBD conexion;

    public CategoriaDAOImpl() {
        this.conexion = new ConexionBD();
    }

    public CategoriaDAOImpl(ConexionBD conexion) {
        this.conexion = conexion != null ? conexion : new ConexionBD();
    }

    @Override
    public Categoria buscarPorId(long idCategoria) {
        String sql = "SELECT id_categoria, nombre, descripcion, activa FROM dbo.categoria WHERE id_categoria = ?";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCategoria(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar categoría por ID: " + e.getMessage(), e);
        }
        throw new RuntimeException("Categoria no encontrada");
    }

    @Override
    public List<Categoria> listar() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id_categoria, nombre, descripcion, activa FROM dbo.categoria WHERE activa = 1 ORDER BY nombre ASC";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearCategoria(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar categorías: " + e.getMessage(), e);
        }
        return lista;
    }

    private Categoria mapearCategoria(ResultSet rs) throws SQLException {
        return new Categoria(
                rs.getLong("id_categoria"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBoolean("activa")
        );
    }
}

