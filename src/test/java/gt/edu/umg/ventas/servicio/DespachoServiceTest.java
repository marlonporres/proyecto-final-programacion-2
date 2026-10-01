package gt.edu.umg.ventas.servicio;
import org.junit.jupiter.api.Test;
import gt.edu.umg.ventas.modelo.Despacho;
import gt.edu.umg.ventas.modelo.*;
import static org.junit.jupiter.api.Assertions.*;

public class DespachoServiceTest {
    @Test void noConfirmaSinGeneracionPrevia() {
        Despacho d=new Despacho(); OrdenVenta o=new OrdenVenta(); o.setId(1); d.setOrden(o);
        Bodega b=new Bodega(); b.setId(1); d.setBodega(b);
        d.getDetalles().add(new DetalleDespacho());
        assertThrows(IllegalStateException.class, () -> new DespachoService().confirmarDespacho(d));
    }
    @Test
    public void testDespachoVacio() {
        DespachoService s = new DespachoService();
        Despacho d = new Despacho();
        
        Exception ex = assertThrows(Exception.class, () -> {
            s.confirmarDespacho(d);
        });
        assertEquals("El despacho debe tener al menos un detalle.", ex.getMessage());
    }
}
