package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.ConexionBD;
import gt.edu.umg.ventas.dao.ExistenciaInventarioDAO;
import gt.edu.umg.ventas.dao.ExistenciaInventarioDAOImpl;
import gt.edu.umg.ventas.dao.MovimientoInventarioDAO;
import gt.edu.umg.ventas.dao.MovimientoInventarioDAOImpl;
import gt.edu.umg.ventas.modelo.Bodega;
import gt.edu.umg.ventas.modelo.ExistenciaInventario;
import gt.edu.umg.ventas.modelo.InventarioResumen;
import gt.edu.umg.ventas.modelo.Producto;
import gt.edu.umg.ventas.modelo.TipoMovimientoInventario;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class InventarioService {

    private final ExistenciaInventarioDAO existenciaDAO;
    private final MovimientoInventarioDAO movimientoDAO;

    public InventarioService() {
        this(new ExistenciaInventarioDAOImpl(), new MovimientoInventarioDAOImpl());
    }

    public InventarioService(ExistenciaInventarioDAO existenciaDAO, MovimientoInventarioDAO movimientoDAO) {
        this.existenciaDAO = Objects.requireNonNull(existenciaDAO);
        this.movimientoDAO = Objects.requireNonNull(movimientoDAO);
    }

    public ExistenciaInventario consultarExistencia(Producto producto, Bodega bodega) {
        return consultarExistencia(producto, bodega, null);
    }

    public ExistenciaInventario consultarExistencia(Producto producto, Bodega bodega, Connection conn) {
        boolean localConn = false;
        try {
            if (conn == null) {
                conn = ConexionBD.obtenerConexion();
                localConn = true;
            }
            ExistenciaInventario ex = existenciaDAO.obtenerParaActualizar(conn, producto.getIdProducto(), bodega.getId());
            if (ex != null) {
                ex.setProducto(producto);
                ex.setBodega(bodega);
            }
            return ex;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar existencia: " + e.getMessage(), e);
        } finally {
            if (localConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean hayDisponibilidad(Producto producto, Bodega bodega, int cantidad) {
        return hayDisponibilidad(producto, bodega, cantidad, null);
    }

    public boolean hayDisponibilidad(Producto producto, Bodega bodega, int cantidad, Connection conn) {
        ExistenciaInventario ex = consultarExistencia(producto, bodega, conn);
        if (ex != null) {
            return ex.getExistenciaDisponible() >= cantidad;
        }
        return false;
    }

    public void registrarEntrada(Producto producto, Bodega bodega, int cantidad, String referencia) throws Exception {
        if (producto == null || producto.getIdProducto() <= 0 || bodega == null
                || bodega.getId() == null || bodega.getId() <= 0 || !producto.isActivo() || !bodega.isActiva()
                || referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("Seleccione producto y bodega activos e indique una referencia.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);
            try {
                if (!existenciaDAO.existeProducto(conn, producto.getIdProducto())
                        || !existenciaDAO.existeBodegaActiva(conn, bodega.getId())) {
                    throw new IllegalStateException("El producto o la bodega ya no está activo. Actualice el catálogo.");
                }
                ExistenciaInventario ex = existenciaDAO.obtenerParaActualizar(conn, producto.getIdProducto(), bodega.getId());
                if (ex != null) {
                    existenciaDAO.actualizarExistenciaActual(conn, ex.getId(), ex.getExistenciaActual() + cantidad);
                } else {
                    existenciaDAO.insertarExistenciaInicial(conn, producto.getIdProducto(), bodega.getId(), cantidad);
                }
                movimientoDAO.registrarMovimiento(conn, producto.getIdProducto(), bodega.getId(),
                        TipoMovimientoInventario.ENTRADA, cantidad, referencia);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void registrarSalida(Producto producto, Bodega bodega, int cantidad, String referencia, Connection conn) throws Exception {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        if (producto == null || bodega == null) {
            throw new IllegalArgumentException("Producto y bodega son obligatorios.");
        }

        boolean localConn = false;
        if (conn == null) {
            conn = ConexionBD.obtenerConexion();
            conn.setAutoCommit(false);
            localConn = true;
        }

        try {
            if (!existenciaDAO.existeBodegaActiva(conn, bodega.getId())) {
                throw new IllegalStateException("Stock insuficiente o bodega inactiva para " + producto.getNombre());
            }
            ExistenciaInventario ex = existenciaDAO.obtenerParaActualizar(conn, producto.getIdProducto(), bodega.getId());
            if (ex == null || ex.getExistenciaDisponible() < cantidad) {
                throw new IllegalStateException("Stock insuficiente o bodega inactiva para " + producto.getNombre());
            }
            existenciaDAO.actualizarExistenciaActual(conn, ex.getId(), ex.getExistenciaActual() - cantidad);
            movimientoDAO.registrarMovimiento(conn, producto.getIdProducto(), bodega.getId(),
                    TipoMovimientoInventario.SALIDA, cantidad, referencia);

            if (localConn) conn.commit();
        } catch (Exception e) {
            if (localConn) conn.rollback();
            throw e;
        } finally {
            if (localConn) conn.close();
        }
    }

    public List<InventarioResumen> consultarSaldos() {
        return existenciaDAO.consultarSaldosActivos();
    }

    public List<InventarioResumen> listarExistencias(Integer idBodega, String filtroTexto) {
        return existenciaDAO.listarExistencias(idBodega, filtroTexto);
    }

    public int obtenerDisponibilidadTotal(long idProducto) {
        return existenciaDAO.obtenerDisponibilidadTotal(idProducto);
    }
}
