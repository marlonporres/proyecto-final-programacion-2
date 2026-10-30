package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.modelo.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class FacturaServiceTest {
    DatosPrueba datos;
    FacturaService servicio;
    @BeforeEach void preparar() { datos = new DatosPrueba(); servicio = datos.servicio(); }
    Factura borrador() { return servicio.crearDesdeOrden(1, datos.usuario); }
    Pago pago() { return new Pago(0, LocalDateTime.now(), new BigDecimal("56.00"), MetodoPago.EFECTIVO, "Prueba"); }

    @Test void rechazaOrdenPendiente() {
        datos.orden.setEstado(EstadoOrdenVenta.PENDIENTE);
        assertThrows(IllegalStateException.class, this::borrador);
    }
    @Test void rechazaOrdenSinDespachoConfirmado() {
        datos.confirmado = false;
        assertThrows(IllegalStateException.class, this::borrador);
    }
    @Test void copiaPrecioHistoricoYTotalDeOrden() {
        Factura f = borrador();
        assertEquals(new BigDecimal("10.00"), f.getDetalles().get(0).getPrecioUnitario());
        assertEquals(datos.orden.getTotal(), f.calcularTotal());
        assertEquals(5, f.getDetalles().get(0).getCantidad().intValueExact());
    }
    @Test void emiteYPersisteRelacion() {
        Factura f = servicio.emitir(borrador().getIdFactura());
        assertEquals(EstadoFactura.EMITIDA, f.getEstado());
        assertEquals(42, f.getIdFactura());
        assertEquals(1, f.getOrden().getId());
        assertSame(f, datos.guardada);
    }
    @Test void rechazaFacturaDuplicadaInclusoAnulada() {
        Factura f = servicio.emitir(borrador().getIdFactura());
        servicio.anular(f.getIdFactura());
        assertThrows(IllegalStateException.class, this::borrador);
    }
    @Test void errorPersistenciaConservaBorradorYPermiteReintento() {
        Factura f = borrador(); datos.falloGuardar = true;
        assertThrows(IllegalStateException.class, () -> servicio.emitir(f.getIdFactura()));
        assertEquals(EstadoFactura.BORRADOR, f.getEstado());
        datos.falloGuardar = false;
        assertEquals(EstadoFactura.EMITIDA, servicio.emitir(f.getIdFactura()).getEstado());
    }
    @Test void dosBorradoresNoPermitenDosFacturas() {
        Factura uno = borrador(), dos = borrador();
        servicio.emitir(uno.getIdFactura());
        assertThrows(IllegalStateException.class, () -> servicio.emitir(dos.getIdFactura()));
    }
    @Test void emisionRecargaCantidadesDelOrigen() {
        Factura f = borrador(); f.getDetalles().get(0).setCantidad(BigDecimal.ONE);
        assertEquals(new BigDecimal("56.00"), servicio.emitir(f.getIdFactura()).calcularTotal());
    }
    @Test void falloPagoNoCambiaEstadoNiPagosActivos() {
        Factura f = servicio.emitir(borrador().getIdFactura()); datos.falloPago = true;
        assertThrows(IllegalStateException.class, () -> servicio.registrarPago(f.getIdFactura(), pago()));
        assertEquals(EstadoFactura.EMITIDA, f.getEstado()); assertTrue(f.getPagos().isEmpty());
        datos.falloPago = false;
        assertEquals(EstadoFactura.PAGADA, servicio.registrarPago(f.getIdFactura(), pago()).getEstado());
    }
    @Test void falloAnulacionNoCambiaEstadoActivo() {
        Factura f = servicio.emitir(borrador().getIdFactura()); datos.falloAnular = true;
        assertThrows(IllegalStateException.class, () -> servicio.anular(f.getIdFactura()));
        assertEquals(EstadoFactura.EMITIDA, f.getEstado());
    }
    @Test void consultaTrasReiniciarServicioRecuperaOrden() {
        Factura f = servicio.emitir(borrador().getIdFactura());
        Factura recuperada = datos.servicio().consultarPorNumero(f.getNumero());
        assertEquals(f.getOrden().getId(), recuperada.getOrden().getId());
    }
    @Test void rechazaPagoEnBorrador() {
        Factura f = borrador();
        assertThrows(IllegalStateException.class, () -> servicio.registrarPago(f.getIdFactura(), pago()));
    }
}
