package gt.edu.umg.ventas.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Fila de solo lectura para la consulta de facturas emitidas. */
public record FacturaResumen(
        long idFactura,
        String numeroFactura,
        LocalDateTime fecha,
        String nit,
        String nombreCliente,
        String numeroOrden,
        String estadoOrden,
        String estadoFactura,
        BigDecimal total,
        BigDecimal pagado,
        BigDecimal saldo) {
}
