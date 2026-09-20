package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.ConexionBD;
import gt.edu.umg.ventas.modelo.Despacho;
import gt.edu.umg.ventas.modelo.DetalleDespacho;
import gt.edu.umg.ventas.modelo.EstadoDespacho;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

public class DespachoService {
    private InventarioService inventarioService;

    public DespachoService() {
        this.inventarioService = new InventarioService();
    }

    public void confirmarDespacho(Despacho despacho) throws Exception {
        if (despacho.getDetalles().isEmpty()) {
            throw new Exception("El despacho debe tener al menos un detalle.");
        }
        
        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);
            try {
                // 1. Descontar Inventario
                for (DetalleDespacho det : despacho.getDetalles()) {
                    if (det.getCantidadDespachada() <= 0) {
                        throw new Exception("La cantidad despachada debe ser mayor a cero.");
                    }
                    if (det.getCantidadDespachada() > det.getCantidadSolicitada()) {
                        throw new Exception("La cantidad despachada supera la solicitada.");
                    }
                    inventarioService.registrarSalida(det.getProducto(), despacho.getBodega(), det.getCantidadDespachada(), "Despacho " + despacho.getNumeroDespacho(), conn);
                }
                
                despacho.setEstado(EstadoDespacho.CONFIRMADO);
                
                // 2. Insertar Despacho
                String sql = "INSERT INTO Despacho (numero_despacho, id_orden, id_bodega, fecha_despacho, estado) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, despacho.getNumeroDespacho());
                    stmt.setInt(2, despacho.getOrden().getId());
                    stmt.setInt(3, despacho.getBodega().getId());
                    stmt.setTimestamp(4, java.sql.Timestamp.valueOf(despacho.getFechaDespacho()));
                    stmt.setString(5, despacho.getEstado().name());
                    stmt.executeUpdate();
                    
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        despacho.setId(rs.getInt(1));
                    }
                }
                
                // 3. Insertar Detalles
                String sqlDet = "INSERT INTO DetalleDespacho (id_despacho, id_producto, cantidad_solicitada, cantidad_despachada) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlDet)) {
                    for (DetalleDespacho det : despacho.getDetalles()) {
                        stmt.setInt(1, despacho.getId());
                        stmt.setInt(2, (int)det.getProducto().getIdProducto());
                        stmt.setInt(3, det.getCantidadSolicitada());
                        stmt.setInt(4, det.getCantidadDespachada());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
                
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
}


