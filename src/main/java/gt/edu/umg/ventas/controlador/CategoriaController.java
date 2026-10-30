package gt.edu.umg.ventas.controlador;

import gt.edu.umg.ventas.dao.CategoriaDAO;
import gt.edu.umg.ventas.dao.CategoriaDAOImpl;
import gt.edu.umg.ventas.modelo.Categoria;

import java.util.List;

public class CategoriaController {

    private final CategoriaDAO categoriaDAO;

    public CategoriaController() {
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    public List<Categoria> listar() {
        return categoriaDAO.listar();
    }
}
