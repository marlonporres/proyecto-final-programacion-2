package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class DespachoDAOImpl implements DespachoDAO {
    @Override
    public Despacho obtener(int id) {
        return consultar("id", id);
    }

    @Override
    public Despacho obtenerPorOrden(int idOrden) {
        return consultar("id_orden", idOrden);
    }

    @Override
    public boolean existePorOrden(Connection con, int idOrden) {
        String sql = "SELECT id FROM dbo.Despacho WHERE id_orden=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo verificar despacho de la orden.", e);
        }
    }

    @Override
    public int insertarCabecera(Connection con, String numero, int idOrden, int idBodega, LocalDateTime fecha) {
        String sql = "INSERT dbo.Despacho(numero_despacho,id_orden,id_bodega,fecha_despacho,estado) VALUES(?,?,?,?, 'PENDIENTE')";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, numero);
            ps.setInt(2, idOrden);
            ps.setInt(3, idBodega);
            ps.setTimestamp(4, Timestamp.valueOf(fecha));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new IllegalStateException("No se obtuvo el ID de despacho.");
                }
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo insertar la cabecera del despacho.", e);
        }
    }

    @Override
    public void insertarDetalles(Connection con, int idDespacho, List<DetalleDespacho> detallesOrdenados) {
        String sql = "INSERT dbo.DetalleDespacho(id_despacho,id_producto,cantidad_solicitada,cantidad_despachada) VALUES(?,?,?,0)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (DetalleDespacho linea : detallesOrdenados) {
                ps.setInt(1, idDespacho);
                ps.setLong(2, linea.getProducto().getIdProducto());
                ps.setInt(3, linea.getCantidadSolicitada());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron insertar los detalles del despacho.", e);
        }
    }

    @Override
    public Despacho obtenerPorIdParaActualizar(Connection con, int idDespacho) {
        String sql = "SELECT d.id, d.numero_despacho, d.id_orden, d.id_bodega, d.estado, dd.id_producto, dd.cantidad_solicitada "
                + "FROM dbo.Despacho d WITH (UPDLOCK, HOLDLOCK) "
                + "JOIN dbo.DetalleDespacho dd ON dd.id_despacho = d.id WHERE d.id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDespacho);
            Despacho d = null;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (d == null) {
                        d = new Despacho();
                        d.setId(rs.getInt("id"));
                        d.setNumeroDespacho(rs.getString("numero_despacho"));
                        OrdenVenta o = new OrdenVenta();
                        o.setId(rs.getInt("id_orden"));
                        d.setOrden(o);
                        Bodega b = new Bodega();
                        b.setId(rs.getInt("id_bodega"));
                        d.setBodega(b);
                        d.setEstado(EstadoDespacho.valueOf(rs.getString("estado")));
                    }
                    Producto p = new Producto();
                    p.setIdProducto(rs.getLong("id_producto"));
                    DetalleDespacho linea = new DetalleDespacho();
                    linea.setDespacho(d);
                    linea.setProducto(p);
                    linea.setCantidadSolicitada(rs.getInt("cantidad_solicitada"));
                    linea.setCantidadDespachada(0);
                    d.getDetalles().add(linea);
                }
            }
            return d;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo obtener el despacho para actualizar.", e);
        }
    }

    @Override
    public void marcarDetallesDespachados(Connection con, int idDespacho) {
        String sql = "UPDATE dbo.DetalleDespacho SET cantidad_despachada=cantidad_solicitada WHERE id_despacho=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDespacho);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron marcar los detalles despachados.", e);
        }
    }

    @Override
    public void confirmarEstado(Connection con, int idDespacho) {
        String sql = "UPDATE dbo.Despacho SET estado='CONFIRMADO' WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDespacho);
            if (ps.executeUpdate() != 1) {
                throw new IllegalStateException("El despacho cambió de estado.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo confirmar el estado del despacho.", e);
        }
    }

    private Despacho consultar(String campo, int id) {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM dbo.Despacho WHERE " + campo + "=?")) {
            ps.setInt(1, id);
            Despacho d;
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                d = new Despacho();
                d.setId(rs.getInt("id"));
                d.setNumeroDespacho(rs.getString("numero_despacho"));
                d.setEstado(EstadoDespacho.valueOf(rs.getString("estado")));
                d.setFechaDespacho(rs.getTimestamp("fecha_despacho").toLocalDateTime());
                d.setOrden(new OrdenVentaDAOImpl().obtener(rs.getInt("id_orden")));
                d.setBodega(new BodegaDAOImpl().obtener(rs.getInt("id_bodega")));
            }
            try (PreparedStatement detalles = con.prepareStatement(
                    "SELECT dd.*,p.codigo,p.nombre FROM dbo.DetalleDespacho dd JOIN dbo.producto p ON p.id_producto=dd.id_producto WHERE dd.id_despacho=? ORDER BY dd.id_producto")) {
                detalles.setInt(1, d.getId());
                try (ResultSet rs = detalles.executeQuery()) {
                    while (rs.next()) {
                        Producto p = new Producto();
                        p.setIdProducto(rs.getLong("id_producto"));
                        p.setCodigo(rs.getString("codigo"));
                        p.setNombre(rs.getString("nombre"));
                        DetalleDespacho linea = new DetalleDespacho();
                        linea.setDespacho(d);
                        linea.setProducto(p);
                        linea.setCantidadSolicitada(rs.getInt("cantidad_solicitada"));
                        linea.setCantidadDespachada(rs.getInt("cantidad_despachada"));
                        d.getDetalles().add(linea);
                    }
                }
            }
            return d;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el despacho.", e);
        }
    }
}
