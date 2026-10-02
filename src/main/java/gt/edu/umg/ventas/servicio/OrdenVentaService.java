package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.OrdenVentaDAO;
import gt.edu.umg.ventas.dao.OrdenVentaDAOImpl;
import gt.edu.umg.ventas.modelo.OrdenVenta;
import gt.edu.umg.ventas.modelo.OrdenVentaResumen;
import gt.edu.umg.ventas.modelo.DetalleOrdenVenta;
import gt.edu.umg.ventas.modelo.EstadoOrdenVenta;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;

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
