package gt.edu.umg.ventas.controlador;

import gt.edu.umg.ventas.dao.ProductoDAO;
import gt.edu.umg.ventas.dao.ProductoDAOImpl;
import gt.edu.umg.ventas.modelo.Producto;

import java.util.List;

public class ProductoController {

    private final ProductoDAO productoDAO;

    public ProductoController() {
        this.productoDAO = new ProductoDAOImpl();
    }

    public List<Producto> listar() {
        return productoDAO.listar();
    }

    public List<Producto> buscar(String criterio) {
        return productoDAO.buscarPorTexto(criterio);
    }

    public Producto buscarPorCodigo(String codigo) {
        return productoDAO.buscarPorCodigo(codigo);
    }
}
