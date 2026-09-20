package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.OrdenVentaDAO;
import gt.edu.umg.ventas.dao.OrdenVentaDAOImpl;
import gt.edu.umg.ventas.modelo.OrdenVenta;
import gt.edu.umg.ventas.modelo.OrdenVentaResumen;

import java.time.LocalDateTime;
import java.util.List;

public class OrdenVentaService {
    private final OrdenVentaDAO ordenVentaDAO;

    public OrdenVentaService() {
        this.ordenVentaDAO = new OrdenVentaDAOImpl();
    }

    public OrdenVentaService(OrdenVentaDAO ordenVentaDAO) {
        this.ordenVentaDAO = ordenVentaDAO != null ? ordenVentaDAO : new OrdenVentaDAOImpl();
    }

    public void crearOrdenVenta(OrdenVenta orden) {
        if (orden == null) {
            throw new IllegalArgumentException("La orden de venta no puede ser nula.");
        }
        if (orden.getCliente() == null) {
            throw new IllegalArgumentException("Debe seleccionar un cliente para la orden de venta.");
        }
        if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La orden de venta debe tener al menos una línea de detalle.");
        }
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
