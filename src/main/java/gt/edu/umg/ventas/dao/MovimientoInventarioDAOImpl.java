package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.MovimientoInventario;
import gt.edu.umg.ventas.modelo.TipoMovimientoInventario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovimientoInventarioDAOImpl implements MovimientoInventarioDAO {
    private static final String SELECT = "SELECT m.id,m.tipo_movimiento,m.cantidad,m.fecha,m.referencia,"
            + MapeoInventario.COLUMNAS + "FROM dbo.MovimientoInventario m" + MapeoInventario.relaciones("m");

    @Override public void crear(MovimientoInventario m) {
        if (m == null || m.getProducto() == null || m.getProducto().getIdProducto() <= 0
                || m.getBodega() == null || m.getBodega().getId() == null || m.getBodega().getId() <= 0
                || m.getTipo() == null || m.getCantidad() <= 0 || m.getFecha() == null
                || m.getReferencia() == null || m.getReferencia().isBlank()) {
            throw new IllegalArgumentException("Producto, bodega, tipo, cantidad, fecha y referencia son obligatorios.");
        }
        String sql = "INSERT dbo.MovimientoInventario(id_producto,id_bodega,tipo_movimiento,cantidad,fecha,referencia) VALUES(?,?,?,?,?,?)";
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, m.getProducto().getIdProducto()); ps.setInt(2, m.getBodega().getId());
            ps.setString(3, m.getTipo().name()); ps.setInt(4, m.getCantidad());
            ps.setTimestamp(5, Timestamp.valueOf(m.getFecha())); ps.setString(6, m.getReferencia());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) m.setId(rs.getInt(1)); }
        } catch (SQLException ex) { throw new IllegalStateException("No se pudo guardar el movimiento de inventario.", ex); }
    }

    @Override public MovimientoInventario obtener(int id) {
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(SELECT + "WHERE m.id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { throw new IllegalStateException("No se pudo consultar el movimiento de inventario.", ex); }
        throw new IllegalArgumentException("Movimiento no encontrado: " + id);
    }

    @Override public List<MovimientoInventario> obtenerTodos() {
        List<MovimientoInventario> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(SELECT + "ORDER BY m.id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
            return lista;
        } catch (SQLException ex) { throw new IllegalStateException("No se pudieron consultar los movimientos.", ex); }
    }

    @Override public void actualizar(MovimientoInventario m) {
        throw new UnsupportedOperationException("Los movimientos históricos no se pueden editar.");
    }

    @Override public void eliminar(int id) {
        throw new UnsupportedOperationException("Los movimientos históricos no se pueden eliminar.");
    }

    private static MovimientoInventario mapear(ResultSet rs) throws SQLException {
        MovimientoInventario m = new MovimientoInventario();
        m.setId(rs.getInt("id")); m.setProducto(MapeoInventario.producto(rs)); m.setBodega(MapeoInventario.bodega(rs));
        m.setTipo(TipoMovimientoInventario.valueOf(rs.getString("tipo_movimiento")));
        m.setCantidad(rs.getInt("cantidad")); m.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        m.setReferencia(rs.getString("referencia"));
        return m;
    }
}
