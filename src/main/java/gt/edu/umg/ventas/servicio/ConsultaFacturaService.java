package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.ConexionBD;
import gt.edu.umg.ventas.modelo.FacturaResumen;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Consulta de solo lectura de facturas con filtros opcionales. */
public class ConsultaFacturaService {

    /** Línea de detalle de una factura (para el botón "Ver detalle"). */
    public record LineaFactura(String codigo, String producto, BigDecimal cantidad,
                               BigDecimal precioUnitario, BigDecimal subtotal) { }

    /**
     * Busca facturas. Todos los parámetros son opcionales (null o vacío = sin filtro).
     * El rango de fechas es [desde, hasta): hasta es exclusivo.
     */
    public List<FacturaResumen> buscar(LocalDateTime desde, LocalDateTime hasta, String cliente,
                                       String numeroOrden, String estadoFactura, String estadoOrden) {
        StringBuilder sql = new StringBuilder(
                "SELECT f.id_factura, f.numero, f.fecha_hora, c.nit, c.nombre, o.numero_orden, "
                + "o.estado AS estado_orden, f.estado AS estado_factura, f.total, COALESCE(p.pagado, 0) AS pagado "
                + "FROM dbo.factura f "
                + "JOIN dbo.cliente c ON c.id_cliente = f.cliente_id "
                + "JOIN dbo.OrdenVenta o ON o.id = f.id_orden "
                + "LEFT JOIN (SELECT factura_id, SUM(monto) AS pagado FROM dbo.pago GROUP BY factura_id) p "
                + "ON p.factura_id = f.id_factura WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();

        if (desde != null) {
            sql.append("AND f.fecha_hora >= ? ");
            params.add(Timestamp.valueOf(desde));
        }
        if (hasta != null) {
            sql.append("AND f.fecha_hora < ? ");
            params.add(Timestamp.valueOf(hasta));
        }
        if (hayTexto(cliente)) {
            sql.append("AND (c.nit LIKE ? OR c.nombre LIKE ?) ");
            String patron = "%" + cliente.trim() + "%";
            params.add(patron);
            params.add(patron);
        }
        if (hayTexto(numeroOrden)) {
            sql.append("AND o.numero_orden LIKE ? ");
            params.add("%" + numeroOrden.trim() + "%");
        }
        if (hayTexto(estadoFactura)) {
            sql.append("AND f.estado = ? ");
            params.add(estadoFactura.trim());
        }
        if (hayTexto(estadoOrden)) {
            sql.append("AND o.estado = ? ");
            params.add(estadoOrden.trim());
        }
        sql.append("ORDER BY f.fecha_hora DESC, f.id_factura DESC");

        List<FacturaResumen> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String estadoFac = rs.getString("estado_factura");
                    BigDecimal total = rs.getBigDecimal("total");
                    BigDecimal pagado = rs.getBigDecimal("pagado");
                    // Una factura anulada ya no tiene saldo por cobrar.
                    BigDecimal saldo = "ANULADA".equals(estadoFac)
                            ? BigDecimal.ZERO.setScale(2)
                            : total.subtract(pagado).max(BigDecimal.ZERO);
                    lista.add(new FacturaResumen(
                            rs.getLong("id_factura"),
                            rs.getString("numero"),
                            rs.getTimestamp("fecha_hora").toLocalDateTime(),
                            rs.getString("nit"),
                            rs.getString("nombre"),
                            rs.getString("numero_orden"),
                            rs.getString("estado_orden"),
                            estadoFac,
                            total, pagado, saldo));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar las facturas: " + e.getMessage(), e);
        }
        return List.copyOf(lista);
    }

    public List<LineaFactura> detalle(long idFactura) {
        String sql = "SELECT p.codigo, p.nombre, d.cantidad, d.precio_unitario, d.subtotal "
                + "FROM dbo.detalle_factura d JOIN dbo.producto p ON p.id_producto = d.producto_id "
                + "WHERE d.factura_id = ? ORDER BY d.id_detalle";
        List<LineaFactura> lineas = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idFactura);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lineas.add(new LineaFactura(rs.getString(1), rs.getString(2), rs.getBigDecimal(3),
                            rs.getBigDecimal(4), rs.getBigDecimal(5)));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el detalle de la factura.", e);
        }
        return List.copyOf(lineas);
    }

    private static boolean hayTexto(String s) {
        return s != null && !s.isBlank();
    }
}
