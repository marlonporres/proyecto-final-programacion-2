package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.Categoria;
import java.util.List;

/**
 * Interfaz de acceso a datos para la entidad Categoria.
 */
public interface CategoriaDAO {

    Categoria buscarPorId(long idCategoria);

    List<Categoria> listar();
}
