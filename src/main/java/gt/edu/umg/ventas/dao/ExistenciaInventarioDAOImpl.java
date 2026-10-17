package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.Bodega;
import gt.edu.umg.ventas.modelo.ExistenciaInventario;
import gt.edu.umg.ventas.modelo.InventarioResumen;
import gt.edu.umg.ventas.modelo.Producto;
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

    @Override
    public ExistenciaInventario obtenerParaActualizar(Connection con, long idProducto, int idBodega) {
        String sql = "SELECT id, id_producto, id_bodega, existencia_actual, existencia_reservada "
                + "FROM dbo.ExistenciaInventario WITH (UPDLOCK, HOLDLOCK) WHERE id_producto=? AND id_bodega=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idProducto);
            ps.setInt(2, idBodega);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ExistenciaInventario ex = new ExistenciaInventario();
                    ex.setId(rs.getInt("id"));
                    Producto p = new Producto();
                    p.setIdProducto(rs.getLong("id_producto"));
                    ex.setProducto(p);
                    Bodega b = new Bodega();
                    b.setId(rs.getInt("id_bodega"));
                    ex.setBodega(b);
                    ex.setExistenciaActual(rs.getInt("existencia_actual"));
                    ex.setExistenciaReservada(rs.getInt("existencia_reservada"));
                    return ex;
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo consultar la existencia para actualizar.", ex);
        }
    }

    @Override
    public void insertarExistenciaInicial(Connection con, long idProducto, int idBodega, int existenciaActual) {
        String sql = "INSERT dbo.ExistenciaInventario(id_producto,id_bodega,existencia_actual,existencia_reservada) VALUES(?,?,?,0)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idProducto);
            ps.setInt(2, idBodega);
            ps.setInt(3, existenciaActual);
            if (ps.executeUpdate() != 1) {
                throw new IllegalStateException("No se pudo insertar la existencia inicial.");
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo insertar la existencia inicial.", ex);
        }
    }

    @Override
    public void actualizarExistenciaActual(Connection con, long idExistencia, int nuevaActual) {
        String sql = "UPDATE dbo.ExistenciaInventario SET existencia_actual=? WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevaActual);
            ps.setLong(2, idExistencia);
            if (ps.executeUpdate() != 1) {
                throw new IllegalStateException("No se encontró la existencia para actualizar.");
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo actualizar la existencia actual.", ex);
        }
    }

    @Override
    public void actualizarExistenciaReservada(Connection con, long idExistencia, int nuevaReservada) {
        String sql = "UPDATE dbo.ExistenciaInventario SET existencia_reservada=? WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevaReservada);
            ps.setLong(2, idExistencia);
            if (ps.executeUpdate() != 1) {
                throw new IllegalStateException("No se encontró la existencia para actualizar reserva.");
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo actualizar la existencia reservada.", ex);
        }
    }

    @Override
    public void actualizarActualYReservada(Connection con, long idExistencia, int nuevaActual, int nuevaReservada) {
        String sql = "UPDATE dbo.ExistenciaInventario SET existencia_actual=?, existencia_reservada=? WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevaActual);
            ps.setInt(2, nuevaReservada);
            ps.setLong(3, idExistencia);
            if (ps.executeUpdate() != 1) {
                throw new IllegalStateException("No se encontró la existencia para actualizar saldos.");
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo actualizar la existencia actual y reservada.", ex);
        }
    }

    @Override
    public boolean existeBodegaActiva(Connection con, int idBodega) {
        String sql = "SELECT activa FROM dbo.Bodega WITH (HOLDLOCK) WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idBodega);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean("activa");
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo verificar el estado de la bodega.", ex);
        }
    }

    @Override
    public boolean existeProducto(Connection con, long idProducto) {
        String sql = "SELECT 1 FROM dbo.producto WITH (HOLDLOCK) WHERE id_producto=? AND activo=1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo verificar el producto.", ex);
        }
    }

    @Override
    public List<InventarioResumen> consultarSaldosActivos() {
        List<InventarioResumen> lista = new ArrayList<>();
        String sql = "SELECT p.codigo, p.nombre AS producto, b.nombre AS bodega, "
                + "ISNULL(e.existencia_actual, 0) AS existencia_actual, "
                + "ISNULL(e.existencia_reservada, 0) AS existencia_reservada, "
                + "ISNULL(e.existencia_actual - e.existencia_reservada, 0) AS disponible "
                + "FROM dbo.producto p "
                + "CROSS JOIN dbo.Bodega b "
                + "LEFT JOIN dbo.ExistenciaInventario e ON e.id_producto = p.id_producto AND e.id_bodega = b.id "
                + "WHERE p.activo = 1 AND b.activa = 1 "
                + "ORDER BY b.nombre, p.nombre";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new InventarioResumen(
                        rs.getString("codigo"),
                        rs.getString("producto"),
                        rs.getString("bodega"),
                        rs.getInt("existencia_actual"),
                        rs.getInt("existencia_reservada"),
                        rs.getInt("disponible")
                ));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al consultar saldos de inventario: " + ex.getMessage(), ex);
        }
        return lista;
    }

    @Override
    public List<InventarioResumen> listarExistencias(Integer idBodega, String filtroTexto) {
        List<InventarioResumen> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.codigo, p.nombre AS producto, b.nombre AS bodega, "
                + "ei.existencia_actual, ei.existencia_reservada, "
                + "(ei.existencia_actual - ei.existencia_reservada) AS disponible "
                + "FROM dbo.ExistenciaInventario ei "
                + "INNER JOIN dbo.producto p ON ei.id_producto = p.id_producto "
                + "INNER JOIN dbo.Bodega b ON ei.id_bodega = b.id "
                + "WHERE 1=1 "
        );

        if (idBodega != null && idBodega > 0) {
            sql.append("AND ei.id_bodega = ? ");
        }
        if (filtroTexto != null && !filtroTexto.isBlank()) {
            sql.append("AND (p.nombre LIKE ? OR p.codigo LIKE ?) ");
        }
        sql.append("ORDER BY b.nombre, p.nombre");

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (idBodega != null && idBodega > 0) {
                stmt.setInt(paramIndex++, idBodega);
            }
            if (filtroTexto != null && !filtroTexto.isBlank()) {
                String patron = "%" + filtroTexto.trim() + "%";
                stmt.setString(paramIndex++, patron);
                stmt.setString(paramIndex++, patron);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InventarioResumen ir = new InventarioResumen(
                            rs.getString("codigo"),
                            rs.getString("producto"),
                            rs.getString("bodega"),
                            rs.getInt("existencia_actual"),
                            rs.getInt("existencia_reservada"),
                            rs.getInt("disponible")
                    );
                    lista.add(ir);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar existencias de inventario: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public int obtenerDisponibilidadTotal(long idProducto) {
        String sql = "SELECT SUM(existencia_actual - existencia_reservada) AS disponible "
                   + "FROM dbo.ExistenciaInventario ei "
                   + "INNER JOIN dbo.Bodega b ON ei.id_bodega = b.id "
                   + "WHERE ei.id_producto = ? AND b.activa = 1";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idProducto);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("disponible");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar la disponibilidad de inventario.", e);
        }
        return 0;
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

