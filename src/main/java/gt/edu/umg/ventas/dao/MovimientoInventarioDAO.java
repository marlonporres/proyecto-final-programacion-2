package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.MovimientoInventario;
import java.util.List;

public interface MovimientoInventarioDAO {
    void crear(MovimientoInventario movimientoInventario);
    MovimientoInventario obtener(int id);
    List<MovimientoInventario> obtenerTodos();
    void actualizar(MovimientoInventario movimientoInventario);
    void eliminar(int id);
}
