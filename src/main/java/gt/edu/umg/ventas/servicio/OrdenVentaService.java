package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.OrdenVentaDAO;
import gt.edu.umg.ventas.dao.OrdenVentaDAOImpl;
import gt.edu.umg.ventas.dao.ConexionBD;
import gt.edu.umg.ventas.modelo.OrdenVenta;
import gt.edu.umg.ventas.modelo.OrdenVentaResumen;
import gt.edu.umg.ventas.modelo.DetalleOrdenVenta;
import gt.edu.umg.ventas.modelo.EstadoOrdenVenta;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.sql.Connection;
import java.sql.SQLException;

import java.time.LocalDateTime;
import java.util.List;

public class OrdenVentaService {
    private final OrdenVentaDAO ordenVentaDAO;
    private final InventarioService inventarioService;

    public OrdenVentaService() {
        this(new OrdenVentaDAOImpl(), new InventarioService());
    }

    public OrdenVentaService(OrdenVentaDAO ordenVentaDAO) {
        this(ordenVentaDAO, new InventarioService());
    }

    public OrdenVentaService(OrdenVentaDAO dao, InventarioService inventario) {
        this.ordenVentaDAO = java.util.Objects.requireNonNull(dao);
        this.inventarioService = java.util.Objects.requireNonNull(inventario);
    }

