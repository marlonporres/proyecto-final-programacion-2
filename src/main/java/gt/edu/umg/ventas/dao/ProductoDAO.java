package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.Producto;
import java.math.BigDecimal;
import java.util.List;

/**
 * Interfaz de acceso a datos para la entidad Producto e Inventario asociado.
 */
public interface ProductoDAO {

    Producto buscarPorId(long idProducto);

    Producto buscarPorCodigo(String codigo);

    List<Producto> listar();

    List<Producto> buscarPorTexto(String criterio);

    void actualizarInventario(long idInventario, BigDecimal nuevaExistencia);
}
