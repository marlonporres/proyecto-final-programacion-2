package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.Despacho;
import java.util.List;

public interface DespachoDAO {
    void crear(Despacho despacho);
    Despacho obtener(int id);
    List<Despacho> obtenerTodos();
    void actualizar(Despacho despacho);
    void eliminar(int id);
}
