package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrdenVentaDAOImpl implements OrdenVentaDAO {
    @Override
    public void crear(OrdenVenta o) {
        String sql = "INSERT INTO OrdenVenta (numero_orden, fecha, id_cliente, id_usuario, total, estado, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            conn.setAutoCommit(false);
            try {
                stmt.setString(1, o.getNumeroOrden());
                stmt.setTimestamp(2, Timestamp.valueOf(o.getFecha()));
                stmt.setLong(3, o.getCliente().getIdCliente());
                stmt.setLong(4, o.getUsuario().getIdUsuario());
                stmt.setBigDecimal(5, o.getTotal());
                stmt.setString(6, o.getEstado().name());
                stmt.setString(7, o.getObservaciones());
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    o.setId(rs.getInt(1));
                }
                
                String sqlDet = "INSERT INTO DetalleOrdenVenta (id_orden, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmtDet = conn.prepareStatement(sqlDet)) {
                    for (DetalleOrdenVenta det : o.getDetalles()) {
                        stmtDet.setInt(1, o.getId());
                        stmtDet.setLong(2, det.getProducto().getIdProducto());
                        stmtDet.setInt(3, det.getCantidad());
                        stmtDet.setBigDecimal(4, det.getPrecioUnitario());
                        stmtDet.setBigDecimal(5, det.getSubtotal());
                        stmtDet.addBatch();
                    }
                    stmtDet.executeBatch();
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public OrdenVenta obtener(int id) {
        // Implementacion simplificada (requiere obtener detalles)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<OrdenVenta> obtenerTodos() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void actualizar(OrdenVenta o) {
        String sql = "UPDATE OrdenVenta SET estado = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, o.getEstado().name());
            stmt.setInt(2, o.getId());
            stmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void eliminar(int id) {
    }

    @Override
    public List<OrdenVentaResumen> buscarPorFecha(LocalDateTime desde, LocalDateTime hasta) {
        List<OrdenVentaResumen> lista = new ArrayList<>();
        String sql = "SELECT o.id, o.numero_orden, o.fecha, c.nombre as cliente, o.estado, o.total, " +
                     "ISNULL(d.estado, 'NO DESPACHADO') as estado_despacho, ISNULL(b.nombre, 'N/A') as bodega " +
                     "FROM OrdenVenta o " +
                     "INNER JOIN Cliente c ON o.id_cliente = c.id " +
                     "LEFT JOIN Despacho d ON d.id_orden = o.id " +
                     "LEFT JOIN Bodega b ON d.id_bodega = b.id " +
                     "WHERE o.fecha >= ? AND o.fecha < ?";
                     
        LocalDateTime hastaFin = hasta.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime desdeInicio = desde.withHour(0).withMinute(0).withSecond(0).withNano(0);

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(desdeInicio));
            stmt.setTimestamp(2, Timestamp.valueOf(hastaFin));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrdenVentaResumen r = new OrdenVentaResumen();
                    r.setId(rs.getInt("id"));
                    r.setNumeroOrden(rs.getString("numero_orden"));
                    r.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    r.setNombreCliente(rs.getString("cliente"));
                    r.setEstado(EstadoOrdenVenta.valueOf(rs.getString("estado")));
                    r.setTotal(rs.getBigDecimal("total"));
                    r.setEstadoDespacho(rs.getString("estado_despacho"));
                    r.setNombreBodega(rs.getString("bodega"));
                    lista.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error en consulta de fechas: " + e.getMessage());
        }
        return lista;
    }
}


