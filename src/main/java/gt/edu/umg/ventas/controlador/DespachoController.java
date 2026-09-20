package gt.edu.umg.ventas.controlador;

import gt.edu.umg.ventas.dao.BodegaDAO;
import gt.edu.umg.ventas.dao.BodegaDAOImpl;
import gt.edu.umg.ventas.modelo.Bodega;
import gt.edu.umg.ventas.modelo.Despacho;
import gt.edu.umg.ventas.modelo.DetalleDespacho;
import gt.edu.umg.ventas.modelo.OrdenVenta;
import gt.edu.umg.ventas.servicio.DespachoService;
import gt.edu.umg.ventas.servicio.OrdenVentaService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DespachoController {

    private final DespachoService despachoService;
    private final OrdenVentaService ordenVentaService;
    private final BodegaDAO bodegaDAO;

    public DespachoController() {
        this.despachoService = new DespachoService();
        this.ordenVentaService = new OrdenVentaService();
        this.bodegaDAO = new BodegaDAOImpl();
    }

    public List<OrdenVenta> obtenerOrdenesPendientes() {
        return ordenVentaService.obtenerPendientes();
    }

    public OrdenVenta obtenerOrdenConDetalles(int id) {
        return ordenVentaService.obtenerOrden(id);
    }

    public List<Bodega> obtenerBodegas() {
        return bodegaDAO.obtenerTodos();
    }

    public Despacho confirmarDespacho(OrdenVenta orden, Bodega bodega, List<DetalleDespacho> detalles) throws Exception {
        if (orden == null) {
            throw new IllegalArgumentException("Debe seleccionar una orden de venta pendiente.");
        }
        if (bodega == null) {
            throw new IllegalArgumentException("Debe seleccionar una bodega de salida.");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La orden seleccionada no contiene detalles para despachar.");
        }

        Despacho despacho = new Despacho();
        String correlativo = "DSP-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        despacho.setNumeroDespacho(correlativo);
        despacho.setOrden(orden);
        despacho.setBodega(bodega);
        despacho.setFechaDespacho(LocalDateTime.now());
        despacho.setDetalles(detalles);

        for (DetalleDespacho d : detalles) {
            d.setDespacho(despacho);
        }

        despachoService.confirmarDespacho(despacho);
        return despacho;
    }
}
