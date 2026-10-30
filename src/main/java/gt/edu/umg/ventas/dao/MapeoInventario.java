package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.*;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Mapeo común de las relaciones de inventario, recuperadas en una sola consulta. */
final class MapeoInventario {
    private MapeoInventario() { }

    static final String COLUMNAS = "p.id_producto,p.codigo,p.nombre AS producto_nombre,p.descripcion,p.precio_venta,p.activo,"
            + "c.id_categoria,c.nombre AS categoria_nombre,c.descripcion AS categoria_descripcion,c.activa AS categoria_activa,"
            + "b.id AS bodega_id,b.nombre AS bodega_nombre,b.ubicacion,b.activa AS bodega_activa ";

    static String relaciones(String alias) {
        return " JOIN dbo.producto p ON p.id_producto=" + alias + ".id_producto"
                + " JOIN dbo.categoria c ON c.id_categoria=p.categoria_id"
                + " JOIN dbo.Bodega b ON b.id=" + alias + ".id_bodega ";
    }

    static Producto producto(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(rs.getLong("id_categoria"), rs.getString("categoria_nombre"),
                rs.getString("categoria_descripcion"), rs.getBoolean("categoria_activa"));
        return new Producto(rs.getLong("id_producto"), rs.getString("codigo"), rs.getString("producto_nombre"),
                rs.getString("descripcion"), rs.getBigDecimal("precio_venta"), rs.getBoolean("activo"), categoria);
    }

    static Bodega bodega(ResultSet rs) throws SQLException {
        Bodega bodega = new Bodega();
        bodega.setId(rs.getInt("bodega_id")); bodega.setNombre(rs.getString("bodega_nombre"));
        bodega.setUbicacion(rs.getString("ubicacion")); bodega.setActiva(rs.getBoolean("bodega_activa"));
        return bodega;
    }
}
