package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.ExistenciaInventario;
import gt.edu.umg.ventas.modelo.InventarioResumen;
import java.sql.Connection;
import java.util.List;

public interface ExistenciaInventarioDAO {
    void crear(ExistenciaInventario existenciaInventario);
    ExistenciaInventario obtener(int id);
    List<ExistenciaInventario> obtenerTodos();
    void actualizar(ExistenciaInventario existenciaInventario);
    void eliminar(int id);

    ExistenciaInventario obtenerParaActualizar(Connection con, long idProducto, int idBodega);
    void insertarExistenciaInicial(Connection con, long idProducto, int idBodega, int existenciaActual);
    void actualizarExistenciaActual(Connection con, long idExistencia, int nuevaActual);
    void actualizarExistenciaReservada(Connection con, long idExistencia, int nuevaReservada);
    void actualizarActualYReservada(Connection con, long idExistencia, int nuevaActual, int nuevaReservada);
    boolean existeBodegaActiva(Connection con, int idBodega);
    boolean existeProducto(Connection con, long idProducto);
    List<InventarioResumen> consultarSaldosActivos();
    List<InventarioResumen> listarExistencias(Integer idBodega, String filtroTexto);
    int obtenerDisponibilidadTotal(long idProducto);
}

