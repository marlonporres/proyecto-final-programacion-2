package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.OrdenVentaDAO;
import gt.edu.umg.ventas.dao.OrdenVentaDAOImpl;
import gt.edu.umg.ventas.modelo.OrdenVenta;
import gt.edu.umg.ventas.modelo.OrdenVentaResumen;
import java.time.LocalDateTime;
import java.util.List;

public class OrdenVentaService {
    private OrdenVentaDAO ordenVentaDAO;

    public OrdenVentaService() {
        this.ordenVentaDAO = new OrdenVentaDAOImpl();
    }

    public void crearOrdenVenta(OrdenVenta orden) {
        ordenVentaDAO.crear(orden);
    }

    public List<OrdenVentaResumen> buscarPorFecha(LocalDateTime desde, LocalDateTime hasta) {
        return ordenVentaDAO.buscarPorFecha(desde, hasta);
    }
}
