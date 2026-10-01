package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.Categoria;
import gt.edu.umg.ventas.modelo.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC para ProductoDAO.
 * Exclusivamente maneja catálogo de productos.
 */
public class ProductoDAOImpl implements ProductoDAO {
    @Override public void guardar(Producto p) { escribir(p, false); }
    @Override public void actualizar(Producto p) { escribir(p, true); }
    private void escribir(Producto p, boolean editar) {
        if (p == null || p.getCodigo() == null || p.getCodigo().isBlank() || p.getNombre() == null
                || p.getNombre().isBlank() || p.getPrecioVenta() == null || p.getPrecioVenta().signum() < 0
                || p.getCategoria() == null || p.getCategoria().getIdCategoria() <= 0 || (editar && p.getIdProducto() <= 0)) {
            throw new IllegalArgumentException("Código, nombre, categoría y precio no negativo son obligatorios.");
        }
        String sql = editar ? "UPDATE dbo.producto SET codigo=?, nombre=?, descripcion=?, precio_venta=?, activo=?, categoria_id=? WHERE id_producto=?"
                : "INSERT dbo.producto (codigo,nombre,descripcion,precio_venta,activo,categoria_id) VALUES (?,?,?,?,?,?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCodigo().trim()); ps.setString(2, p.getNombre().trim());
            ps.setString(3, p.getDescripcion()); ps.setBigDecimal(4, p.getPrecioVenta());
            ps.setBoolean(5, p.isActivo()); ps.setLong(6, p.getCategoria().getIdCategoria());
            if (editar) ps.setLong(7, p.getIdProducto());
            if (ps.executeUpdate() != 1) throw new SQLException("Producto no encontrado.");
            if (!editar) try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) p.setIdProducto(rs.getLong(1)); }
        } catch (SQLException e) { throw new IllegalStateException("No se pudo guardar el producto: " + e.getMessage(), e); }
    }

    private final ConexionBD conexion;

    public ProductoDAOImpl() {
        this.conexion = new ConexionBD();
    }

    public ProductoDAOImpl(ConexionBD conexion) {
        this.conexion = conexion != null ? conexion : new ConexionBD();
    }

    @Override
    public Producto buscarPorId(long idProducto) {
        String sql = baseSelect() + "WHERE p.id_producto = ?";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto por ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Producto buscarPorCodigo(String codigo) {
        String sql = baseSelect() + "WHERE p.codigo = ?";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo != null ? codigo.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto por código: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Producto> listar() {
        List<Producto> lista = new ArrayList<>();
        String sql = baseSelect() + "ORDER BY p.nombre ASC";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public List<Producto> buscarPorTexto(String criterio) {
        List<Producto> lista = new ArrayList<>();
        String sql = baseSelect() + "WHERE p.codigo LIKE ? OR p.nombre LIKE ? ORDER BY p.nombre ASC";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + (criterio != null ? criterio.trim() : "") + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar productos por texto: " + e.getMessage(), e);
        }
        return lista;
    }

    private String baseSelect() {
        return "SELECT p.id_producto, p.codigo, p.nombre, p.descripcion, p.precio_venta, p.activo, "
                + "c.id_categoria, c.nombre AS cat_nombre, c.descripcion AS cat_desc, c.activa AS cat_activa "
                + "FROM dbo.producto p "
                + "INNER JOIN dbo.categoria c ON p.categoria_id = c.id_categoria ";
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Categoria cat = new Categoria(
                rs.getLong("id_categoria"),
                rs.getString("cat_nombre"),
                rs.getString("cat_desc"),
                rs.getBoolean("cat_activa")
        );
        return new Producto(
                rs.getLong("id_producto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBigDecimal("precio_venta"),
                rs.getBoolean("activo"),
                cat
        );
    }
}
