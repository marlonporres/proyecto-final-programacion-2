package gt.edu.umg.ventas.controlador;
import gt.edu.umg.ventas.dao.*;
import gt.edu.umg.ventas.modelo.*;
import gt.edu.umg.ventas.servicio.*;
import java.util.List;

public class DespachoController {
    private final DespachoService despachos = new DespachoService();
    private final OrdenVentaService ordenes = new OrdenVentaService();
    public List<OrdenVenta> obtenerOrdenesPendientes() { return ordenes.obtenerPendientes(); }
    public OrdenVenta obtenerOrdenConDetalles(int id) { return ordenes.obtenerOrden(id); }
    public List<Bodega> obtenerBodegas() { return new BodegaDAOImpl().obtenerTodos().stream().filter(Bodega::isActiva).toList(); }
    public Despacho obtenerPorOrden(int id) { return despachos.obtenerPorOrden(id); }
    public Despacho generarDespacho(OrdenVenta orden, Bodega bodega, List<DetalleDespacho> detalles) throws Exception {
        Despacho d = new Despacho(); d.setOrden(orden); d.setBodega(bodega); d.setDetalles(detalles);
        despachos.generarDespacho(d); return d;
    }
    public Despacho confirmarDespacho(int id) throws Exception { return despachos.confirmarDespacho(id); }
}
