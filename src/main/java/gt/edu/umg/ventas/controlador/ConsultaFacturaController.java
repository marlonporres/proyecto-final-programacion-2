package gt.edu.umg.ventas.controlador;

import gt.edu.umg.ventas.modelo.FacturaResumen;
import gt.edu.umg.ventas.servicio.ConsultaFacturaService;
import gt.edu.umg.ventas.servicio.ConsultaFacturaService.LineaFactura;

import java.time.LocalDate;
import java.util.List;

public class ConsultaFacturaController {
    private final ConsultaFacturaService servicio = new ConsultaFacturaService();

    /**
     * @param desde, hasta fechas inclusivas (null = sin límite)
     * @param estadoFactura, estadoOrden "TODOS" o null = sin filtro
     */
    public List<FacturaResumen> buscar(LocalDate desde, LocalDate hasta, String cliente,
                                       String numeroOrden, String estadoFactura, String estadoOrden) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'Desde' no puede ser mayor que 'Hasta'.");
        }
        return servicio.buscar(
                desde == null ? null : desde.atStartOfDay(),
                // 'Hasta' es inclusivo: se consulta hasta el inicio del día siguiente.
                hasta == null ? null : hasta.plusDays(1).atStartOfDay(),
                cliente, numeroOrden, normalizar(estadoFactura), normalizar(estadoOrden));
    }

    public List<LineaFactura> detalle(long idFactura) {
        return servicio.detalle(idFactura);
    }

    private static String normalizar(String estado) {
        return estado == null || estado.equalsIgnoreCase("TODOS") ? null : estado;
    }
}
