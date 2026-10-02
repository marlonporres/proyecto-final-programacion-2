package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.ConexionBD;
import java.sql.*;
import gt.edu.umg.ventas.modelo.OperacionLinea;
import java.util.ArrayList;
import java.util.List;

/** Consulta de solo lectura de la cadena completa de una orden. */
public class ConsultaOperacionService {
    public List<OperacionLinea> consultar(int idOrden) {
        List<OperacionLinea> lineas = new ArrayList<>();
        String sql = "SELECT o.numero_orden, p.codigo, od.cantidad, dd.cantidad_despachada, od.precio_unitario, b.nombre, "
                + "d.numero_despacho, m.tipo_movimiento, m.cantidad AS salida, f.numero, f.total "
                + "FROM dbo.OrdenVenta o JOIN dbo.DetalleOrdenVenta od ON od.id_orden = o.id "
                + "JOIN dbo.producto p ON p.id_producto = od.id_producto "
                + "LEFT JOIN dbo.Despacho d ON d.id_orden = o.id LEFT JOIN dbo.Bodega b ON b.id = d.id_bodega "
                + "LEFT JOIN dbo.DetalleDespacho dd ON dd.id_despacho = d.id AND dd.id_producto = p.id_producto "
                + "LEFT JOIN dbo.MovimientoInventario m ON m.id_producto = p.id_producto AND m.id_bodega = b.id "
                + "AND m.referencia = CONCAT('Despacho ', d.numero_despacho) AND m.tipo_movimiento = 'SALIDA' "
                + "LEFT JOIN dbo.factura f ON f.id_orden = o.id WHERE o.id = ? ORDER BY od.id";
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lineas.add(new OperacionLinea(rs.getString(1), rs.getString(2), rs.getInt(3),
                            (Integer) rs.getObject(4), rs.getBigDecimal(5), rs.getString(6), rs.getString(7),
                            rs.getString(8), (Integer) rs.getObject(9), rs.getString(10), rs.getBigDecimal(11)));
                }
            }
        } catch (SQLException e) { throw new IllegalStateException("No se pudo consultar la operación.", e); }
        return List.copyOf(lineas);
    }
}
