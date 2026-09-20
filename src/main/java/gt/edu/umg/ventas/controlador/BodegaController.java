package gt.edu.umg.ventas.controlador;

import gt.edu.umg.ventas.dao.BodegaDAO;
import gt.edu.umg.ventas.dao.BodegaDAOImpl;
import gt.edu.umg.ventas.modelo.Bodega;
import java.util.List;

public class BodegaController {
    private BodegaDAO bodegaDAO = new BodegaDAOImpl();

    public List<Bodega> listar() {
        return bodegaDAO.obtenerTodos();
    }
    
    public void crear(String nombre, String ubicacion, boolean activa) {
        Bodega b = new Bodega();
        b.setNombre(nombre);
        b.setUbicacion(ubicacion);
        b.setActiva(activa);
        bodegaDAO.crear(b);
    }
}
