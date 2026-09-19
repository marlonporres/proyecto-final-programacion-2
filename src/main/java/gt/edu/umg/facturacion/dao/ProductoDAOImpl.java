package gt.edu.umg.facturacion.dao;

import gt.edu.umg.facturacion.modelo.Categoria;
import gt.edu.umg.facturacion.modelo.Inventario;
import gt.edu.umg.facturacion.modelo.Producto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC para ProductoDAO.
 */
public class ProductoDAOImpl implements ProductoDAO {

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

    @Override
    public void actualizarInventario(long idInventario, BigDecimal nuevaExistencia) {
        String sql = "UPDATE dbo.inventario SET existencia = ?, actualizado_en = SYSDATETIME() WHERE id_inventario = ?";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, nuevaExistencia);
            ps.setLong(2, idInventario);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar existencia en inventario: " + e.getMessage(), e);
        }
    }

    private String baseSelect() {
        return "SELECT p.id_producto, p.codigo, p.nombre, p.descripcion, p.precio_venta, p.activo, "
                + "c.id_categoria, c.nombre AS cat_nombre, c.descripcion AS cat_desc, c.activa AS cat_activa, "
                + "i.id_inventario, i.existencia, i.stock_minimo, i.actualizado_en "
                + "FROM dbo.producto p "
                + "INNER JOIN dbo.categoria c ON p.categoria_id = c.id_categoria "
                + "INNER JOIN dbo.inventario i ON p.inventario_id = i.id_inventario ";
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Categoria cat = new Categoria(
                rs.getLong("id_categoria"),
                rs.getString("cat_nombre"),
                rs.getString("cat_desc"),
                rs.getBoolean("cat_activa")
        );
        Timestamp ts = rs.getTimestamp("actualizado_en");
        Inventario inv = new Inventario(
                rs.getLong("id_inventario"),
                rs.getBigDecimal("existencia"),
                rs.getBigDecimal("stock_minimo"),
                ts != null ? ts.toLocalDateTime() : null
        );
        return new Producto(
                rs.getLong("id_producto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBigDecimal("precio_venta"),
                rs.getBoolean("activo"),
                cat,
                inv
        );
    }
}
