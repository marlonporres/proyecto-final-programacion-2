package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.OrdenVentaDAO;
import gt.edu.umg.ventas.dao.OrdenVentaDAOImpl;
import gt.edu.umg.ventas.modelo.OperacionLinea;
import java.util.List;
import java.util.Objects;

/** Consulta de solo lectura de la cadena completa de una orden. */
public class ConsultaOperacionService {
    private final OrdenVentaDAO ordenDAO;

    public ConsultaOperacionService() {
        this(new OrdenVentaDAOImpl());
    }

    public ConsultaOperacionService(OrdenVentaDAO ordenDAO) {
        this.ordenDAO = Objects.requireNonNull(ordenDAO);
    }

    public List<OperacionLinea> consultar(int idOrden) {
        return ordenDAO.consultarTrazabilidad(idOrden);
    }
}
