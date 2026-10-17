package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.controlador.BodegaController;
import gt.edu.umg.ventas.controlador.ClienteController;
import gt.edu.umg.ventas.dao.BodegaDAO;
import gt.edu.umg.ventas.dao.ClienteDAO;
import gt.edu.umg.ventas.dao.OrdenVentaDAO;
import gt.edu.umg.ventas.modelo.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class DespachoServiceTest {
    @Test void noConfirmaSinGeneracionPrevia() {
        Despacho d = new Despacho(); OrdenVenta o = new OrdenVenta(); o.setId(1); d.setOrden(o);
        Bodega b = new Bodega(); b.setId(1); d.setBodega(b);
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

    @Test
    public void testConsultaOperacionDelegaEnOrdenVentaDAO() {
        OperacionLinea linea = new OperacionLinea("OV-1", "TEC-001", 2, 2,
                new BigDecimal("95.00"), "Bodega Central", "DSP-1", "SALIDA", 2, "FAC-1", new BigDecimal("212.80"));
        OrdenVentaDAO ordenDAO = (OrdenVentaDAO) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{OrdenVentaDAO.class},
                (proxy, method, args) -> {
                    if ("consultarTrazabilidad".equals(method.getName())) {
                        return List.of(linea);
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
        ConsultaOperacionService service = new ConsultaOperacionService(ordenDAO);
        List<OperacionLinea> resultado = service.consultar(1);
        assertEquals(1, resultado.size());
        assertEquals("OV-1", resultado.get(0).numeroOrden());
    }

    @Test
    public void testClienteControllerValidacionesYMultiplesTelefonos() {
        Cliente existente = new Cliente(5L, "123456-7", "Empresa S.A.", "Zona 1", "22223333", "info@empresa.com");
        AtomicReference<Cliente> actualizado = new AtomicReference<>();

        ClienteDAO dao = (ClienteDAO) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ClienteDAO.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "buscarPorNit" -> "123456-7".equals(args[0]) ? existente : null;
                    case "guardar", "actualizar" -> {
                        actualizado.set((Cliente) args[0]);
                        yield null;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );

        ClienteController controller = new ClienteController(dao);

        // Rechaza duplicado al crear cliente nuevo
        Cliente nuevoConMismoNit = new Cliente(0L, "123456-7", "Otro", "", "", "");
        assertThrows(IllegalArgumentException.class, () -> controller.guardar(nuevoConMismoNit));

        // Permite conservar su propio NIT al actualizar y depura múltiples teléfonos
        Cliente edicionMismoCliente = new Cliente(5L, "123456-7", "Empresa Editada S.A.", "Zona 10", "", "info@empresa.com");
        edicionMismoCliente.setTelefonos(Arrays.asList(" 5555-1111 ", "5555-2222", "5555-1111", "  "));
        controller.actualizar(edicionMismoCliente);
        assertEquals("Empresa Editada S.A.", actualizado.get().getNombre());
        assertEquals(List.of("5555-1111", "5555-2222"), actualizado.get().getTelefonos());
        assertEquals("5555-1111", actualizado.get().getTelefono());
        assertEquals("5555-1111, 5555-2222", actualizado.get().getTelefonosTexto());

        // Rechaza NIT de otro cliente al actualizar
        Cliente edicionOtroCliente = new Cliente(9L, "123456-7", "Intruso", "", "", "");
        assertThrows(IllegalArgumentException.class, () -> controller.actualizar(edicionOtroCliente));

        // Rechaza longitudes fuera de esquema (NIT > 20 y teléfono > 20)
        Cliente nitLargo = new Cliente(0L, "123456789012345678901", "Valido", "", "", "");
        assertThrows(IllegalArgumentException.class, () -> controller.guardar(nitLargo));

        Cliente telLargo = new Cliente(0L, "998877-6", "Valido", "", "", "");
        telLargo.agregarTelefono("123456789012345678901");
        assertThrows(IllegalArgumentException.class, () -> controller.guardar(telLargo));
    }

    @Test
    public void testBodegaControllerValidacionesYActualizacion() {
        AtomicReference<Bodega> guardada = new AtomicReference<>();
        BodegaDAO dao = (BodegaDAO) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{BodegaDAO.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "crear", "actualizar" -> {
                        guardada.set((Bodega) args[0]);
                        yield null;
                    }
                    case "obtener" -> {
                        Bodega b = new Bodega();
                        b.setId((Integer) args[0]);
                        b.setNombre("Bodega Norte");
                        b.setUbicacion("Zona 18");
                        b.setActiva(true);
                        yield b;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );

        BodegaController controller = new BodegaController(dao);
        assertThrows(IllegalArgumentException.class, () -> controller.crear("  ", "Ubicacion", true));
        assertThrows(IllegalArgumentException.class, () -> controller.crear("A".repeat(101), "Ubicacion", true));
        assertThrows(IllegalArgumentException.class, () -> controller.crear("Bodega", "U".repeat(201), true));

        controller.actualizar(3, "Bodega Sur", "Zona 12", false);
        assertEquals(3, guardada.get().getId());
        assertEquals("Bodega Sur", guardada.get().getNombre());
        assertFalse(guardada.get().isActiva());
        assertEquals("Bodega Norte", controller.obtenerPorId(3).getNombre());
    }
}
