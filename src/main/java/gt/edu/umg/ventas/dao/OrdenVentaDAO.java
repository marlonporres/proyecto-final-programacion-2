package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.OrdenVenta;
import java.util.List;

public interface OrdenVentaDAO {
    void crear(OrdenVenta ordenVenta);
    OrdenVenta obtener(int id);
    List<OrdenVenta> obtenerTodos();
    void actualizar(OrdenVenta ordenVenta);
    void eliminar(int id);
    List<gt.edu.umg.ventas.modelo.OrdenVentaResumen> buscarPorFecha(java.time.LocalDateTime desde, java.time.LocalDateTime hasta);
}

