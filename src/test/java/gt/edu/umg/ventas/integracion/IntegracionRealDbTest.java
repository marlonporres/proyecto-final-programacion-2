package gt.edu.umg.ventas.integracion;

import gt.edu.umg.ventas.dao.*;
import gt.edu.umg.ventas.modelo.*;
import gt.edu.umg.ventas.servicio.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Datos propios por caso; nunca consume semillas ni borra registros ajenos. */
@EnabledIfSystemProperty(named = "ventas.it", matches = "true")
class IntegracionRealDbTest {
    Cliente cliente;
    Categoria categoria;
    Bodega bodega;
    Usuario usuario;
    List<Producto> productos = new ArrayList<>();
    InventarioService inventario = new InventarioService();

    @BeforeAll static void verificarBase() throws Exception {
        try (Connection con = ConexionBD.obtenerConexion();
             Statement ps = con.createStatement();
             ResultSet rs = ps.executeQuery("SELECT COL_LENGTH('dbo.factura', 'id_orden') AS orden, OBJECT_ID('dbo.Despacho') AS despacho")) {
            assertTrue(rs.next());
            assertNotNull(rs.getObject("orden"), "Instale el esquema actualizado antes de ejecutar integración.");
            assertNotNull(rs.getObject("despacho"));
        }
    }

    @BeforeEach void preparar() throws Exception {
        String token = UUID.randomUUID().toString();
        cliente = new Cliente(0, token.substring(0, 18), "Cliente IT", "", "", "");
        new ClienteDAOImpl().guardar(cliente);
        categoria = new Categoria(0, "IT-" + token, "Prueba aislada", true);
        new CategoriaDAOImpl().guardar(categoria);
        bodega = new Bodega(); bodega.setNombre("IT-" + token); bodega.setActiva(true);
        new BodegaDAOImpl().crear(bodega);
        usuario = new UsuarioDAOImpl().buscarPorNombreUsuario("admin");
        for (int i = 0; i < 2; i++) {
            Producto p = new Producto(0, "IT-" + token + "-" + i, "Producto IT " + i, "", new BigDecimal("10.00"), true, categoria);
            new ProductoDAOImpl().guardar(p); productos.add(p);
            inventario.registrarEntrada(p, bodega, 20, "Entrada IT");
        }
    }

