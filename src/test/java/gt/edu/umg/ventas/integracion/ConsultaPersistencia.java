package gt.edu.umg.ventas.integracion;

import gt.edu.umg.ventas.dao.OrdenVentaDAOImpl;
import gt.edu.umg.ventas.modelo.*;
import gt.edu.umg.ventas.servicio.*;

/** Proceso independiente y de solo lectura: demuestra recuperación sin caché de la JVM anterior. */
public final class ConsultaPersistencia {
    public static void main(String[] args) {
        OrdenVenta orden = new OrdenVentaDAOImpl().obtener(Integer.parseInt(args[0]));
        Despacho despacho = new DespachoService().obtenerPorOrden(orden.getId());
        Factura factura = new FacturaService().consultarPorNumero(args[1]);
        if (orden.getEstado() != EstadoOrdenVenta.COMPLETADA || despacho.getEstado() != EstadoDespacho.CONFIRMADO
                || factura.getEstado() != EstadoFactura.EMITIDA || !orden.getId().equals(factura.getOrden().getId())
                || factura.getDetalles().size() != 2 || orden.getTotal().compareTo(factura.calcularTotal()) != 0
                || despacho.getDetalles().stream().anyMatch(d -> d.getCantidadDespachada() != d.getCantidadSolicitada())) {
            throw new IllegalStateException("La operación recuperada no conserva sus relaciones y totales.");
        }
        System.out.println("PERSISTENCIA_OK " + orden.getNumeroOrden() + " " + despacho.getNumeroDespacho() + " " + factura.getNumero());
    }
}
