package gt.edu.umg.ventas.controlador;

import gt.edu.umg.ventas.dao.ClienteDAO;
import gt.edu.umg.ventas.dao.ClienteDAOImpl;
import gt.edu.umg.ventas.dao.ProductoDAO;
import gt.edu.umg.ventas.dao.ProductoDAOImpl;
import gt.edu.umg.ventas.modelo.Cliente;
import gt.edu.umg.ventas.modelo.DetalleOrdenVenta;
import gt.edu.umg.ventas.modelo.EstadoOrdenVenta;
import gt.edu.umg.ventas.modelo.OrdenVenta;
import gt.edu.umg.ventas.modelo.Producto;
import gt.edu.umg.ventas.servicio.OrdenVentaService;
import gt.edu.umg.ventas.util.SesionUsuario;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrdenVentaController {

    private final OrdenVentaService ordenVentaService;
    private final ClienteDAO clienteDAO;
    private final ProductoDAO productoDAO;

    public OrdenVentaController() {
        this.ordenVentaService = new OrdenVentaService();
        this.clienteDAO = new ClienteDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
    }

    public List<Cliente> obtenerClientes() {
        return clienteDAO.listar();
    }

    public List<Producto> obtenerProductosActivos() {
        return productoDAO.listar();
    }

    public OrdenVenta guardarOrden(Cliente cliente, List<DetalleOrdenVenta> detalles, String observaciones) {
        if (cliente == null) {
            throw new IllegalArgumentException("Debe seleccionar un cliente.");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un producto a la orden.");
        }

        OrdenVenta orden = new OrdenVenta();
        // Generación de número correlativo académico con prefijo OV-
        String correlativo = "OV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        orden.setNumeroOrden(correlativo);
        orden.setFecha(LocalDateTime.now());
        orden.setCliente(cliente);
        orden.setUsuario(SesionUsuario.getUsuarioActivo());
        orden.setEstado(EstadoOrdenVenta.PENDIENTE);
        orden.setObservaciones(observaciones);

        for (DetalleOrdenVenta d : detalles) {
            d.setOrden(orden);
            orden.agregarDetalle(d);
        }

        ordenVentaService.crearOrdenVenta(orden);
        return orden;
    }
}
