package gt.edu.umg.ventas.servicio;
import org.junit.jupiter.api.Test;
import gt.edu.umg.ventas.modelo.Despacho;
import static org.junit.jupiter.api.Assertions.*;

public class DespachoServiceTest {
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
