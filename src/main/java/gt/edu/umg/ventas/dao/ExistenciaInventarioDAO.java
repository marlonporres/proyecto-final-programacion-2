package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.ExistenciaInventario;
import java.util.List;

public interface ExistenciaInventarioDAO {
    void crear(ExistenciaInventario existenciaInventario);
    ExistenciaInventario obtener(int id);
    List<ExistenciaInventario> obtenerTodos();
    void actualizar(ExistenciaInventario existenciaInventario);
    void eliminar(int id);
}
