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
    @Override public void guardar(Categoria c) { escribir(c, false); }
    @Override public void actualizar(Categoria c) { escribir(c, true); }
    private void escribir(Categoria c, boolean editar) {
        if (c == null || c.getNombre() == null || c.getNombre().isBlank() || (editar && c.getIdCategoria() <= 0)) {
            throw new IllegalArgumentException("El nombre de categoría es obligatorio.");
        }
        String sql = editar ? "UPDATE dbo.categoria SET nombre=?, descripcion=?, activa=? WHERE id_categoria=?"
                : "INSERT dbo.categoria (nombre,descripcion,activa) VALUES (?,?,?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre().trim()); ps.setString(2, c.getDescripcion()); ps.setBoolean(3, c.isActiva());
            if (editar) ps.setLong(4, c.getIdCategoria());
            if (ps.executeUpdate() != 1) throw new SQLException("Categoría no encontrada.");
            if (!editar) try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) c.setIdCategoria(rs.getLong(1)); }
        } catch (SQLException e) { throw new IllegalStateException("No se pudo guardar la categoría: " + e.getMessage(), e); }
    }

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
        String sql = "SELECT id_categoria, nombre, descripcion, activa FROM dbo.categoria ORDER BY nombre ASC";
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

