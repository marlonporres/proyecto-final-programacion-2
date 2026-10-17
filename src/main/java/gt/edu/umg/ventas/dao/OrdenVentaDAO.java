package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.EstadoOrdenVenta;
import gt.edu.umg.ventas.modelo.OperacionLinea;
import gt.edu.umg.ventas.modelo.OrdenVenta;
import gt.edu.umg.ventas.modelo.OrdenVentaResumen;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public interface OrdenVentaDAO {
    void crear(OrdenVenta ordenVenta);
    OrdenVenta obtener(int id);
    List<OrdenVenta> obtenerTodos();
    List<OrdenVenta> obtenerPendientes();
    void actualizar(OrdenVenta ordenVenta);
    void eliminar(int id);
    List<OrdenVentaResumen> buscarPorFecha(LocalDateTime desde, LocalDateTime hasta);
    boolean tieneDespachoConfirmado(int idOrden);
    OrdenVenta obtenerPorIdParaActualizar(Connection con, int idOrden);
    void cambiarEstado(Connection con, int idOrden, EstadoOrdenVenta estado);
    List<OperacionLinea> consultarTrazabilidad(int idOrden);
}
