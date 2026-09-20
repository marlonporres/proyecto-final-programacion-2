package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.modelo.Categoria;
import gt.edu.umg.ventas.modelo.Cliente;
import gt.edu.umg.ventas.modelo.EstadoFactura;
import gt.edu.umg.ventas.modelo.Factura;
import gt.edu.umg.ventas.modelo.Producto;
import gt.edu.umg.ventas.modelo.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias de Capa de Servicio - FacturaService")
public class FacturaServiceTest {

    private FacturaService servicio;
    private Cliente cliente;
    private Usuario usuario;
    private Producto productoActivo;
    private Producto productoInactivo;

    @BeforeEach
    public void setUp() {
        servicio = new FacturaService(null, null);

        cliente = new Cliente(1L, "112233-4", "Cliente Prueba", "Guatemala", "22334455", "cliente@prueba.com");
        usuario = new Usuario(1L, "Marlon Porres", "admin", "ADMIN", true);

        Categoria cat = new Categoria(1L, "General", "General", true);

        productoActivo = new Producto(1L, "PROD-A", "Producto Activo", "Desc", new BigDecimal("100.00"), true, cat);
        productoInactivo = new Producto(2L, "PROD-I", "Producto Inactivo", "Desc", new BigDecimal("50.00"), false, cat);
    }

    @Test
    @DisplayName("No debe permitir agregar cantidades menores o iguales a cero")
    public void testAgregarCantidadInvalida() {
        Factura f = servicio.crear(cliente, usuario);
        assertThrows(IllegalArgumentException.class, () -> 
                servicio.agregarProducto(f.getIdFactura(), productoActivo, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> 
                servicio.agregarProducto(f.getIdFactura(), productoActivo, new BigDecimal("-1.00")));
    }

    @Test
    @DisplayName("No debe permitir agregar productos inactivos")
    public void testAgregarProductoInactivo() {
        Factura f = servicio.crear(cliente, usuario);
        assertThrows(IllegalStateException.class, () -> 
                servicio.agregarProducto(f.getIdFactura(), productoInactivo, new BigDecimal("1.00")));
    }

    @Test
    @DisplayName("No debe permitir emitir facturas sin detalles")
    public void testEmitirFacturaSinDetalles() {
        Factura f = servicio.crear(cliente, usuario);
        assertThrows(IllegalStateException.class, () -> servicio.emitir(f.getIdFactura()));
    }

    @Test
    @DisplayName("Debe cambiar estado a EMITIDA al emitir")
    public void testEmitirFacturaConExito() {
        Factura f = servicio.crear(cliente, usuario);
        servicio.agregarProducto(f.getIdFactura(), productoActivo, new BigDecimal("3.00"));

        assertEquals(EstadoFactura.BORRADOR, f.getEstado());

        Factura emitida = servicio.emitir(f.getIdFactura());
        assertEquals(EstadoFactura.EMITIDA, emitida.getEstado());
    }

    @Test
    @DisplayName("No debe permitir emitir facturas anuladas")
    public void testEmitirFacturaAnulada() {
        Factura f = servicio.crear(cliente, usuario);
        servicio.agregarProducto(f.getIdFactura(), productoActivo, new BigDecimal("1.00"));
        servicio.anular(f.getIdFactura());

        assertThrows(IllegalStateException.class, () -> servicio.emitir(f.getIdFactura()));
    }

    @Test
    @DisplayName("Debe cambiar estado a ANULADA al anular factura")
    public void testAnularFactura() {
        Factura f = servicio.crear(cliente, usuario);
        servicio.agregarProducto(f.getIdFactura(), productoActivo, new BigDecimal("4.00"));
        servicio.emitir(f.getIdFactura());
        assertEquals(EstadoFactura.EMITIDA, f.getEstado());

        servicio.anular(f.getIdFactura());
        assertEquals(EstadoFactura.ANULADA, f.getEstado());
    }
}
