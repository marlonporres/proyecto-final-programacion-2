package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.Bodega;
import java.util.List;

public interface BodegaDAO {
    void crear(Bodega bodega);
    Bodega obtener(int id);
    List<Bodega> obtenerTodos();
    void actualizar(Bodega bodega);
    void eliminar(int id);
}