    OrdenVenta crearOrden(int cantidad) {
        OrdenVenta o = new OrdenVenta(); o.setNumeroOrden("OV-" + UUID.randomUUID());
        o.setFecha(LocalDateTime.now()); o.setCliente(cliente); o.setUsuario(usuario); o.setObservaciones("Prueba IT");
        for (Producto p : productos) {
            DetalleOrdenVenta d = new DetalleOrdenVenta(); d.setProducto(p); d.setCantidad(cantidad);
            d.setPrecioUnitario(p.getPrecioVenta()); o.agregarDetalle(d);
        }
        new OrdenVentaService().crearOrdenVenta(o); return o;
    }
    Despacho prepararDespacho(OrdenVenta o) {
        Despacho d = new Despacho(); d.setOrden(o); d.setBodega(bodega);
        for (DetalleOrdenVenta od : o.getDetalles()) {
            DetalleDespacho dd = new DetalleDespacho(); dd.setProducto(od.getProducto()); dd.setDespacho(d);
            dd.setCantidadSolicitada(od.getCantidad()); dd.setCantidadDespachada(od.getCantidad()); d.getDetalles().add(dd);
        }
        return d;
    }
    Despacho generarConfirmar(OrdenVenta o) throws Exception {
        Despacho d=prepararDespacho(o); new DespachoService().generarDespacho(d);
        new DespachoService().confirmarDespacho(d); return d;
    }
    int stock(Producto p) { return inventario.consultarExistencia(p, bodega).getExistenciaActual(); }
    int contar(String sql, int id) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id); try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    @Test void flujoCompletoYConsultaTrasReinicio() throws Exception {
        OrdenVenta o = crearOrden(5);
        assertEquals(20, stock(productos.get(0)));
        assertThrows(IllegalStateException.class, () -> new FacturaService().crearDesdeOrden(o.getId(), usuario));
        Despacho d = prepararDespacho(o);
        new DespachoService().generarDespacho(d);
        assertEquals(EstadoDespacho.PENDIENTE, d.getEstado());
        assertEquals(20, stock(productos.get(0)));
        assertEquals(0, contar("SELECT SUM(cantidad_despachada) FROM dbo.DetalleDespacho WHERE id_despacho=?",d.getId()));
        Despacho recuperado = new DespachoService().obtenerPorOrden(o.getId());
        assertEquals(d.getNumeroDespacho(),recuperado.getNumeroDespacho());
        assertThrows(IllegalStateException.class, () -> new FacturaService().crearDesdeOrden(o.getId(),usuario));
        assertThrows(IllegalStateException.class, () -> new DespachoService().generarDespacho(prepararDespacho(o)));
        new DespachoService().confirmarDespacho(recuperado);
        assertEquals(15, stock(productos.get(0))); assertEquals(15, stock(productos.get(1)));
        assertEquals(EstadoOrdenVenta.COMPLETADA, new OrdenVentaDAOImpl().obtener(o.getId()).getEstado());
        // El precio del catálogo puede cambiar; se debe facturar el precio de la orden.
        productos.get(0).setPrecioVenta(new BigDecimal("99.00")); new ProductoDAOImpl().actualizar(productos.get(0));
        FacturaService servicio = new FacturaService();
        Factura f = servicio.emitir(servicio.crearDesdeOrden(o.getId(), usuario).getIdFactura());
        Factura recuperada = new FacturaService().consultarPorNumero(f.getNumero());
        assertEquals(o.getId(), recuperada.getOrden().getId());
        assertEquals(new BigDecimal("112.00"), recuperada.calcularTotal());
        assertEquals(new BigDecimal("10.00"), recuperada.getDetalles().get(0).getPrecioUnitario());
        assertEquals(15, stock(productos.get(0)));
        var lineas = new ConsultaOperacionService().consultar(o.getId());
        assertEquals(2, lineas.size());
        assertEquals(f.getNumero(), lineas.get(0).numeroFactura());
        assertEquals("SALIDA", lineas.get(0).tipoMovimiento());
        assertEquals(5, lineas.get(0).cantidadSalida());
        assertEquals(new BigDecimal("112.00"), lineas.get(0).totalFactura());
        var resumen = new OrdenVentaService().buscarPorFecha(LocalDateTime.now().minusDays(1), LocalDateTime.now()).stream()
                .filter(r -> r.getId().equals(o.getId())).findFirst().orElseThrow();
        assertEquals(f.getNumero(), resumen.getNumeroFactura());
        assertEquals(d.getNumeroDespacho(), resumen.getNumeroDespacho());
        assertEquals(2, contar("SELECT COUNT(*) FROM dbo.MovimientoInventario WHERE id_bodega=? AND tipo_movimiento='SALIDA'", bodega.getId()));
        servicio.registrarPago(f.getIdFactura(), new Pago(0, LocalDateTime.now(), new BigDecimal("112.00"), MetodoPago.EFECTIVO, "IT"));
        assertEquals(EstadoFactura.PAGADA, new FacturaService().consultarPorNumero(f.getNumero()).getEstado());
        servicio.anular(f.getIdFactura());
        assertEquals(EstadoFactura.ANULADA, new FacturaService().consultarPorNumero(f.getNumero()).getEstado());
        assertEquals(15, stock(productos.get(0)));
        assertThrows(IllegalStateException.class, () -> new FacturaService().crearDesdeOrden(o.getId(), usuario));
    }

    @Test void ensayoCompletoDesdeMenusSwing() throws Exception {
        EnsayoInterfaz.ejecutar(cliente, bodega, productos);
    }

    @Test void stockInsuficienteRevierteTodasLasLineas() throws Exception {
        OrdenVenta o = crearOrden(5);
        Despacho pendiente = prepararDespacho(o); new DespachoService().generarDespacho(pendiente);
        inventario.registrarSalida(productos.get(1), bodega, 19, "Otra salida IT", null);
        assertThrows(Exception.class, () -> new DespachoService().confirmarDespacho(pendiente));
        assertEquals(20, stock(productos.get(0))); assertEquals(1, stock(productos.get(1)));
        assertEquals(1, contar("SELECT COUNT(*) FROM dbo.Despacho WHERE id_orden=?", o.getId()));
        assertEquals(EstadoDespacho.PENDIENTE, new DespachoService().obtenerPorOrden(o.getId()).getEstado());
        assertEquals(1, contar("SELECT COUNT(*) FROM dbo.MovimientoInventario WHERE id_bodega=? AND tipo_movimiento='SALIDA'", bodega.getId()));
        assertEquals(EstadoOrdenVenta.PENDIENTE, new OrdenVentaDAOImpl().obtener(o.getId()).getEstado());
        assertTrue(new DespachoService().obtenerPorOrden(o.getId()).getDetalles().stream()
                .allMatch(d -> d.getCantidadDespachada()==0));
        // Puede abastecerse y reintentarse el mismo despacho, sin generar otro.
        inventario.registrarEntrada(productos.get(1), bodega, 4, "Reposición IT");
        new DespachoService().confirmarDespacho(pendiente);
        assertEquals(15, stock(productos.get(0))); assertEquals(0, stock(productos.get(1)));
        assertEquals(1, contar("SELECT COUNT(*) FROM dbo.Despacho WHERE id_orden=?", o.getId()));
    }

    @Test void rechazaDespachoParcialYRepetido() throws Exception {
        OrdenVenta o = crearOrden(5);
        Despacho parcial = prepararDespacho(o); parcial.getDetalles().get(0).setCantidadDespachada(4);
        assertThrows(IllegalArgumentException.class, () -> new DespachoService().generarDespacho(parcial));
        assertEquals(20, stock(productos.get(0)));
        Despacho incompleto = prepararDespacho(o); incompleto.getDetalles().remove(1);
        assertThrows(IllegalArgumentException.class, () -> new DespachoService().generarDespacho(incompleto));
        generarConfirmar(o);
        assertThrows(IllegalStateException.class, () -> new DespachoService().confirmarDespacho(new DespachoService().obtenerPorOrden(o.getId())));
        assertEquals(15, stock(productos.get(0)));
    }

    @Test void errorAlGuardarFacturaNoDejaEncabezadoNiDetalles() throws Exception {
        OrdenVenta o = crearOrden(5); generarConfirmar(o);
        FacturaService servicio = new FacturaService(); Factura f = servicio.crearDesdeOrden(o.getId(), usuario);
        f.getUsuario().setIdUsuario(Long.MAX_VALUE); // Fuerza fallo de FK durante emisión.
        assertThrows(IllegalStateException.class, () -> servicio.emitir(f.getIdFactura()));
        assertEquals(0, contar("SELECT COUNT(*) FROM dbo.factura WHERE id_orden=?", o.getId()));
        assertEquals(EstadoFactura.BORRADOR, f.getEstado()); assertEquals(15, stock(productos.get(0)));
    }

    @Test void noPermiteFacturarDosVeces() throws Exception {
        OrdenVenta o = crearOrden(5); generarConfirmar(o);
        FacturaService a = new FacturaService(), b = new FacturaService();
        Factura fa = a.crearDesdeOrden(o.getId(), usuario), fb = b.crearDesdeOrden(o.getId(), usuario);
        a.emitir(fa.getIdFactura());
        assertThrows(IllegalStateException.class, () -> b.emitir(fb.getIdFactura()));
        assertEquals(1, contar("SELECT COUNT(*) FROM dbo.factura WHERE id_orden=?", o.getId()));
    }

    @Test void confirmacionesConcurrentesSoloDescuentanUnaVez() throws Exception {
        OrdenVenta o = crearOrden(5);
        assertEquals(1, confirmarConcurrentes(o, o));
        assertEquals(15, stock(productos.get(0)));
        assertEquals(1, contar("SELECT COUNT(*) FROM dbo.Despacho WHERE id_orden=?", o.getId()));
    }

    @Test void ordenesConcurrentesNoProducenStockNegativo() throws Exception {
        OrdenVenta a = crearOrden(15), b = crearOrden(15);
        assertEquals(1, confirmarConcurrentes(a, b));
        assertEquals(5, stock(productos.get(0))); assertEquals(5, stock(productos.get(1)));
    }

    int confirmarConcurrentes(OrdenVenta a, OrdenVenta b) throws Exception {
        Map<Integer,Integer> ids = new HashMap<>();
        for (OrdenVenta o : List.of(a,b)) if (!ids.containsKey(o.getId())) {
            Despacho d=prepararDespacho(o); new DespachoService().generarDespacho(d); ids.put(o.getId(),d.getId());
        }
        CountDownLatch inicio = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            List<Future<Boolean>> intentos = new ArrayList<>();
            for (OrdenVenta o : List.of(a, b)) intentos.add(pool.submit(() -> {
                inicio.await();
                try { new DespachoService().confirmarDespacho(ids.get(o.getId())); return true; }
                catch (Exception e) { return false; }
            }));
            inicio.countDown();
            int exitos = 0; for (Future<Boolean> intento : intentos) if (intento.get(20, TimeUnit.SECONDS)) exitos++;
            return exitos;
        }
    }

    @AfterEach void limpiarSoloDatosDelCaso() throws SQLException {
        if (cliente == null || cliente.getIdCliente() <= 0) return;
        String ordenes = "SELECT id FROM dbo.OrdenVenta WHERE id_cliente=?";
        List<String> sql = List.of(
                "DELETE dbo.pago WHERE factura_id IN (SELECT id_factura FROM dbo.factura WHERE id_orden IN (" + ordenes + "))",
                "DELETE dbo.detalle_factura WHERE factura_id IN (SELECT id_factura FROM dbo.factura WHERE id_orden IN (" + ordenes + "))",
                "DELETE dbo.factura WHERE id_orden IN (" + ordenes + ")",
                "DELETE dbo.DetalleDespacho WHERE id_despacho IN (SELECT id FROM dbo.Despacho WHERE id_orden IN (" + ordenes + "))",
                "DELETE dbo.Despacho WHERE id_orden IN (" + ordenes + ")",
                "DELETE dbo.DetalleOrdenVenta WHERE id_orden IN (" + ordenes + ")",
                "DELETE dbo.OrdenVenta WHERE id_cliente=?");
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                for (String query : sql) ejecutar(con, query, cliente.getIdCliente());
                if (bodega != null && bodega.getId() != null) {
                    ejecutar(con, "DELETE dbo.MovimientoInventario WHERE id_bodega=?", bodega.getId());
                    ejecutar(con, "DELETE dbo.ExistenciaInventario WHERE id_bodega=?", bodega.getId());
                    ejecutar(con, "DELETE dbo.Bodega WHERE id=?", bodega.getId());
                }
                for (Producto p : productos) ejecutar(con, "DELETE dbo.producto WHERE id_producto=?", p.getIdProducto());
                if (categoria != null && categoria.getIdCategoria() > 0) ejecutar(con, "DELETE dbo.categoria WHERE id_categoria=?", categoria.getIdCategoria());
                ejecutar(con, "DELETE dbo.cliente WHERE id_cliente=?", cliente.getIdCliente()); con.commit();
            } catch (SQLException e) { con.rollback(); throw e; }
        }
    }
    void ejecutar(Connection con, String sql, long id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) { ps.setLong(1, id); ps.executeUpdate(); }
    }
}
