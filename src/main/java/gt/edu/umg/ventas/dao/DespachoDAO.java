package gt.edu.umg.ventas.dao;
import gt.edu.umg.ventas.modelo.Despacho;
/** Consulta; generación y confirmación deben pasar por el servicio transaccional. */
public interface DespachoDAO {
    Despacho obtener(int id);
    Despacho obtenerPorOrden(int idOrden);
}
