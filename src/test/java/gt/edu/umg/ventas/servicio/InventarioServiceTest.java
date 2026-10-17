package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.ExistenciaInventarioDAO;
import gt.edu.umg.ventas.dao.MovimientoInventarioDAO;
import gt.edu.umg.ventas.modelo.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Test
    public void testDelegacionPuraEnDAOs() throws Exception {
        AtomicInteger nuevaActualGuardada = new AtomicInteger(-1);
        AtomicInteger movimientosRegistrados = new AtomicInteger(0);

        ExistenciaInventario existenciaMock = new ExistenciaInventario();
        existenciaMock.setId(10);
        existenciaMock.setExistenciaActual(25);
        existenciaMock.setExistenciaReservada(5);

        ExistenciaInventarioDAO existenciaDAO = (ExistenciaInventarioDAO) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ExistenciaInventarioDAO.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "existeBodegaActiva", "existeProducto" -> true;
                    case "obtenerParaActualizar" -> existenciaMock;
                    case "actualizarExistenciaActual" -> {
                        nuevaActualGuardada.set((Integer) args[2]);
                        yield null;
                    }
                    case "consultarSaldosActivos" -> List.of(new InventarioResumen("P1", "Prod", "Bod", 25, 5, 20));
                    case "obtenerDisponibilidadTotal" -> 20;
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );

        MovimientoInventarioDAO movimientoDAO = (MovimientoInventarioDAO) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{MovimientoInventarioDAO.class},
                (proxy, method, args) -> {
                    if ("registrarMovimiento".equals(method.getName())) {
                        movimientosRegistrados.incrementAndGet();
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );

        Connection conMock = (Connection) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> null
        );

        InventarioService service = new InventarioService(existenciaDAO, movimientoDAO);
        Producto p = new Producto(); p.setIdProducto(1); p.setNombre("Laptop"); p.setActivo(true);
        Bodega b = new Bodega(); b.setId(1); b.setNombre("Central"); b.setActiva(true);

        assertTrue(service.hayDisponibilidad(p, b, 20, conMock));
        assertFalse(service.hayDisponibilidad(p, b, 21, conMock));
        assertEquals(1, service.consultarSaldos().size());
        assertEquals(20, service.obtenerDisponibilidadTotal(1L));

        service.registrarSalida(p, b, 5, "Despacho DSP-1", conMock);
        assertEquals(20, nuevaActualGuardada.get());
        assertEquals(1, movimientosRegistrados.get());
    }
}
