package gt.edu.umg.ventas.controlador;

import gt.edu.umg.ventas.modelo.OrdenVentaResumen;
import gt.edu.umg.ventas.servicio.OrdenVentaService;
import java.time.LocalDateTime;
import java.util.List;

public class ConsultaOrdenVentaController {
    private OrdenVentaService ordenVentaService;

    public ConsultaOrdenVentaController() {
        this.ordenVentaService = new OrdenVentaService();
    }

    public List<OrdenVentaResumen> buscar(LocalDateTime desde, LocalDateTime hasta) throws Exception {
        if (desde == null || hasta == null) {
            throw new Exception("Debe seleccionar ambas fechas.");
        }
        if (desde.isAfter(hasta)) {
            throw new Exception("La fecha 'Desde' no puede ser mayor que 'Hasta'.");
        }
        return ordenVentaService.buscarPorFecha(desde, hasta);
    }
}
