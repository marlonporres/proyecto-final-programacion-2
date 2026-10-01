package gt.edu.umg.ventas.dao;
import gt.edu.umg.ventas.modelo.*;
import java.sql.*;

public class DespachoDAOImpl implements DespachoDAO {
    @Override public Despacho obtener(int id) { return consultar("id",id); }
    @Override public Despacho obtenerPorOrden(int idOrden) { return consultar("id_orden",idOrden); }
    private Despacho consultar(String campo, int id) {
        try (Connection con=ConexionBD.obtenerConexion();
             PreparedStatement ps=con.prepareStatement("SELECT * FROM dbo.Despacho WHERE "+campo+"=?")) {
            ps.setInt(1,id);
            Despacho d;
            try (ResultSet rs=ps.executeQuery()) {
                if (!rs.next()) return null;
                d=new Despacho(); d.setId(rs.getInt("id")); d.setNumeroDespacho(rs.getString("numero_despacho"));
                d.setEstado(EstadoDespacho.valueOf(rs.getString("estado")));
                d.setFechaDespacho(rs.getTimestamp("fecha_despacho").toLocalDateTime());
                d.setOrden(new OrdenVentaDAOImpl().obtener(rs.getInt("id_orden")));
                d.setBodega(new BodegaDAOImpl().obtener(rs.getInt("id_bodega")));
            }
            try (PreparedStatement detalles=con.prepareStatement(
                    "SELECT dd.*,p.codigo,p.nombre FROM dbo.DetalleDespacho dd JOIN dbo.producto p ON p.id_producto=dd.id_producto WHERE dd.id_despacho=? ORDER BY dd.id_producto")) {
                detalles.setInt(1,d.getId());
                try (ResultSet rs=detalles.executeQuery()) {
                    while (rs.next()) {
                        Producto p=new Producto(); p.setIdProducto(rs.getLong("id_producto")); p.setCodigo(rs.getString("codigo")); p.setNombre(rs.getString("nombre"));
                        DetalleDespacho linea=new DetalleDespacho(); linea.setDespacho(d); linea.setProducto(p);
                        linea.setCantidadSolicitada(rs.getInt("cantidad_solicitada")); linea.setCantidadDespachada(rs.getInt("cantidad_despachada"));
                        d.getDetalles().add(linea);
                    }
                }
            }
            return d;
        } catch (SQLException e) { throw new IllegalStateException("No se pudo consultar el despacho.",e); }
    }
}
