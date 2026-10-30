package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.Despacho;
import gt.edu.umg.ventas.modelo.DetalleDespacho;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

/** Consulta y operaciones transaccionales de despacho. */
public interface DespachoDAO {
    Despacho obtener(int id);
    Despacho obtenerPorOrden(int idOrden);
    boolean existePorOrden(Connection con, int idOrden);
    int insertarCabecera(Connection con, String numero, int idOrden, int idBodega, LocalDateTime fecha);
    void insertarDetalles(Connection con, int idDespacho, List<DetalleDespacho> detallesOrdenados);
    Despacho obtenerPorIdParaActualizar(Connection con, int idDespacho);
    void marcarDetallesDespachados(Connection con, int idDespacho);
    void confirmarEstado(Connection con, int idDespacho);
}
