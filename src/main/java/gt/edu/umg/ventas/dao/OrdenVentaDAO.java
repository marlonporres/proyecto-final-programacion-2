package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.OrdenVenta;
import gt.edu.umg.ventas.modelo.OrdenVentaResumen;

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
}