    public void crearOrdenVenta(OrdenVenta orden) {
        if (orden == null) {
            throw new IllegalArgumentException("La orden de venta no puede ser nula.");
        }
        if (orden.getId() != null) {
            throw new IllegalStateException("La orden ya está guardada. No se puede crear nuevamente.");
        }
        if (orden.getCliente() == null || orden.getCliente().getIdCliente() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un cliente para la orden de venta.");
        }
        if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La orden de venta debe tener al menos una línea de detalle.");
        }
        if (orden.getUsuario() == null || orden.getUsuario().getIdUsuario() <= 0 || !orden.getUsuario().isActivo()) {
            throw new IllegalArgumentException("Debe seleccionar un usuario activo.");
        }
        HashSet<Long> productos = new HashSet<>();
        for (DetalleOrdenVenta d : orden.getDetalles()) {
            if (d == null || d.getProducto() == null || !d.getProducto().isActivo()
                    || d.getProducto().getIdProducto() <= 0 || d.getCantidad() <= 0
                    || d.getPrecioUnitario() == null || d.getPrecioUnitario().signum() < 0) {
                throw new IllegalArgumentException("Detalle de orden inválido o producto inactivo.");
            }
            if (!productos.add(d.getProducto().getIdProducto())) {
                throw new IllegalArgumentException("Cada producto debe aparecer una sola vez en la orden.");
            }
            if (inventarioService.obtenerDisponibilidadTotal(d.getProducto().getIdProducto()) < d.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente para " + d.getProducto().getNombre());
            }
            d.setOrden(orden);
            d.setPrecioUnitario(d.getPrecioUnitario().setScale(2, RoundingMode.HALF_UP));
            d.setSubtotal(d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()))
                    .setScale(2, RoundingMode.HALF_UP));
        }
        orden.setEstado(EstadoOrdenVenta.PENDIENTE);
        orden.calcularTotal();
        ordenVentaDAO.crear(orden);
    }

    public OrdenVenta obtenerOrden(int id) {
        return ordenVentaDAO.obtener(id);
    }

    /** Edita sólo órdenes pendientes sin documentos, conservando precios históricos. */
    public void actualizarOrdenVenta(OrdenVenta propuesta) {
        validarPropuestaEdicion(propuesta);
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                OrdenVenta anterior = obtenerParaModificar(con, propuesta.getId());
                Map<Long, BigDecimal> preciosAnteriores = new HashMap<>();
                for (DetalleOrdenVenta linea : anterior.getDetalles()) {
                    preciosAnteriores.put(linea.getProducto().getIdProducto(), linea.getPrecioUnitario());
                }
                HashSet<Long> productos = new HashSet<>();
                for (DetalleOrdenVenta linea : propuesta.getDetalles()) {
                    long productoId = linea.getProducto().getIdProducto();
                    if (!productos.add(productoId)) {
                        throw new IllegalArgumentException("Cada producto debe aparecer una sola vez en la orden.");
                    }
                    BigDecimal precio = preciosAnteriores.getOrDefault(productoId, linea.getPrecioUnitario());
                    if (precio == null || precio.signum() < 0) {
                        throw new IllegalArgumentException("El precio del producto no es válido.");
                    }
                    if (inventarioService.obtenerDisponibilidadTotal(productoId) < linea.getCantidad()) {
                        throw new IllegalStateException("Stock insuficiente para " + linea.getProducto().getNombre());
                    }
                    linea.setPrecioUnitario(precio.setScale(2, RoundingMode.HALF_UP));
                    linea.setSubtotal(linea.getPrecioUnitario().multiply(BigDecimal.valueOf(linea.getCantidad()))
                            .setScale(2, RoundingMode.HALF_UP));
                    linea.setOrden(propuesta);
                }
                propuesta.setNumeroOrden(anterior.getNumeroOrden());
                propuesta.setFecha(anterior.getFecha());
                propuesta.setUsuario(anterior.getUsuario());
                propuesta.setEstado(EstadoOrdenVenta.PENDIENTE);
                propuesta.calcularTotal();
                ordenVentaDAO.actualizarCompleta(con, propuesta);
                con.commit();
            } catch (RuntimeException e) {
                revertir(con, e);
                throw e;
            } catch (SQLException e) {
                revertir(con, e);
                throw new IllegalStateException("No se pudo guardar la edición de la orden.", e);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo conectar para editar la orden.", e);
        }
    }

    /** Cancelación lógica e idempotente; no elimina detalles ni modifica inventario. */
    public void cancelarOrdenVenta(int id) {
        if (id <= 0) throw new IllegalArgumentException("El identificador de la orden no es válido.");
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                OrdenVenta orden = ordenVentaDAO.obtenerPorIdParaActualizar(con, id);
                if (orden == null) throw new NoSuchElementException("La orden de venta no existe.");
                if (orden.getEstado() == EstadoOrdenVenta.CANCELADA) {
                    con.commit();
                    return;
                }
                verificarModificable(con, orden);
                ordenVentaDAO.cambiarEstado(con, id, EstadoOrdenVenta.CANCELADA);
                con.commit();
            } catch (RuntimeException e) {
                revertir(con, e);
                throw e;
            } catch (SQLException e) {
                revertir(con, e);
                throw new IllegalStateException("No se pudo cancelar la orden.", e);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo conectar para cancelar la orden.", e);
        }
    }

    private OrdenVenta obtenerParaModificar(Connection con, int id) {
        OrdenVenta orden = ordenVentaDAO.obtenerPorIdParaActualizar(con, id);
        if (orden == null) throw new NoSuchElementException("La orden de venta no existe.");
        verificarModificable(con, orden);
        return orden;
    }

    private void verificarModificable(Connection con, OrdenVenta orden) {
        if (orden.getEstado() != EstadoOrdenVenta.PENDIENTE
                || ordenVentaDAO.tieneDocumentosRelacionados(con, orden.getId())) {
            throw new IllegalStateException("Sólo se puede modificar una orden pendiente sin despacho ni factura.");
        }
    }

    private void validarPropuestaEdicion(OrdenVenta orden) {
        if (orden == null || orden.getId() == null || orden.getId() <= 0) {
            throw new IllegalArgumentException("La orden de venta no es válida para editar.");
        }
        if (orden.getCliente() == null || orden.getCliente().getIdCliente() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un cliente.");
        }
        if (orden.getObservaciones() != null && orden.getObservaciones().length() > 500) {
            throw new IllegalArgumentException("Las observaciones no pueden exceder 500 caracteres.");
        }
        if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un producto a la orden.");
        }
        for (DetalleOrdenVenta linea : orden.getDetalles()) {
            if (linea == null || linea.getProducto() == null || linea.getProducto().getIdProducto() <= 0
                    || linea.getCantidad() <= 0) {
                throw new IllegalArgumentException("Los productos y sus cantidades deben ser válidos.");
            }
        }
    }

    private static void revertir(Connection con, Exception causa) {
        try { con.rollback(); } catch (SQLException fallo) { causa.addSuppressed(fallo); }
    }

    public List<OrdenVenta> obtenerPendientes() {
        return ordenVentaDAO.obtenerPendientes();
    }

    public List<OrdenVentaResumen> buscarPorFecha(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Las fechas 'Desde' y 'Hasta' son obligatorias.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'Desde' no puede ser mayor que 'Hasta'.");
        }
        return ordenVentaDAO.buscarPorFecha(desde, hasta);
    }
}
