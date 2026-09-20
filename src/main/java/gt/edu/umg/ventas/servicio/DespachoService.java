package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.ConexionBD;
import gt.edu.umg.ventas.modelo.Despacho;
import gt.edu.umg.ventas.modelo.DetalleDespacho;
import gt.edu.umg.ventas.modelo.EstadoDespacho;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DespachoService {

    private final InventarioService inventarioService;

    public DespachoService() {
        this.inventarioService = new InventarioService();
    }

    public DespachoService(InventarioService inventarioService) {
        this.inventarioService = inventarioService != null ? inventarioService : new InventarioService();
    }

    public void confirmarDespacho(Despacho despacho) throws Exception {
        if (despacho == null || despacho.getDetalles() == null || despacho.getDetalles().isEmpty()) {
            throw new Exception("El despacho debe tener al menos un detalle.");
        }
        if (despacho.getOrden() == null) {
            throw new IllegalArgumentException("El despacho debe estar asociado a una orden de venta.");
        }
        if (despacho.getBodega() == null) {
            throw new IllegalArgumentException("Debe seleccionar una bodega para el despacho.");
        }

        if (despacho.getNumeroDespacho() == null || despacho.getNumeroDespacho().isBlank()) {
            despacho.setNumeroDespacho("DSP-" + System.currentTimeMillis() % 1000000);
        }
        if (despacho.getFechaDespacho() == null) {
            despacho.setFechaDespacho(LocalDateTime.now());
        }

        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);
            try {
                // 1. Validar y descontar inventario en la bodega seleccionada
                for (DetalleDespacho det : despacho.getDetalles()) {
                    if (det.getCantidadDespachada() <= 0) {
                        throw new IllegalArgumentException("La cantidad despachada para " 
                                + det.getProducto().getNombre() + " debe ser mayor a cero.");
                    }
                    if (det.getCantidadDespachada() > det.getCantidadSolicitada()) {
                        throw new IllegalArgumentException("La cantidad despachada (" + det.getCantidadDespachada() 
                                + ") supera la solicitada (" + det.getCantidadSolicitada() + ") para " 
                                + det.getProducto().getNombre());
                    }

                    // Descuenta existencia_actual y genera MovimientoInventario de tipo SALIDA
                    inventarioService.registrarSalida(
                            det.getProducto(),
                            despacho.getBodega(),
                            det.getCantidadDespachada(),
                            "Despacho " + despacho.getNumeroDespacho(),
                            conn
                    );
                }

                despacho.setEstado(EstadoDespacho.CONFIRMADO);

                // 2. Insertar encabezado de Despacho
                String sql = "INSERT INTO dbo.Despacho (numero_despacho, id_orden, id_bodega, fecha_despacho, estado) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, despacho.getNumeroDespacho());
                    stmt.setInt(2, despacho.getOrden().getId());
                    stmt.setInt(3, despacho.getBodega().getId());
                    stmt.setTimestamp(4, Timestamp.valueOf(despacho.getFechaDespacho()));
                    stmt.setString(5, despacho.getEstado().name());
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            despacho.setId(rs.getInt(1));
                        }
                    }
                }

                // 3. Insertar Detalles de Despacho
                String sqlDet = "INSERT INTO dbo.DetalleDespacho (id_despacho, id_producto, cantidad_solicitada, cantidad_despachada) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmtDet = conn.prepareStatement(sqlDet)) {
                    for (DetalleDespacho det : despacho.getDetalles()) {
                        stmtDet.setInt(1, despacho.getId());
                        stmtDet.setLong(2, det.getProducto().getIdProducto());
                        stmtDet.setInt(3, det.getCantidadSolicitada());
                        stmtDet.setInt(4, det.getCantidadDespachada());
                        stmtDet.addBatch();
                    }
                    stmtDet.executeBatch();
                }

                // 4. Actualizar estado de la Orden de Venta a COMPLETADA
                String sqlUpdateOrden = "UPDATE dbo.OrdenVenta SET estado = 'COMPLETADA' WHERE id = ?";
                try (PreparedStatement stmtOrden = conn.prepareStatement(sqlUpdateOrden)) {
                    stmtOrden.setInt(1, despacho.getOrden().getId());
                    stmtOrden.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
