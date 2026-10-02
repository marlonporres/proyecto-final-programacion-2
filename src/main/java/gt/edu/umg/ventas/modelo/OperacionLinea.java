package gt.edu.umg.ventas.modelo;

import java.math.BigDecimal;

/** Proyección de solo lectura; las relaciones se recuperan mediante las claves guardadas. */
public record OperacionLinea(String numeroOrden, String codigoProducto, int cantidadSolicitada,
        Integer cantidadDespachada, BigDecimal precioUnitario, String bodega, String numeroDespacho,
        String tipoMovimiento, Integer cantidadSalida, String numeroFactura, BigDecimal totalFactura) { }
