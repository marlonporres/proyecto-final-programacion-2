package gt.edu.umg.ventas.servicio;

import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import gt.edu.umg.ventas.modelo.*;
import static org.junit.jupiter.api.Assertions.*;

class OrdenVentaServiceTest {
    DatosPrueba datos = new DatosPrueba();
    int disponible = 20;
    OrdenVentaService servicio = new OrdenVentaService(datos.ordenDAO(), new InventarioService() {
        @Override public int obtenerDisponibilidadTotal(long id) { return disponible; }
    });
    @BeforeEach void ordenNueva() { datos.orden.setId(null); }
    @Test void rechazaOrdenYaGuardada() {
        datos.orden.setId(1);
        assertThrows(IllegalStateException.class, () -> servicio.crearOrdenVenta(datos.orden));
    }
    @Test void rechazaClienteYUsuarioSinPersistir() {
        datos.orden.getCliente().setIdCliente(0);
        assertThrows(IllegalArgumentException.class, () -> servicio.crearOrdenVenta(datos.orden));
        datos.orden.getCliente().setIdCliente(1);
        datos.orden.getUsuario().setIdUsuario(0);
        assertThrows(IllegalArgumentException.class, () -> servicio.crearOrdenVenta(datos.orden));
    }
    @Test void redondeaPrecioAntesDeCalcularSubtotalPersistido() {
        datos.orden.getDetalles().get(0).setPrecioUnitario(new BigDecimal("10.005"));
        servicio.crearOrdenVenta(datos.orden);
        assertEquals(new BigDecimal("10.01"), datos.orden.getDetalles().get(0).getPrecioUnitario());
        assertEquals(new BigDecimal("56.06"), datos.orden.getTotal());
    }
    @Test void ordenNoRebajaStockYNormalizaImportes() {
        datos.orden.getDetalles().get(0).setSubtotal(BigDecimal.ZERO);
        servicio.crearOrdenVenta(datos.orden);
        assertEquals(new BigDecimal("56.00"), datos.orden.getTotal());
        assertEquals(EstadoOrdenVenta.PENDIENTE, datos.orden.getEstado());
        assertEquals(20, disponible);
    }
    @Test void rechazaStockInsuficiente() {
        disponible = 4;
        assertThrows(IllegalStateException.class, () -> servicio.crearOrdenVenta(datos.orden));
    }
    @Test void rechazaProductoRepetido() {
        datos.orden.getDetalles().add(datos.orden.getDetalles().get(0));
        assertThrows(IllegalArgumentException.class, () -> servicio.crearOrdenVenta(datos.orden));
    }
    @Test void rechazaCantidadCeroEInactivo() {
        datos.orden.getDetalles().get(0).setCantidad(0);
        assertThrows(IllegalArgumentException.class, () -> servicio.crearOrdenVenta(datos.orden));
        datos.orden.getDetalles().get(0).setCantidad(5);
        datos.orden.getDetalles().get(0).getProducto().setActivo(false);
        assertThrows(IllegalArgumentException.class, () -> servicio.crearOrdenVenta(datos.orden));
    }
}
