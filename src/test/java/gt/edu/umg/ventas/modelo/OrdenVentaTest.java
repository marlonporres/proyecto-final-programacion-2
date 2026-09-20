package gt.edu.umg.ventas.modelo;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class OrdenVentaTest {
    @Test
    public void testCalculosTotal() {
        OrdenVenta orden = new OrdenVenta();
        DetalleOrdenVenta d = new DetalleOrdenVenta();
        d.setSubtotal(new BigDecimal("100.50"));
        orden.agregarDetalle(d);
        
        assertEquals(new BigDecimal("100.50"), orden.getTotal());
        
        DetalleOrdenVenta d2 = new DetalleOrdenVenta();
        d2.setSubtotal(new BigDecimal("50.00"));
        orden.agregarDetalle(d2);
        
        assertEquals(new BigDecimal("150.50"), orden.getTotal());
    }
}
