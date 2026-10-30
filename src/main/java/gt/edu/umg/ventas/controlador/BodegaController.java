package gt.edu.umg.ventas.controlador;

import gt.edu.umg.ventas.dao.BodegaDAO;
import gt.edu.umg.ventas.dao.BodegaDAOImpl;
import gt.edu.umg.ventas.modelo.Bodega;
import java.util.List;
import java.util.Objects;

public class BodegaController {
    private final BodegaDAO bodegaDAO;

    public BodegaController() {
        this(new BodegaDAOImpl());
    }

    public BodegaController(BodegaDAO bodegaDAO) {
        this.bodegaDAO = Objects.requireNonNull(bodegaDAO, "El DAO de bodega es obligatorio.");
    }

    public List<Bodega> listar() {
        return bodegaDAO.obtenerTodos();
    }

    public Bodega obtenerPorId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID de bodega debe ser mayor a cero.");
        }
        return bodegaDAO.obtener(id);
    }

    public void crear(String nombre, String ubicacion, boolean activa) {
        Bodega b = construirYValidar(nombre, ubicacion, activa);
        bodegaDAO.crear(b);
    }

    public void actualizar(int id, String nombre, String ubicacion, boolean activa) {
        if (id <= 0) {
            throw new IllegalArgumentException("Seleccione una bodega válida para actualizar.");
        }
        Bodega b = construirYValidar(nombre, ubicacion, activa);
        b.setId(id);
        bodegaDAO.actualizar(b);
    }

    private Bodega construirYValidar(String nombre, String ubicacion, boolean activa) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la bodega es obligatorio.");
        }
        String nombreLimpio = nombre.trim();
        if (nombreLimpio.length() > 100) {
            throw new IllegalArgumentException("El nombre de la bodega no puede exceder 100 caracteres.");
        }
        String ubicacionLimpia = ubicacion != null ? ubicacion.trim() : null;
        if (ubicacionLimpia != null && ubicacionLimpia.length() > 200) {
            throw new IllegalArgumentException("La ubicación de la bodega no puede exceder 200 caracteres.");
        }
        Bodega b = new Bodega();
        b.setNombre(nombreLimpio);
        b.setUbicacion(ubicacionLimpia);
        b.setActiva(activa);
        return b;
    }
}
