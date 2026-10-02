package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.*;
import gt.edu.umg.ventas.modelo.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/** Generar no mueve stock. Confirmar consume el despacho persistido, no datos editables de la vista. */
public class DespachoService {
    private final InventarioService inventario;
    public DespachoService() { this(new InventarioService()); }
    public DespachoService(InventarioService inventario) { this.inventario = Objects.requireNonNull(inventario); }

    public Despacho obtenerPorOrden(int idOrden) { return new DespachoDAOImpl().obtenerPorOrden(idOrden); }

    public void generarDespacho(Despacho d) throws Exception {
        validarCabecera(d);
        if (d.getId() != null) throw new IllegalStateException("El despacho ya está guardado.");
        int id;
        String numero = "DSP-" + UUID.randomUUID();
        LocalDateTime fecha = LocalDateTime.now();
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                bloquearOrden(con, d.getOrden().getId());
                try (PreparedStatement ps = con.prepareStatement("SELECT id FROM dbo.Despacho WHERE id_orden=?")) {
                    ps.setInt(1, d.getOrden().getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) throw new IllegalStateException("La orden ya tiene un despacho generado.");
                    }
                }
                validarBodega(con, d.getBodega().getId());
                validarDetalles(con, d);
                for (DetalleDespacho linea : ordenados(d)) {
                    if (!inventario.hayDisponibilidad(linea.getProducto(), d.getBodega(), linea.getCantidadSolicitada(), con))
                        throw new IllegalStateException("Stock insuficiente para generar el despacho.");
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT dbo.Despacho(numero_despacho,id_orden,id_bodega,fecha_despacho,estado) VALUES(?,?,?,?,'PENDIENTE')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, numero); ps.setInt(2, d.getOrden().getId()); ps.setInt(3, d.getBodega().getId());
                    ps.setTimestamp(4, Timestamp.valueOf(fecha)); ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("No se obtuvo el ID de despacho.");
                        id = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT dbo.DetalleDespacho(id_despacho,id_producto,cantidad_solicitada,cantidad_despachada) VALUES(?,?,?,0)")) {
                    for (DetalleDespacho linea : ordenados(d)) {
                        ps.setInt(1, id); ps.setLong(2, linea.getProducto().getIdProducto());
                        ps.setInt(3, linea.getCantidadSolicitada()); ps.addBatch();
                    }
                    ps.executeBatch();
                }
                con.commit();
            } catch (Exception e) { rollback(con, e); throw e; }
        }
        d.setId(id); d.setNumeroDespacho(numero); d.setFechaDespacho(fecha); d.setEstado(EstadoDespacho.PENDIENTE);
        for (DetalleDespacho linea : d.getDetalles()) { linea.setDespacho(d); linea.setCantidadDespachada(0); }
    }

    public void confirmarDespacho(Despacho d) throws Exception {
        validarCabecera(d);
        if (d.getId() == null) throw new IllegalStateException("Genere y guarde el despacho antes de confirmarlo.");
        Despacho confirmado = confirmarDespacho(d.getId());
        d.setEstado(confirmado.getEstado()); d.setFechaDespacho(confirmado.getFechaDespacho());
        d.setDetalles(confirmado.getDetalles()); d.getOrden().setEstado(EstadoOrdenVenta.COMPLETADA);
    }

    public Despacho confirmarDespacho(int idDespacho) throws Exception {
        // Siempre bloquear orden antes de despacho: mismo orden de locks que generación/facturación.
        Despacho d = new DespachoDAOImpl().obtener(idDespacho);
        if (d == null) throw new IllegalStateException("Despacho no encontrado.");
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                bloquearOrden(con, d.getOrden().getId());
                try (PreparedStatement ps = con.prepareStatement("SELECT estado FROM dbo.Despacho WITH (UPDLOCK,HOLDLOCK) WHERE id=?")) {
                    ps.setInt(1, idDespacho);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !"PENDIENTE".equals(rs.getString(1)))
                            throw new IllegalStateException("Solo se puede confirmar un despacho pendiente.");
                    }
                }
                validarBodega(con, d.getBodega().getId());
                validarDetalles(con, d);
                for (DetalleDespacho linea : ordenados(d))
                    inventario.registrarSalida(linea.getProducto(), d.getBodega(), linea.getCantidadSolicitada(),
                            "Despacho " + d.getNumeroDespacho(), con);
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE dbo.DetalleDespacho SET cantidad_despachada=cantidad_solicitada WHERE id_despacho=?")) {
                    ps.setInt(1, idDespacho); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE dbo.Despacho SET estado='CONFIRMADO' WHERE id=? AND estado='PENDIENTE'")) {
                    ps.setInt(1, idDespacho);
                    if (ps.executeUpdate() != 1) throw new IllegalStateException("El despacho cambió de estado.");
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE dbo.OrdenVenta SET estado='COMPLETADA' WHERE id=? AND estado='PENDIENTE'")) {
                    ps.setInt(1, d.getOrden().getId());
                    if (ps.executeUpdate() != 1) throw new IllegalStateException("La orden cambió de estado.");
                }
                con.commit();
            } catch (Exception e) { rollback(con, e); throw e; }
        }
        return new DespachoDAOImpl().obtener(idDespacho);
    }

    private void validarCabecera(Despacho d) {
        if (d == null || d.getDetalles() == null || d.getDetalles().isEmpty())
            throw new IllegalArgumentException("El despacho debe tener al menos un detalle.");
        if (d.getOrden() == null || d.getOrden().getId() == null)
            throw new IllegalArgumentException("Seleccione una orden guardada.");
        if (d.getBodega() == null || d.getBodega().getId() == null)
            throw new IllegalArgumentException("Seleccione una bodega.");
    }
    private void bloquearOrden(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT estado FROM dbo.OrdenVenta WITH (UPDLOCK,HOLDLOCK) WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !"PENDIENTE".equals(rs.getString(1)))
                    throw new IllegalStateException("Solo se pueden despachar órdenes pendientes.");
            }
        }
    }
    private void validarBodega(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM dbo.Bodega WHERE id=? AND activa=1")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("La bodega está inactiva.");
            }
        }
    }
    private void validarDetalles(Connection con, Despacho d) throws SQLException {
        Map<Long,Integer> solicitadas = new HashMap<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT od.id_producto,od.cantidad,p.activo FROM dbo.DetalleOrdenVenta od JOIN dbo.producto p ON p.id_producto=od.id_producto WHERE od.id_orden=?")) {
            ps.setInt(1, d.getOrden().getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!rs.getBoolean(3)) throw new IllegalStateException("La orden contiene un producto inactivo.");
                    solicitadas.put(rs.getLong(1),rs.getInt(2));
                }
            }
        }
        Set<Long> vistos = new HashSet<>();
        for (DetalleDespacho linea : d.getDetalles()) {
            if (linea == null || linea.getProducto() == null || !vistos.add(linea.getProducto().getIdProducto())
                    || !Integer.valueOf(linea.getCantidadSolicitada()).equals(solicitadas.get(linea.getProducto().getIdProducto()))
                    || (linea.getCantidadDespachada()!=0 && linea.getCantidadDespachada()!=linea.getCantidadSolicitada()))
                throw new IllegalArgumentException("El despacho debe contener exactamente los productos y cantidades de la orden.");
        }
        if (!vistos.equals(solicitadas.keySet())) throw new IllegalArgumentException("El despacho debe ser completo.");
    }
    private List<DetalleDespacho> ordenados(Despacho d) {
        return d.getDetalles().stream().sorted(Comparator.comparingLong(x -> x.getProducto().getIdProducto())).toList();
    }
    private void rollback(Connection con, Exception e) {
        try { con.rollback(); } catch (SQLException fallo) { e.addSuppressed(fallo); }
    }
}
