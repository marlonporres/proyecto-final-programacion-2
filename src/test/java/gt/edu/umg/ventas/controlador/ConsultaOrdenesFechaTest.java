package gt.edu.umg.ventas.controlador;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class ConsultaOrdenesFechaTest {
    @Test
    public void testFechasInvalidas() {
        ConsultaOrdenVentaController c = new ConsultaOrdenVentaController();
        LocalDateTime desde = LocalDateTime.now();
        LocalDateTime hasta = desde.minusDays(1);
        
        Exception ex = assertThrows(Exception.class, () -> {
            c.buscar(desde, hasta);
        });
        assertEquals("La fecha 'Desde' no puede ser mayor que 'Hasta'.", ex.getMessage());
    }
}
