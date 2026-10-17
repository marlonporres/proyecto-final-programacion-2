package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.*;
import gt.edu.umg.ventas.modelo.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/** Generar no mueve stock. Confirmar consume el despacho persistido, no datos editables de la vista. */
public class DespachoService {
    private final InventarioService inventario;
    private final DespachoDAO despachoDAO;
    private final OrdenVentaDAO ordenDAO;
    private final ExistenciaInventarioDAO existenciaDAO;

    public DespachoService() {
        this(new InventarioService());
    }

    public DespachoService(InventarioService inventario) {
        this(inventario, new DespachoDAOImpl(), new OrdenVentaDAOImpl(), new ExistenciaInventarioDAOImpl());
    }

    public DespachoService(InventarioService inventario, DespachoDAO despachoDAO,
                           OrdenVentaDAO ordenDAO, ExistenciaInventarioDAO existenciaDAO) {
        this.inventario = Objects.requireNonNull(inventario);
        this.despachoDAO = Objects.requireNonNull(despachoDAO);
        this.ordenDAO = Objects.requireNonNull(ordenDAO);
        this.existenciaDAO = Objects.requireNonNull(existenciaDAO);
    }

    public Despacho obtenerPorOrden(int idOrden) {
        return despachoDAO.obtenerPorOrden(idOrden);
    }

    public void generarDespacho(Despacho d) throws Exception {
        validarCabecera(d);
        if (d.getId() != null) throw new IllegalStateException("El despacho ya está guardado.");
        int id;
        String numero = "DSP-" + UUID.randomUUID();
        LocalDateTime fecha = LocalDateTime.now();
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                OrdenVenta orden = obtenerOrdenPendienteParaActualizar(con, d.getOrden().getId());
                if (despachoDAO.existePorOrden(con, d.getOrden().getId())) {
                    throw new IllegalStateException("La orden ya tiene un despacho generado.");
                }
                validarBodega(con, d.getBodega().getId());
                validarDetalles(orden, d);
                List<DetalleDespacho> detallesOrdenados = ordenados(d);
                for (DetalleDespacho linea : detallesOrdenados) {
                    if (!inventario.hayDisponibilidad(linea.getProducto(), d.getBodega(), linea.getCantidadSolicitada(), con))
                        throw new IllegalStateException("Stock insuficiente para generar el despacho.");
                }
                id = despachoDAO.insertarCabecera(con, numero, d.getOrden().getId(), d.getBodega().getId(), fecha);
                despachoDAO.insertarDetalles(con, id, detallesOrdenados);
                con.commit();
            } catch (Exception e) {
                rollback(con, e);
                throw e;
            }
        }
        d.setId(id);
        d.setNumeroDespacho(numero);
        d.setFechaDespacho(fecha);
        d.setEstado(EstadoDespacho.PENDIENTE);
        for (DetalleDespacho linea : d.getDetalles()) {
            linea.setDespacho(d);
            linea.setCantidadDespachada(0);
        }
    }

    public void confirmarDespacho(Despacho d) throws Exception {
        validarCabecera(d);
        if (d.getId() == null) throw new IllegalStateException("Genere y guarde el despacho antes de confirmarlo.");
        Despacho confirmado = confirmarDespacho(d.getId());
        d.setEstado(confirmado.getEstado());
        d.setFechaDespacho(confirmado.getFechaDespacho());
        d.setDetalles(confirmado.getDetalles());
        d.getOrden().setEstado(EstadoOrdenVenta.COMPLETADA);
    }

    public Despacho confirmarDespacho(int idDespacho) throws Exception {
        // Siempre bloquear orden antes de despacho: mismo orden de locks que generación/facturación.
        Despacho previo = despachoDAO.obtener(idDespacho);
        if (previo == null) throw new IllegalStateException("Despacho no encontrado.");
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                OrdenVenta orden = obtenerOrdenPendienteParaActualizar(con, previo.getOrden().getId());
                Despacho d = despachoDAO.obtenerPorIdParaActualizar(con, idDespacho);
                if (d == null || d.getEstado() != EstadoDespacho.PENDIENTE) {
                    throw new IllegalStateException("Solo se puede confirmar un despacho pendiente.");
                }
                validarBodega(con, d.getBodega().getId());
                validarDetalles(orden, d);
                for (DetalleDespacho linea : ordenados(d)) {
                    inventario.registrarSalida(linea.getProducto(), d.getBodega(), linea.getCantidadSolicitada(),
                            "Despacho " + d.getNumeroDespacho(), con);
                }
                despachoDAO.marcarDetallesDespachados(con, idDespacho);
                despachoDAO.confirmarEstado(con, idDespacho);
                ordenDAO.cambiarEstado(con, d.getOrden().getId(), EstadoOrdenVenta.COMPLETADA);
                con.commit();
            } catch (Exception e) {
                rollback(con, e);
                throw e;
            }
        }
        return despachoDAO.obtener(idDespacho);
    }

    private void validarCabecera(Despacho d) {
        if (d == null || d.getDetalles() == null || d.getDetalles().isEmpty())
            throw new IllegalArgumentException("El despacho debe tener al menos un detalle.");
        if (d.getOrden() == null || d.getOrden().getId() == null)
            throw new IllegalArgumentException("Seleccione una orden guardada.");
        if (d.getBodega() == null || d.getBodega().getId() == null)
            throw new IllegalArgumentException("Seleccione una bodega.");
    }

    private OrdenVenta obtenerOrdenPendienteParaActualizar(Connection con, int idOrden) {
        OrdenVenta orden = ordenDAO.obtenerPorIdParaActualizar(con, idOrden);
        if (orden == null || orden.getEstado() != EstadoOrdenVenta.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden despachar órdenes pendientes.");
        }
        return orden;
    }

    private void validarBodega(Connection con, int idBodega) {
        if (!existenciaDAO.existeBodegaActiva(con, idBodega)) {
            throw new IllegalStateException("La bodega está inactiva.");
        }
    }

    private void validarDetalles(OrdenVenta orden, Despacho d) {
        Map<Long, Integer> solicitadas = new HashMap<>();
        for (DetalleOrdenVenta od : orden.getDetalles()) {
            if (od.getProducto() == null || !od.getProducto().isActivo()) {
                throw new IllegalStateException("La orden contiene un producto inactivo.");
            }
            solicitadas.put(od.getProducto().getIdProducto(), od.getCantidad());
        }
        Set<Long> vistos = new HashSet<>();
        for (DetalleDespacho linea : d.getDetalles()) {
            if (linea == null || linea.getProducto() == null || !vistos.add(linea.getProducto().getIdProducto())
                    || !Integer.valueOf(linea.getCantidadSolicitada()).equals(solicitadas.get(linea.getProducto().getIdProducto()))
                    || (linea.getCantidadDespachada() != 0 && linea.getCantidadDespachada() != linea.getCantidadSolicitada()))
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
