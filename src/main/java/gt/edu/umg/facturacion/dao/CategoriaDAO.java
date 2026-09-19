package gt.edu.umg.facturacion.dao;

import gt.edu.umg.facturacion.modelo.Categoria;
import java.util.List;

/**
 * Interfaz de acceso a datos para la entidad Categoria.
 */
public interface CategoriaDAO {

    Categoria buscarPorId(long idCategoria);

    List<Categoria> listar();
}
