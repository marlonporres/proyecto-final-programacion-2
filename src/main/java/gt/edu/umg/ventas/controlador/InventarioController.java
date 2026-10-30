package gt.edu.umg.ventas.controlador;

import gt.edu.umg.ventas.dao.BodegaDAO;
import gt.edu.umg.ventas.dao.BodegaDAOImpl;
import gt.edu.umg.ventas.modelo.Bodega;
import gt.edu.umg.ventas.modelo.InventarioResumen;
import gt.edu.umg.ventas.modelo.Producto;
import gt.edu.umg.ventas.dao.ProductoDAOImpl;
import gt.edu.umg.ventas.servicio.InventarioService;

import java.util.List;

public class InventarioController {

    private final InventarioService inventarioService;
    private final BodegaDAO bodegaDAO;

    public InventarioController() {
        this.inventarioService = new InventarioService();
        this.bodegaDAO = new BodegaDAOImpl();
    }

    public List<Bodega> obtenerBodegas() {
        return bodegaDAO.obtenerTodos();
    }

    public List<InventarioResumen> listarExistencias(Integer idBodega, String filtroTexto) {
        return inventarioService.listarExistencias(idBodega, filtroTexto);
    }

    public List<Producto> obtenerProductosActivos() {
        return new ProductoDAOImpl().listar().stream().filter(Producto::isActivo).toList();
    }

    public List<Bodega> obtenerBodegasActivas() {
        return obtenerBodegas().stream().filter(Bodega::isActiva).toList();
    }

    public void registrarEntrada(Producto producto, Bodega bodega, int cantidad, String referencia) throws Exception {
        inventarioService.registrarEntrada(producto, bodega, cantidad, referencia);
    }
}
