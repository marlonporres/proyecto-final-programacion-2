package gt.edu.umg.ventas.integracion;

import gt.edu.umg.ventas.dao.*;
import gt.edu.umg.ventas.modelo.*;
import gt.edu.umg.ventas.servicio.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IntegracionRealDbTest {

    @BeforeAll
    public static void checkDbConnection() {
        try (Connection con = ConexionBD.obtenerConexion()) {
            Assumptions.assumeTrue(con != null && !con.isClosed(), "SQL Server no está disponible para test de integración.");
        } catch (Exception e) {
            Assumptions.abort("SQL Server no está disponible: " + e.getMessage());
        }
    }

    @Test
    public void testFlujoCompletoVentasInventarioDespacho() throws Exception {
        ClienteDAO clienteDAO = new ClienteDAOImpl();
        ProductoDAO productoDAO = new ProductoDAOImpl();
        BodegaDAO bodegaDAO = new BodegaDAOImpl();
        OrdenVentaDAO ordenVentaDAO = new OrdenVentaDAOImpl();
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
        InventarioService inventarioService = new InventarioService();
        OrdenVentaService ordenVentaService = new OrdenVentaService();
        DespachoService despachoService = new DespachoService();

        // 1. Verificar Semilla Clientes y Productos
        List<Cliente> clientes = clienteDAO.listar();
        assertNotNull(clientes);
        assertFalse(clientes.isEmpty(), "Debe haber clientes semilla en la BD");
        Cliente cliente = clientes.get(0);
        System.out.println("Cliente seleccionado: " + cliente.getNombre() + " (NIT: " + cliente.getNit() + ")");

        List<Producto> productos = productoDAO.listar();
        assertNotNull(productos);
        assertFalse(productos.isEmpty(), "Debe haber productos semilla en la BD");
        Producto producto = productos.get(0);
        System.out.println("Producto seleccionado: " + producto.getNombre() + " (Código: " + producto.getCodigo() + ")");

        List<Bodega> bodegas = bodegaDAO.obtenerTodos();
        assertNotNull(bodegas);
        assertFalse(bodegas.isEmpty(), "Debe haber bodegas semilla en la BD");
        Bodega bodega = bodegas.get(0);
        System.out.println("Bodega seleccionada: " + bodega.getNombre());

        Usuario usuario = usuarioDAO.buscarPorId(1L);
        if (usuario == null) {
            usuario = usuarioDAO.buscarPorNombreUsuario("admin");
        }
        assertNotNull(usuario, "Debe existir un usuario activo en la BD");

        // 2. Consultar Existencia Inicial
        List<InventarioResumen> resumenes = inventarioService.listarExistencias(null, null);
        assertNotNull(resumenes);
        System.out.println("Existencias registradas en BD: " + resumenes.size());
        for (InventarioResumen r : resumenes) {
            System.out.println(" - " + r.getCodigo() + " | " + r.getProducto() + " | " + r.getBodega() + " | Stock: " + r.getExistenciaActual() + " | Disp: " + r.getDisponible());
        }

        ExistenciaInventario existenciaInicial = inventarioService.consultarExistencia(producto, bodega);
        assertNotNull(existenciaInicial, "Debe existir registro de existencia para el producto");
        int stockInicial = existenciaInicial.getExistenciaActual();
        System.out.println("Stock inicial de " + producto.getNombre() + " en " + bodega.getNombre() + ": " + stockInicial);

        // 3. Crear Orden de Venta
        OrdenVenta orden = new OrdenVenta();
        orden.setNumeroOrden("OV-TEST-" + (System.currentTimeMillis() % 10000));
        orden.setFecha(LocalDateTime.now());
        orden.setCliente(cliente);
        orden.setUsuario(usuario);
        orden.setEstado(EstadoOrdenVenta.PENDIENTE);
        orden.setObservaciones("Orden de prueba de integración real");

        int cantidadVentaInt = 2;
        BigDecimal cantidadVenta = new BigDecimal(cantidadVentaInt);

        DetalleOrdenVenta detalle = new DetalleOrdenVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidadVentaInt);
        detalle.setPrecioUnitario(producto.getPrecioVenta());
        detalle.setSubtotal(producto.getPrecioVenta().multiply(cantidadVenta));
        orden.agregarDetalle(detalle);

        ordenVentaService.crearOrdenVenta(orden);
        assertTrue(orden.getId() > 0, "La orden debe tener un ID asignado tras guardarse");
        System.out.println("Orden de Venta creada con ID: " + orden.getId() + " (Número: " + orden.getNumeroOrden() + ") Estado: " + orden.getEstado());
        assertEquals(EstadoOrdenVenta.PENDIENTE, orden.getEstado());

        // 4. Consultar Órdenes Pendientes
        List<OrdenVenta> pendientes = ordenVentaService.obtenerPendientes();
        boolean encontrada = pendientes.stream().anyMatch(o -> o.getId().equals(orden.getId()));
        assertTrue(encontrada, "La orden creada debe figurar en la lista de pendientes");

        // 5. Consultar Órdenes por Fecha
        List<OrdenVentaResumen> ordenesRango = ordenVentaService.buscarPorFecha(
                LocalDate.now().minusDays(1).atStartOfDay(),
                LocalDate.now().plusDays(1).atTime(23, 59, 59)
        );
        boolean enRango = ordenesRango.stream().anyMatch(o -> o.getId().equals(orden.getId()));
        assertTrue(enRango, "La orden creada debe aparecer en el rango de fechas de hoy");

        // 6. Ejecutar Despacho
        Despacho despacho = new Despacho();
        despacho.setNumeroDespacho("DSP-TEST-" + (System.currentTimeMillis() % 10000));
        despacho.setOrden(orden);
        despacho.setBodega(bodega);
        despacho.setFechaDespacho(LocalDateTime.now());
        despacho.setEstado(EstadoDespacho.PENDIENTE);

        List<DetalleDespacho> detallesDespacho = new ArrayList<>();
        for (DetalleOrdenVenta d : orden.getDetalles()) {
            DetalleDespacho dd = new DetalleDespacho();
            dd.setDespacho(despacho);
            dd.setProducto(d.getProducto());
            dd.setCantidadSolicitada(d.getCantidad());
            dd.setCantidadDespachada(d.getCantidad());
            detallesDespacho.add(dd);
        }
        despacho.setDetalles(detallesDespacho);

        despachoService.confirmarDespacho(despacho);
        assertTrue(despacho.getId() > 0, "El despacho debe tener un ID asignado tras confirmarse");
        assertEquals(EstadoDespacho.CONFIRMADO, despacho.getEstado());
        System.out.println("Despacho confirmado con ID: " + despacho.getId() + " Estado: " + despacho.getEstado());

        // 7. Verificar que la Orden pasó a COMPLETADA en BD
        OrdenVenta ordenActualizada = ordenVentaDAO.obtener(orden.getId());
        assertNotNull(ordenActualizada);
        assertEquals(EstadoOrdenVenta.COMPLETADA, ordenActualizada.getEstado(), "La orden de venta debe quedar en estado COMPLETADA tras el despacho");
        System.out.println("Estado de orden tras despacho: " + ordenActualizada.getEstado());

        // 8. Verificar rebaja de inventario
        ExistenciaInventario existenciaFinal = inventarioService.consultarExistencia(producto, bodega);
        assertNotNull(existenciaFinal);
        int stockFinal = existenciaFinal.getExistenciaActual();
        System.out.println("Stock final de " + producto.getNombre() + " en " + bodega.getNombre() + ": " + stockFinal);
        assertEquals(stockInicial - cantidadVentaInt, stockFinal, "El stock debe haberse reducido exactamente en " + cantidadVentaInt);

        // 9. Verificar MovimientoInventario registrado en BD
        try (Connection con = ConexionBD.obtenerConexion();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TOP 1 tipo_movimiento, cantidad, referencia FROM dbo.MovimientoInventario WHERE id_producto = " + producto.getIdProducto() + " ORDER BY id DESC")) {
            assertTrue(rs.next(), "Debe haber un movimiento de inventario registrado");
            assertEquals("SALIDA", rs.getString("tipo_movimiento"));
            assertEquals(cantidadVentaInt, rs.getInt("cantidad"));
            System.out.println("Movimiento registrado: " + rs.getString("tipo_movimiento") + " | Cantidad: " + rs.getInt("cantidad") + " | Ref: " + rs.getString("referencia"));
        }
    }
}
