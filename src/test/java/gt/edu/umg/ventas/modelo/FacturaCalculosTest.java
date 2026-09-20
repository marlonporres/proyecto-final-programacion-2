package gt.edu.umg.ventas.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias de Cálculos de Factura")
public class FacturaCalculosTest {

    private Factura factura;
    private Producto producto1;
    private Producto producto2;

    @BeforeEach
    public void setUp() {
        Usuario usuario = new Usuario(1L, "Marlon Porres", "admin", "ADMIN", true);
        Cliente cliente = new Cliente(1L, "1234567-8", "Cliente de Prueba", "Ciudad", "12345678", "test@test.com");
        factura = new Factura(1L, "FAC-001", LocalDateTime.now(), EstadoFactura.BORRADOR, "Nota de prueba", cliente, usuario);

        Categoria categoria = new Categoria(1L, "Ferretería", "Herramientas", true);
        Inventario inv1 = new Inventario(1L, new BigDecimal("100.00"), new BigDecimal("10.00"), LocalDateTime.now());
        Inventario inv2 = new Inventario(2L, new BigDecimal("50.00"), new BigDecimal("5.00"), LocalDateTime.now());

        producto1 = new Producto(1L, "P1", "Martillo", "16oz", new BigDecimal("50.00"), true, categoria, inv1);
        producto2 = new Producto(2L, "P2", "Taladro", "650W", new BigDecimal("200.00"), true, categoria, inv2);
    }

    @Test
    @DisplayName("Debe calcular subtotal correctamente con BigDecimal")
    public void testCalcularSubtotal() {
        // 2 Martillos a Q 50.00 = Q 100.00
        factura.agregarDetalle(producto1, new BigDecimal("2.00"));
        // 1 Taladro a Q 200.00 = Q 200.00
        factura.agregarDetalle(producto2, new BigDecimal("1.00"));

        BigDecimal subtotal = factura.calcularSubtotal();
        assertEquals(new BigDecimal("300.00"), subtotal);
    }

    @Test
    @DisplayName("Debe calcular impuesto IVA (12%) correctamente")
    public void testCalcularImpuesto() {
        // 2 Martillos a Q 50.00 = Q 100.00. IVA (12%) = Q 12.00
        factura.agregarDetalle(producto1, new BigDecimal("2.00"));

        BigDecimal impuesto = factura.calcularImpuesto();
        assertEquals(new BigDecimal("12.00"), impuesto);
    }

    @Test
    @DisplayName("Debe calcular el total sumando subtotal e impuesto")
    public void testCalcularTotal() {
        // Subtotal = 100.00, Impuesto = 12.00 -> Total = 112.00
        factura.agregarDetalle(producto1, new BigDecimal("2.00"));

        BigDecimal total = factura.calcularTotal();
        assertEquals(new BigDecimal("112.00"), total);
    }

    @Test
    @DisplayName("Debe cambiar estado a PAGADA cuando los pagos cubren el total")
    public void testRegistrarPagoYCambioEstado() {
        factura.agregarDetalle(producto1, new BigDecimal("2.00")); // Total = 112.00
        factura.setEstado(EstadoFactura.EMITIDA);

        // Pago parcial de Q 50.00
        Pago pago1 = new Pago(1L, LocalDateTime.now(), new BigDecimal("50.00"), MetodoPago.EFECTIVO, "Efectivo");
        factura.registrarPago(pago1);
        assertEquals(EstadoFactura.EMITIDA, factura.getEstado());
        assertEquals(new BigDecimal("50.00"), factura.obtenerTotalPagado());

        // Pago restante de Q 62.00 (Total acumulado: 112.00)
        Pago pago2 = new Pago(2L, LocalDateTime.now(), new BigDecimal("62.00"), MetodoPago.TARJETA, "VOUCHER-123");
        factura.registrarPago(pago2);
        assertEquals(EstadoFactura.PAGADA, factura.getEstado());
        assertEquals(new BigDecimal("112.00"), factura.obtenerTotalPagado());
    }

    @Test
    @DisplayName("No debe permitir registrar pagos si la factura está ANULADA")
    public void testRegistrarPagoFacturaAnulada() {
        factura.setEstado(EstadoFactura.ANULADA);
        Pago pago = new Pago(1L, LocalDateTime.now(), new BigDecimal("100.00"), MetodoPago.EFECTIVO, "Efectivo");
        assertThrows(IllegalStateException.class, () -> factura.registrarPago(pago));
    }
}
