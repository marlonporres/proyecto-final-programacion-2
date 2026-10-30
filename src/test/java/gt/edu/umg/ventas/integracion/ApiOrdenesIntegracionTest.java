package gt.edu.umg.ventas.integracion;

import gt.edu.umg.ventas.dao.*;
import gt.edu.umg.ventas.modelo.*;
import gt.edu.umg.ventas.servicio.DespachoService;
import gt.edu.umg.ventas.servicio.InventarioService;
import gt.edu.umg.ventas.servicio.OrdenVentaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas SQL optativas: sólo crean y eliminan registros propios, nunca recrean la base. */
@EnabledIfSystemProperty(named = "ventas.api.it", matches = "true")
class ApiOrdenesIntegracionTest {
    private final OrdenVentaDAOImpl ordenDAO = new OrdenVentaDAOImpl();
    private final OrdenVentaService ordenService = new OrdenVentaService();
    private final InventarioService inventario = new InventarioService();
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Producto> productos = new ArrayList<>();
    private final List<Integer> ordenesPropias = new ArrayList<>();
    private Categoria categoria;
    private Bodega bodega;
    private Usuario usuario;

    @BeforeAll
    static void verificarEsquema() throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT OBJECT_ID('dbo.OrdenVenta') AS orden, "
                     + "OBJECT_ID('dbo.Despacho') AS despacho, COL_LENGTH('dbo.factura','id_orden') AS factura")) {
            assertTrue(rs.next());
            assertNotNull(rs.getObject("orden"), "Se necesita el esquema actual de ventas.");
            assertNotNull(rs.getObject("despacho"));
            assertNotNull(rs.getObject("factura"));
        }
    }

    @BeforeEach
    void prepararDatosPropios() throws Exception {
        String token = UUID.randomUUID().toString();
        for (int i = 0; i < 2; i++) {
            Cliente cliente = new Cliente(0L, "API" + token.substring(0, 14) + i,
                    "Cliente API IT " + i, "", "", "");
            clientes.add(cliente);
            new ClienteDAOImpl().guardar(cliente);
        }
        categoria = new Categoria(0L, "API-IT-" + token, "Pruebas propias de órdenes API", true);
        new CategoriaDAOImpl().guardar(categoria);
        bodega = new Bodega();
        bodega.setNombre("API-IT-" + token);
        bodega.setActiva(true);
        new BodegaDAOImpl().crear(bodega);
        usuario = new UsuarioDAOImpl().buscarPorNombreUsuario("admin");
        assertNotNull(usuario, "La demostración requiere el usuario académico admin.");
        assertTrue(usuario.isActivo());
        for (int i = 0; i < 3; i++) {
            Producto producto = new Producto(0L, "API-IT-" + token + "-" + i,
                    "Producto API IT " + i, "", BigDecimal.valueOf((i + 1) * 10L).setScale(2), true, categoria);
            productos.add(producto);
            new ProductoDAOImpl().guardar(producto);
            inventario.registrarEntrada(producto, bodega, 20, "Entrada propia API IT");
        }
    }

    @Test
    void editarConservaPrecioHistoricoIdentidadYNoMueveInventario() throws Exception {
        OrdenVenta original = crearOrden(2);
        OrdenVenta antes = ordenDAO.obtener(original.getId());
        long movimientosAntes = contarMovimientos();

        productos.get(0).setPrecioVenta(new BigDecimal("99.00"));
        new ProductoDAOImpl().actualizar(productos.get(0));
        OrdenVenta propuesta = proponer(original.getId(), clientes.get(1), "Edición desde API",
                linea(productos.get(0), 3), linea(productos.get(1), 2));
        propuesta.setNumeroOrden("ESTE-NUMERO-NO-DEBE-PERSISTIR");
        propuesta.setFecha(LocalDateTime.of(2000, 1, 1, 0, 0));
        propuesta.setEstado(EstadoOrdenVenta.COMPLETADA);
        ordenService.actualizarOrdenVenta(propuesta);

        OrdenVenta despues = ordenDAO.obtener(original.getId());
        assertEquals(antes.getNumeroOrden(), despues.getNumeroOrden());
        assertEquals(antes.getFecha(), despues.getFecha());
        assertEquals(antes.getUsuario().getIdUsuario(), despues.getUsuario().getIdUsuario());
        assertEquals(clientes.get(1).getIdCliente(), despues.getCliente().getIdCliente());
        assertEquals("Edición desde API", despues.getObservaciones());
        assertEquals(EstadoOrdenVenta.PENDIENTE, despues.getEstado());
        Map<Long, DetalleOrdenVenta> detalles = porProducto(despues);
        assertEquals(2, detalles.size());
        assertEquals(3, detalles.get(productos.get(0).getIdProducto()).getCantidad());
        assertEquals(new BigDecimal("10.00"), detalles.get(productos.get(0).getIdProducto()).getPrecioUnitario());
        assertEquals(new BigDecimal("20.00"), detalles.get(productos.get(1).getIdProducto()).getPrecioUnitario());
        assertEquals(new BigDecimal("78.40"), despues.getTotal());
        assertEquals(movimientosAntes, contarMovimientos());
        comprobarStockSinReservas(20);
    }

    @Test
    void cancelarEsIdempotenteYConservaCabeceraDetallesYStock() throws Exception {
        OrdenVenta orden = crearOrden(2);
        OrdenVenta antes = ordenDAO.obtener(orden.getId());
        long movimientosAntes = contarMovimientos();
        ordenService.cancelarOrdenVenta(orden.getId());
        ordenService.cancelarOrdenVenta(orden.getId());

        OrdenVenta cancelada = ordenDAO.obtener(orden.getId());
        assertNotNull(cancelada);
        assertEquals(EstadoOrdenVenta.CANCELADA, cancelada.getEstado());
        assertEquals(antes.getNumeroOrden(), cancelada.getNumeroOrden());
        assertEquals(antes.getTotal(), cancelada.getTotal());
        assertEquals(1, cancelada.getDetalles().size());
        assertEquals(2, cancelada.getDetalles().getFirst().getCantidad());
        assertThrows(IllegalStateException.class, () -> ordenService.actualizarOrdenVenta(
                proponer(orden.getId(), clientes.get(0), "No permitido", linea(productos.get(0), 3))));
        assertEquals(movimientosAntes, contarMovimientos());
        comprobarStockSinReservas(20);
    }

    @Test
    void despachoPendienteImpideEditarYCancelarAunqueAunNoDescuenteStock() throws Exception {
        OrdenVenta orden = crearOrden(2);
        new DespachoService().generarDespacho(prepararDespacho(orden));
        assertTrue(ordenDAO.tieneDocumentosRelacionados(orden.getId()));
        assertThrows(IllegalStateException.class, () -> ordenService.actualizarOrdenVenta(
                proponer(orden.getId(), clientes.get(1), "No permitido", linea(productos.get(0), 3))));
        assertThrows(IllegalStateException.class, () -> ordenService.cancelarOrdenVenta(orden.getId()));
        comprobarOrdenOriginal(orden.getId());
        comprobarStockSinReservas(20);
    }

    @Test
    void existenciaDeFacturaPorSiSolaImpideModificarUnaOrden() throws Exception {
        OrdenVenta orden = crearOrden(2);
        // Encabezado histórico aislado: comprueba el guard de existencia, sin depender del de despacho.
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement("INSERT dbo.factura "
                     + "(numero,fecha_hora,estado,observaciones,cliente_id,usuario_id,id_orden,total) "
                     + "VALUES (?,SYSDATETIME(),'ANULADA','Fixture propia API IT',?,?,?,?)")) {
            ps.setString(1, "FAC-" + UUID.randomUUID());
            ps.setLong(2, clientes.get(0).getIdCliente());
            ps.setLong(3, usuario.getIdUsuario());
            ps.setInt(4, orden.getId());
            ps.setBigDecimal(5, orden.getTotal());
            assertEquals(1, ps.executeUpdate());
        }
        assertTrue(ordenDAO.tieneDocumentosRelacionados(orden.getId()));
        assertThrows(IllegalStateException.class, () -> ordenService.actualizarOrdenVenta(
                proponer(orden.getId(), clientes.get(1), "No permitido", linea(productos.get(0), 3))));
        assertThrows(IllegalStateException.class, () -> ordenService.cancelarOrdenVenta(orden.getId()));
        comprobarOrdenOriginal(orden.getId());
        comprobarStockSinReservas(20);
    }

    @Test
    void ordenCompletadaNoSeEditaNiSeCancela() throws Exception {
        OrdenVenta orden = crearOrden(2);
        DespachoService despachos = new DespachoService();
        Despacho despacho = prepararDespacho(orden);
        despachos.generarDespacho(despacho);
        despachos.confirmarDespacho(despacho);
        long movimientosAntes = contarMovimientos();

        assertThrows(IllegalStateException.class, () -> ordenService.actualizarOrdenVenta(
                proponer(orden.getId(), clientes.get(1), "No permitido", linea(productos.get(0), 3))));
        assertThrows(IllegalStateException.class, () -> ordenService.cancelarOrdenVenta(orden.getId()));
        assertEquals(EstadoOrdenVenta.COMPLETADA, ordenDAO.obtener(orden.getId()).getEstado());
        assertEquals(18, stock(productos.get(0)));
        assertEquals(movimientosAntes, contarMovimientos());
    }

    @Test
    void productoDesactivadoDespuesDeSeleccionarloRevierteLaEdicion() throws Exception {
        OrdenVenta orden = crearOrden(2);
        Producto desactivado = new ProductoDAOImpl().buscarPorId(productos.get(1).getIdProducto());
        desactivado.setActivo(false);
        new ProductoDAOImpl().actualizar(desactivado);
        assertTrue(productos.get(1).isActivo(), "El objeto seleccionado se conserva activo en memoria.");
        long movimientosAntes = contarMovimientos();

        assertThrows(IllegalStateException.class, () -> ordenService.actualizarOrdenVenta(
                proponer(orden.getId(), clientes.get(1), "Debe revertirse",
                        linea(productos.get(0), 3), linea(productos.get(1), 1))));
        comprobarOrdenOriginal(orden.getId());
        assertEquals(movimientosAntes, contarMovimientos());
        comprobarStockSinReservas(20);
    }

    @Test
    void errorDespuesDeEscribirRevierteCabeceraYReemplazoDeDetalles() throws Exception {
        OrdenVenta orden = crearOrden(2);
        AtomicBoolean escrituraObservada = new AtomicBoolean();
        OrdenVentaDAO conFallo = (OrdenVentaDAO) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{OrdenVentaDAO.class}, (proxy, method, args) -> {
                    Object resultado;
                    try { resultado = method.invoke(ordenDAO, args); }
                    catch (InvocationTargetException e) { throw e.getCause(); }
                    if ("actualizarCompleta".equals(method.getName())) {
                        Connection con = (Connection) args[0];
                        try (PreparedStatement ps = con.prepareStatement(
                                "SELECT id_cliente FROM dbo.OrdenVenta WHERE id=?")) {
                            ps.setInt(1, orden.getId());
                            try (ResultSet rs = ps.executeQuery()) {
                                assertTrue(rs.next());
                                assertEquals(clientes.get(1).getIdCliente(), rs.getLong(1));
                            }
                        }
                        escrituraObservada.set(true);
                        throw new IllegalStateException("Fallo controlado después de escribir la edición");
                    }
                    return resultado;
                });

        assertThrows(IllegalStateException.class, () -> new OrdenVentaService(conFallo).actualizarOrdenVenta(
                proponer(orden.getId(), clientes.get(1), "Debe revertirse",
                        linea(productos.get(0), 3), linea(productos.get(1), 2))));
        assertTrue(escrituraObservada.get());
        comprobarOrdenOriginal(orden.getId());
        comprobarStockSinReservas(20);
    }

    @Test
    void cancelacionYGeneracionConcurrentesConservanUnaSecuenciaValida() throws Exception {
        comprobarCarreraConDespacho(true);
    }

    @Test
    void edicionYGeneracionConcurrentesConservanUnaSecuenciaValida() throws Exception {
        comprobarCarreraConDespacho(false);
    }

    private void comprobarCarreraConDespacho(boolean cancelar) throws Exception {
        OrdenVenta orden = crearOrden(2);
        Despacho despacho = prepararDespacho(orden);
        OrdenVenta propuesta = proponer(orden.getId(), clientes.get(1), "Edición concurrente", linea(productos.get(0), 3));
        CountDownLatch preparados = new CountDownLatch(2);
        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService ejecutor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> cambio = ejecutor.submit(() -> intentar(preparados, salida, () -> {
                if (cancelar) ordenService.cancelarOrdenVenta(orden.getId());
                else ordenService.actualizarOrdenVenta(propuesta);
            }));
            Future<Boolean> generacion = ejecutor.submit(() -> intentar(preparados, salida,
                    () -> new DespachoService().generarDespacho(despacho)));
            assertTrue(preparados.await(10, TimeUnit.SECONDS));
            salida.countDown();
            boolean cambioExitoso = cambio.get(30, TimeUnit.SECONDS);
            boolean despachoExitoso = generacion.get(30, TimeUnit.SECONDS);
            assertNotEquals(cambioExitoso, despachoExitoso,
                    "Sólo una operación puede aceptar el documento y sus cantidades originales.");
            OrdenVenta persistida = ordenDAO.obtener(orden.getId());
            if (cambioExitoso) {
                assertFalse(ordenDAO.tieneDocumentosRelacionados(orden.getId()));
                assertEquals(cancelar ? EstadoOrdenVenta.CANCELADA : EstadoOrdenVenta.PENDIENTE, persistida.getEstado());
                assertEquals(cancelar ? 2 : 3, persistida.getDetalles().getFirst().getCantidad());
            } else {
                assertTrue(ordenDAO.tieneDocumentosRelacionados(orden.getId()));
                assertEquals(EstadoOrdenVenta.PENDIENTE, persistida.getEstado());
                assertEquals(2, persistida.getDetalles().getFirst().getCantidad());
            }
            comprobarStockSinReservas(20);
        } finally {
            salida.countDown();
            ejecutor.shutdownNow();
            assertTrue(ejecutor.awaitTermination(30, TimeUnit.SECONDS), "La carrera debe terminar antes de limpiar sus datos.");
        }
    }

    private boolean intentar(CountDownLatch preparados, CountDownLatch salida, Accion accion) throws Exception {
        preparados.countDown();
        assertTrue(salida.await(10, TimeUnit.SECONDS));
        try { accion.ejecutar(); return true; }
        catch (IllegalStateException | IllegalArgumentException esperadoPorLaCarrera) { return false; }
    }

    @FunctionalInterface
    private interface Accion { void ejecutar() throws Exception; }

    private OrdenVenta crearOrden(int cantidad) {
        OrdenVenta orden = new OrdenVenta();
        orden.setNumeroOrden("OV-" + UUID.randomUUID());
        orden.setFecha(LocalDateTime.now().withNano(0));
        orden.setCliente(clientes.get(0));
        orden.setUsuario(usuario);
        orden.setObservaciones("Orden original API IT");
        orden.agregarDetalle(linea(productos.get(0), cantidad));
        ordenService.crearOrdenVenta(orden);
        ordenesPropias.add(orden.getId());
        return orden;
    }

    private OrdenVenta proponer(int id, Cliente cliente, String observaciones, DetalleOrdenVenta... detalles) {
        OrdenVenta propuesta = new OrdenVenta();
        propuesta.setId(id);
        propuesta.setCliente(cliente);
        propuesta.setObservaciones(observaciones);
        for (DetalleOrdenVenta detalle : detalles) propuesta.agregarDetalle(detalle);
        return propuesta;
    }

    private DetalleOrdenVenta linea(Producto producto, int cantidad) {
        DetalleOrdenVenta linea = new DetalleOrdenVenta();
        linea.setProducto(producto);
        linea.setCantidad(cantidad);
        linea.setPrecioUnitario(producto.getPrecioVenta());
        return linea;
    }

    private Despacho prepararDespacho(OrdenVenta orden) {
        Despacho despacho = new Despacho();
        despacho.setOrden(orden);
        despacho.setBodega(bodega);
        for (DetalleOrdenVenta linea : orden.getDetalles()) {
            DetalleDespacho detalle = new DetalleDespacho();
            detalle.setProducto(linea.getProducto());
            detalle.setCantidadSolicitada(linea.getCantidad());
            detalle.setCantidadDespachada(0);
            despacho.getDetalles().add(detalle);
        }
        return despacho;
    }

    private Map<Long, DetalleOrdenVenta> porProducto(OrdenVenta orden) {
        return orden.getDetalles().stream().collect(Collectors.toMap(d -> d.getProducto().getIdProducto(), d -> d));
    }

    private void comprobarOrdenOriginal(int id) {
        OrdenVenta persistida = ordenDAO.obtener(id);
        assertEquals(EstadoOrdenVenta.PENDIENTE, persistida.getEstado());
        assertEquals(clientes.get(0).getIdCliente(), persistida.getCliente().getIdCliente());
        assertEquals("Orden original API IT", persistida.getObservaciones());
        assertEquals(new BigDecimal("22.40"), persistida.getTotal());
        assertEquals(1, persistida.getDetalles().size());
        assertEquals(2, persistida.getDetalles().getFirst().getCantidad());
    }

    private long contarMovimientos() throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM dbo.MovimientoInventario WHERE id_bodega=?")) {
            ps.setInt(1, bodega.getId());
            try (ResultSet rs = ps.executeQuery()) { assertTrue(rs.next()); return rs.getLong(1); }
        }
    }

    private int stock(Producto producto) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement("SELECT existencia_actual FROM dbo.ExistenciaInventario WHERE id_bodega=? AND id_producto=?")) {
            ps.setInt(1, bodega.getId());
            ps.setLong(2, producto.getIdProducto());
            try (ResultSet rs = ps.executeQuery()) { assertTrue(rs.next()); return rs.getInt(1); }
        }
    }

    private void comprobarStockSinReservas(int esperado) throws SQLException {
        for (Producto producto : productos) assertEquals(esperado, stock(producto));
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement("SELECT SUM(existencia_reservada) FROM dbo.ExistenciaInventario WHERE id_bodega=?")) {
            ps.setInt(1, bodega.getId());
            try (ResultSet rs = ps.executeQuery()) { assertTrue(rs.next()); assertEquals(0, rs.getInt(1)); }
        }
    }

    @AfterEach
    void limpiarExclusivamenteDatosPropios() throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                for (int id : ordenesPropias) {
                    ejecutar(con, "DELETE dbo.pago WHERE factura_id IN (SELECT id_factura FROM dbo.factura WHERE id_orden=?)", id);
                    ejecutar(con, "DELETE dbo.detalle_factura WHERE factura_id IN (SELECT id_factura FROM dbo.factura WHERE id_orden=?)", id);
                    ejecutar(con, "DELETE dbo.factura WHERE id_orden=?", id);
                    ejecutar(con, "DELETE dbo.DetalleDespacho WHERE id_despacho IN (SELECT id FROM dbo.Despacho WHERE id_orden=?)", id);
                    ejecutar(con, "DELETE dbo.Despacho WHERE id_orden=?", id);
                    ejecutar(con, "DELETE dbo.DetalleOrdenVenta WHERE id_orden=?", id);
                    ejecutar(con, "DELETE dbo.OrdenVenta WHERE id=?", id);
                }
                if (bodega != null && bodega.getId() != null) {
                    ejecutar(con, "DELETE dbo.MovimientoInventario WHERE id_bodega=?", bodega.getId());
                    ejecutar(con, "DELETE dbo.ExistenciaInventario WHERE id_bodega=?", bodega.getId());
                    ejecutar(con, "DELETE dbo.Bodega WHERE id=?", bodega.getId());
                }
                for (Producto producto : productos) if (producto.getIdProducto() > 0)
                    ejecutar(con, "DELETE dbo.producto WHERE id_producto=?", producto.getIdProducto());
                if (categoria != null && categoria.getIdCategoria() > 0)
                    ejecutar(con, "DELETE dbo.categoria WHERE id_categoria=?", categoria.getIdCategoria());
                for (Cliente cliente : clientes) if (cliente.getIdCliente() > 0)
                    ejecutar(con, "DELETE dbo.cliente WHERE id_cliente=?", cliente.getIdCliente());
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private void ejecutar(Connection con, String sql, long id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) { ps.setLong(1, id); ps.executeUpdate(); }
    }
}
