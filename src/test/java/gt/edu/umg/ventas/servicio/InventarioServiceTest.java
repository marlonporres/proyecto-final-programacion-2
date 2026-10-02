package gt.edu.umg.ventas.servicio;
import org.junit.jupiter.api.Test;
import gt.edu.umg.ventas.modelo.Producto;
import gt.edu.umg.ventas.modelo.Bodega;
import static org.junit.jupiter.api.Assertions.*;

public class InventarioServiceTest {
    @Test
    public void testRegistroSalidaInvalida() {
        InventarioService s = new InventarioService();
        Producto p = new Producto(); p.setIdProducto(1); p.setNombre("Test");
        Bodega b = new Bodega(); b.setId(1);
        
        Exception ex = assertThrows(Exception.class, () -> {
            s.registrarSalida(p, b, -5, "Ref", null);
        });
        assertEquals("La cantidad debe ser mayor a cero.", ex.getMessage());
    }
}
