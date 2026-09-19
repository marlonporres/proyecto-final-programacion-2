package gt.edu.umg.facturacion.servicio;

import gt.edu.umg.facturacion.modelo.Categoria;
import gt.edu.umg.facturacion.modelo.Cliente;
import gt.edu.umg.facturacion.modelo.EstadoFactura;
import gt.edu.umg.facturacion.modelo.Factura;
import gt.edu.umg.facturacion.modelo.Inventario;
import gt.edu.umg.facturacion.modelo.Producto;
import gt.edu.umg.facturacion.modelo.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        // Inicializamos el servicio sin dependencias obligatorias de BD para pruebas unitarias puras
        servicio = new FacturaService(null, null);

        cliente = new Cliente(1L, "112233-4", "Cliente Prueba", "Guatemala", "22334455", "cliente@prueba.com");
        usuario = new Usuario(1L, "Marlon Porres", "admin", "ADMIN", true);

        Categoria cat = new Categoria(1L, "General", "General", true);
        Inventario inv1 = new Inventario(1L, new BigDecimal("10.00"), new BigDecimal("2.00"), LocalDateTime.now());
        Inventario inv2 = new Inventario(2L, new BigDecimal("5.00"), new BigDecimal("1.00"), LocalDateTime.now());

        productoActivo = new Producto(1L, "PROD-A", "Producto Activo", "Desc", new BigDecimal("100.00"), true, cat, inv1);
        productoInactivo = new Producto(2L, "PROD-I", "Producto Inactivo", "Desc", new BigDecimal("50.00"), false, cat, inv2);
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
    @DisplayName("No debe permitir agregar más de la disponibilidad de inventario")
    public void testAgregarSinStock() {
        Factura f = servicio.crear(cliente, usuario);
        // Disponibilidad actual: 10.00. Solicitado: 10.01
        assertThrows(IllegalStateException.class, () -> 
                servicio.agregarProducto(f.getIdFactura(), productoActivo, new BigDecimal("10.01")));
    }

    @Test
    @DisplayName("No debe permitir emitir facturas sin detalles")
    public void testEmitirFacturaSinDetalles() {
        Factura f = servicio.crear(cliente, usuario);
        assertThrows(IllegalStateException.class, () -> servicio.emitir(f.getIdFactura()));
    }

    @Test
    @DisplayName("Debe descontar inventario y cambiar estado a EMITIDA al emitir")
    public void testEmitirFacturaConExito() {
        Factura f = servicio.crear(cliente, usuario);
        servicio.agregarProducto(f.getIdFactura(), productoActivo, new BigDecimal("3.00"));

        assertEquals(new BigDecimal("10.00"), productoActivo.getInventario().getExistencia());
        assertEquals(EstadoFactura.BORRADOR, f.getEstado());

        Factura emitida = servicio.emitir(f.getIdFactura());
        assertEquals(EstadoFactura.EMITIDA, emitida.getEstado());
        // Inventario descontado: 10.00 - 3.00 = 7.00
        assertEquals(new BigDecimal("7.00"), productoActivo.getInventario().getExistencia());
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
    @DisplayName("Debe reponer inventario al anular una factura previamente emitida")
    public void testAnularFacturaReponeInventario() {
        Factura f = servicio.crear(cliente, usuario);
        servicio.agregarProducto(f.getIdFactura(), productoActivo, new BigDecimal("4.00"));
        servicio.emitir(f.getIdFactura());
        assertEquals(new BigDecimal("6.00"), productoActivo.getInventario().getExistencia());

        servicio.anular(f.getIdFactura());
        assertEquals(EstadoFactura.ANULADA, f.getEstado());
        // Inventario repuesto: 6.00 + 4.00 = 10.00
        assertEquals(new BigDecimal("10.00"), productoActivo.getInventario().getExistencia());
    }
}
