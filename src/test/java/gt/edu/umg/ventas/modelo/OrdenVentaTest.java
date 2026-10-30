package gt.edu.umg.ventas.modelo;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class OrdenVentaTest {
    @Test
    public void calculaDetalleDesdeCantidadYPrecioSinSubtotalManual() {
        OrdenVenta orden = new OrdenVenta();
        DetalleOrdenVenta detalle = new DetalleOrdenVenta();
        detalle.setCantidad(5);
        detalle.setPrecioUnitario(new BigDecimal("95.00"));
        orden.agregarDetalle(detalle);
        assertEquals(new BigDecimal("475.00"), detalle.getSubtotal());
        assertEquals(new BigDecimal("532.00"), orden.getTotal());
        detalle.setCantidad(2);
        orden.calcularTotal();
        assertEquals(new BigDecimal("212.80"), orden.getTotal());
    }
    @Test
    public void testCalculosTotal() {
        OrdenVenta orden = new OrdenVenta();
        DetalleOrdenVenta d = new DetalleOrdenVenta();
        d.setSubtotal(new BigDecimal("100.50"));
        orden.agregarDetalle(d);
        
        assertEquals(new BigDecimal("112.56"), orden.getTotal());
        
        DetalleOrdenVenta d2 = new DetalleOrdenVenta();
        d2.setSubtotal(new BigDecimal("50.00"));
        orden.agregarDetalle(d2);
        
        assertEquals(new BigDecimal("168.56"), orden.getTotal());
    }
}
